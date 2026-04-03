package com.clubportal.controller;

import com.clubportal.dto.ClubMembershipMemberResponse;
import com.clubportal.dto.MembershipPlanResponse;
import com.clubportal.dto.MembershipPlanUpsertRequest;
import com.clubportal.dto.MembershipPurchaseResponse;
import com.clubportal.dto.MyMembershipResponse;
import com.clubportal.model.Club;
import com.clubportal.model.CheckoutSession;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.TransactionRecord;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.CheckoutSessionRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.TransactionRecordRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.MembershipService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class MembershipController {

    private final MembershipPlanRepository membershipPlanRepo;
    private final UserMembershipRepository userMembershipRepo;
    private final TransactionRecordRepository transactionRepo;
    private final CheckoutSessionRepository checkoutSessionRepo;
    private final ClubRepository clubRepo;
    private final UserRepository userRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final CurrentUserService currentUserService;
    private final MembershipService membershipService;

    public MembershipController(MembershipPlanRepository membershipPlanRepo,
                                UserMembershipRepository userMembershipRepo,
                                TransactionRecordRepository transactionRepo,
                                CheckoutSessionRepository checkoutSessionRepo,
                                ClubRepository clubRepo,
                                UserRepository userRepo,
                                ClubAdminRepository clubAdminRepo,
                                CurrentUserService currentUserService,
                                MembershipService membershipService) {
        this.membershipPlanRepo = membershipPlanRepo;
        this.userMembershipRepo = userMembershipRepo;
        this.transactionRepo = transactionRepo;
        this.checkoutSessionRepo = checkoutSessionRepo;
        this.clubRepo = clubRepo;
        this.userRepo = userRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.currentUserService = currentUserService;
        this.membershipService = membershipService;
    }

    @GetMapping("/clubs/{clubId}/membership-plans")
    public ResponseEntity<?> listPublicMembershipPlans(@PathVariable Integer clubId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }
        return ResponseEntity.ok(membershipService.catalogPlans(membershipPlanRepo.findByClubIdAndEnabledTrueOrderByDurationDaysAsc(clubId)).stream()
                .map(this::toPlanResponse)
                .toList());
    }

    @GetMapping("/my/clubs/{clubId}/membership-plans")
    public ResponseEntity<?> listClubMembershipPlans(@PathVariable Integer clubId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        return ResponseEntity.ok(membershipService.catalogPlans(membershipPlanRepo.findByClubIdOrderByDurationDaysAsc(clubId)).stream()
                .map(this::toPlanResponse)
                .toList());
    }

    @PutMapping("/my/clubs/{clubId}/membership-plans")
    @Transactional
    public ResponseEntity<?> upsertClubMembershipPlans(@PathVariable Integer clubId,
                                                       @RequestBody List<MembershipPlanUpsertRequest> request) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        List<MembershipPlanUpsertRequest> rows = request == null ? List.of() : request.stream().filter(Objects::nonNull).toList();
        if (rows.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing membership plan data");
        }

        Map<String, MembershipPlan> existingByCode = membershipPlanRepo.findByClubId(clubId).stream()
                .filter(plan -> !membershipService.normalizeStandardPlanCode(plan.getPlanCode()).isBlank())
                .collect(Collectors.toMap(
                        plan -> membershipService.normalizeStandardPlanCode(plan.getPlanCode()),
                        plan -> plan,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Set<String> seenCodes = new HashSet<>();
        List<MembershipPlan> saved = new ArrayList<>();
        for (MembershipPlanUpsertRequest row : rows) {
            MembershipPlan plan;
            String planCode = membershipService.normalizeStandardPlanCode(row.getPlanCode());
            if (row.getPlanId() != null) {
                plan = membershipPlanRepo.findById(row.getPlanId()).orElse(null);
                if (plan == null || !Objects.equals(plan.getClubId(), clubId)) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membership plan not found");
                }
                String existingCode = membershipService.normalizePlanCode(plan.getPlanCode());
                planCode = membershipService.isStandardPlanCode(existingCode)
                        ? membershipService.normalizeStandardPlanCode(existingCode)
                        : existingCode;
            } else if (!planCode.isBlank()) {
                plan = existingByCode.getOrDefault(planCode, new MembershipPlan());
            } else {
                return ResponseEntity.badRequest().body("Missing membership plan identifier");
            }
            if (!seenCodes.add(planCode)) {
                return ResponseEntity.badRequest().body("Duplicate membership plan code: " + planCode);
            }
            try {
                saved.add(saveMembershipPlan(clubId, plan, row, !membershipService.isStandardPlanCode(planCode)));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body(ex.getMessage());
            }
        }

        return ResponseEntity.ok(membershipService.sortPlans(saved).stream().map(this::toPlanResponse).toList());
    }

    @PostMapping("/my/clubs/{clubId}/membership-plans")
    @Transactional
    public ResponseEntity<?> createCustomMembershipPlan(@PathVariable Integer clubId,
                                                        @RequestBody MembershipPlanUpsertRequest request) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        MembershipPlan draft = new MembershipPlan();
        draft.setClubId(clubId);
        draft.setPlanCode(membershipService.newCustomPlanCode());
        try {
            MembershipPlan saved = saveMembershipPlan(clubId, draft, request, true);
            return ResponseEntity.status(HttpStatus.CREATED).body(toPlanResponse(saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/my/clubs/{clubId}/membership-plans/{planId}")
    @Transactional
    public ResponseEntity<?> deleteCustomMembershipPlan(@PathVariable Integer clubId,
                                                        @PathVariable Integer planId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        MembershipPlan plan = membershipPlanRepo.findById(planId).orElse(null);
        if (plan == null || !Objects.equals(plan.getClubId(), clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membership plan not found");
        }
        if (membershipService.isStandardPlanCode(plan.getPlanCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Standard membership plans cannot be deleted");
        }
        if (userMembershipRepo.existsByPlanId(planId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This membership plan has already been purchased and cannot be deleted");
        }
        membershipPlanRepo.delete(plan);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my/memberships")
    public ResponseEntity<?> listMyMemberships() {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can view memberships");
        }

        return ResponseEntity.ok(loadUserMembershipResponses(me.getUserId()));
    }

    @GetMapping("/my/clubs/{clubId}/membership")
    public ResponseEntity<?> myActiveMembershipForClub(@PathVariable Integer clubId) {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can view memberships");
        }

        List<MyMembershipResponse> memberships = loadUserMembershipResponses(me.getUserId()).stream()
                .filter(row -> Objects.equals(row.clubId(), clubId))
                .filter(row -> "ACTIVE".equalsIgnoreCase(row.status()))
                .sorted(Comparator.comparing(MyMembershipResponse::endDate, Comparator.nullsLast(LocalDate::compareTo)).reversed())
                .toList();

        if (memberships.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active membership");
        }
        return ResponseEntity.ok(memberships.get(0));
    }

    @GetMapping("/my/clubs/{clubId}/memberships")
    public ResponseEntity<?> listClubMemberships(@PathVariable Integer clubId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        List<MembershipPlan> plans = membershipPlanRepo.findByClubIdOrderByDurationDaysAsc(clubId);
        if (plans.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Map<Integer, MembershipPlan> planById = plans.stream()
                .collect(Collectors.toMap(MembershipPlan::getPlanId, plan -> plan));

        List<Integer> planIds = plans.stream().map(MembershipPlan::getPlanId).toList();
        List<UserMembership> memberships = userMembershipRepo.findByPlanIdInOrderByCreatedAtDesc(planIds);
        if (memberships.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        Set<Integer> userIds = memberships.stream()
                .map(UserMembership::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, User> userById = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, user -> user));

        LocalDate today = LocalDate.now();
        List<ClubMembershipMemberResponse> out = memberships.stream()
                .map(membership -> {
                    MembershipPlan plan = planById.get(membership.getPlanId());
                    if (plan == null) return null;
                    User user = userById.get(membership.getUserId());
                    return new ClubMembershipMemberResponse(
                            membership.getUserMembershipId(),
                            membership.getUserId(),
                            user == null ? "" : safe(user.getUsername()),
                            user == null ? "" : safe(user.getEmail()),
                            plan.getPlanId(),
                            safe(plan.getPlanCode()),
                            membershipService.normalizeBenefitType(plan.getBenefitType()),
                            safe(plan.getPlanName()),
                            membershipService.normalizePrice(plan.getPrice()),
                            membershipService.normalizeDiscount(plan.getDiscountPercent()),
                            membershipService.normalizeIncludedBookings(plan.getIncludedBookings()),
                            membershipService.normalizeRemainingBookings(membership.getRemainingBookings()),
                            membership.getStartDate(),
                            membership.getEndDate(),
                            membershipService.effectiveStatus(membership, plan, today),
                            membership.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt((ClubMembershipMemberResponse row) -> statusRank(row.status()))
                        .thenComparing(ClubMembershipMemberResponse::endDate, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ClubMembershipMemberResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return ResponseEntity.ok(out);
    }

    @PostMapping("/membership-plans/{planId}/purchase")
    @Transactional
    public ResponseEntity<?> purchaseMembership(@PathVariable Integer planId) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body("Create a checkout session first via /api/payments/checkout-sessions");
    }

    private List<MyMembershipResponse> loadUserMembershipResponses(Integer userId) {
        List<UserMembership> memberships = userMembershipRepo.findByUserIdOrderByCreatedAtDesc(userId);
        if (memberships.isEmpty()) return List.of();

        Set<Integer> planIds = memberships.stream()
                .map(UserMembership::getPlanId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, MembershipPlan> planById = membershipPlanRepo.findAllById(planIds).stream()
                .collect(Collectors.toMap(MembershipPlan::getPlanId, plan -> plan));

        Set<Integer> clubIds = planById.values().stream()
                .map(MembershipPlan::getClubId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Club> clubById = clubRepo.findAllById(clubIds).stream()
                .collect(Collectors.toMap(Club::getClubId, club -> club));

        Set<Integer> membershipIds = memberships.stream()
                .map(UserMembership::getUserMembershipId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, String> orderNoByMembershipId = new HashMap<>();
        if (!membershipIds.isEmpty()) {
            for (CheckoutSession session : checkoutSessionRepo.findByUserMembershipIdInAndStatusOrderByCreatedAtDesc(membershipIds, "PAID")) {
                if (session.getUserMembershipId() != null) {
                    orderNoByMembershipId.putIfAbsent(session.getUserMembershipId(), safe(session.getOrderNo()));
                }
            }
        }

        LocalDate today = LocalDate.now();
        return memberships.stream()
                .map(membership -> {
                    MembershipPlan plan = planById.get(membership.getPlanId());
                    if (plan == null) return null;
                    Club club = clubById.get(plan.getClubId());
                    return new MyMembershipResponse(
                            membership.getUserMembershipId(),
                            orderNoByMembershipId.getOrDefault(membership.getUserMembershipId(), ""),
                            plan.getClubId(),
                            club == null ? "" : safe(club.getClubName()),
                            plan.getPlanId(),
                            safe(plan.getPlanCode()),
                            membershipService.normalizeBenefitType(plan.getBenefitType()),
                            safe(plan.getPlanName()),
                            membershipService.normalizePrice(plan.getPrice()),
                            membershipService.normalizeDiscount(plan.getDiscountPercent()),
                            membershipService.normalizeIncludedBookings(plan.getIncludedBookings()),
                            membershipService.normalizeRemainingBookings(membership.getRemainingBookings()),
                            membership.getStartDate(),
                            membership.getEndDate(),
                            membershipService.effectiveStatus(membership, plan, today),
                            membership.getCreatedAt()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private MembershipPlanResponse toPlanResponse(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getPlanId(),
                plan.getClubId(),
                safe(plan.getPlanCode()),
                membershipService.normalizeBenefitType(plan.getBenefitType()),
                safe(plan.getPlanName()),
                membershipService.normalizePrice(plan.getPrice()),
                Optional.ofNullable(plan.getDurationDays()).orElse(0),
                membershipService.normalizeDiscount(plan.getDiscountPercent()),
                membershipService.normalizeIncludedBookings(plan.getIncludedBookings()),
                membershipService.normalizeEnabled(plan.getEnabled(), false),
                membershipService.isStandardPlanCode(plan.getPlanCode()),
                safe(plan.getDescription())
        );
    }

    private MembershipPlan saveMembershipPlan(Integer clubId,
                                              MembershipPlan plan,
                                              MembershipPlanUpsertRequest row,
                                              boolean allowCustomPlan) {
        if (plan == null) {
            throw new IllegalArgumentException("Membership plan not found");
        }
        plan.setClubId(clubId);
        String existingCode = membershipService.normalizePlanCode(plan.getPlanCode());
        String standardCode = membershipService.normalizeStandardPlanCode(existingCode);
        String planCode = standardCode.isBlank()
                ? (allowCustomPlan ? existingCode : membershipService.normalizeStandardPlanCode(row == null ? null : row.getPlanCode()))
                : standardCode;
        if (planCode.isBlank()) {
            throw new IllegalArgumentException("Invalid membership plan code");
        }
        plan.setPlanCode(planCode);

        String benefitType = membershipService.normalizeBenefitType(row == null ? null : row.getBenefitType());
        if (safe(plan.getBenefitType()).isBlank()) {
            plan.setBenefitType(benefitType);
        } else if (safe(row == null ? null : row.getBenefitType()).isBlank()) {
            benefitType = membershipService.normalizeBenefitType(plan.getBenefitType());
        }
        plan.setBenefitType(benefitType);

        Integer durationDays = row == null ? null : row.getDurationDays();
        if (durationDays == null || durationDays <= 0) {
            durationDays = plan.getDurationDays();
        }
        if (durationDays == null || durationDays <= 0) {
            durationDays = membershipService.isStandardPlanCode(planCode)
                    ? membershipService.defaultDurationDays(planCode)
                    : 30;
        }
        plan.setDurationDays(durationDays);

        BigDecimal price = row == null ? null : row.getPrice();
        if (price == null) {
            price = plan.getPrice();
        }
        if (price == null && membershipService.isStandardPlanCode(planCode)) {
            price = membershipService.defaultPlanPrice(planCode);
        }
        plan.setPrice(membershipService.normalizePrice(price));

        BigDecimal discountPercent = row == null ? null : row.getDiscountPercent();
        if (discountPercent == null) {
            discountPercent = plan.getDiscountPercent();
        }
        if (discountPercent == null && membershipService.isStandardPlanCode(planCode)) {
            discountPercent = membershipService.defaultPlanDiscount(planCode);
        }
        BigDecimal normalizedDiscount = membershipService.normalizeDiscount(discountPercent);

        Integer includedBookings = row == null ? null : row.getIncludedBookings();
        if (includedBookings == null) {
            includedBookings = plan.getIncludedBookings();
        }
        int normalizedIncludedBookings = membershipService.normalizeIncludedBookings(includedBookings);

        if (MembershipService.BENEFIT_BOOKING_PACK.equals(benefitType)) {
            if (normalizedIncludedBookings <= 0) {
                throw new IllegalArgumentException("Booking packs must include at least 1 booking");
            }
            plan.setDiscountPercent(BigDecimal.ZERO.setScale(2));
            plan.setIncludedBookings(normalizedIncludedBookings);
        } else {
            plan.setDiscountPercent(normalizedDiscount);
            plan.setIncludedBookings(0);
        }

        plan.setEnabled(membershipService.normalizeEnabled(
                row == null ? null : row.getEnabled(),
                membershipService.normalizeEnabled(plan.getEnabled(), false)
        ));

        String planName = safe(row == null ? null : row.getPlanName());
        if (planName.isBlank()) {
            planName = membershipService.isStandardPlanCode(planCode)
                    ? membershipService.defaultPlanName(planCode)
                    : membershipService.defaultCustomPlanName(benefitType);
        }
        plan.setPlanName(planName);

        String description = safe(row == null ? null : row.getDescription());
        if (description.isBlank()) {
            description = membershipService.defaultDescription(
                    benefitType,
                    planCode,
                    plan.getDiscountPercent(),
                    plan.getIncludedBookings(),
                    plan.getDurationDays()
            );
        }
        plan.setDescription(description);

        return membershipPlanRepo.save(plan);
    }

    private ResponseEntity<?> requireClubAdmin(User me, Integer clubId) {
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can access this resource");
        }

        boolean isAdmin = me.getRole() == User.Role.ADMIN;
        boolean isClubAdmin = clubAdminRepo.existsByUserIdAndClubId(me.getUserId(), clubId);
        if (!isAdmin && !isClubAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only access your own club");
        }
        return null;
    }

    private static int statusRank(String status) {
        return switch (safe(status).toUpperCase(Locale.ROOT)) {
            case "ACTIVE" -> 0;
            case "EXHAUSTED" -> 1;
            case "SCHEDULED" -> 2;
            case "EXPIRED" -> 3;
            default -> 4;
        };
    }

    private static String safe(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
