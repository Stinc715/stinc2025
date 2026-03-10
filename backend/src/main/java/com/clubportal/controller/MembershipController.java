package com.clubportal.controller;

import com.clubportal.dto.ClubMembershipMemberResponse;
import com.clubportal.dto.MembershipPlanResponse;
import com.clubportal.dto.MembershipPlanUpsertRequest;
import com.clubportal.dto.MembershipPurchaseResponse;
import com.clubportal.dto.MyMembershipResponse;
import com.clubportal.model.Club;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.TransactionRecord;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
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
    private final ClubRepository clubRepo;
    private final UserRepository userRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final CurrentUserService currentUserService;
    private final MembershipService membershipService;

    public MembershipController(MembershipPlanRepository membershipPlanRepo,
                                UserMembershipRepository userMembershipRepo,
                                TransactionRecordRepository transactionRepo,
                                ClubRepository clubRepo,
                                UserRepository userRepo,
                                ClubAdminRepository clubAdminRepo,
                                CurrentUserService currentUserService,
                                MembershipService membershipService) {
        this.membershipPlanRepo = membershipPlanRepo;
        this.userMembershipRepo = userMembershipRepo;
        this.transactionRepo = transactionRepo;
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
        membershipService.ensureStandardPlansForClub(clubId);
        return ResponseEntity.ok(membershipPlanRepo.findByClubIdAndEnabledTrueOrderByDurationDaysAsc(clubId).stream()
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

        return ResponseEntity.ok(membershipService.ensureStandardPlansForClub(clubId).stream()
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
                .filter(plan -> !membershipService.normalizePlanCode(plan.getPlanCode()).isBlank())
                .collect(Collectors.toMap(
                        plan -> membershipService.normalizePlanCode(plan.getPlanCode()),
                        plan -> plan,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        Set<String> seenCodes = new HashSet<>();
        List<MembershipPlan> saved = new ArrayList<>();
        for (MembershipPlanUpsertRequest row : rows) {
            String planCode = membershipService.normalizePlanCode(row.getPlanCode());
            if (planCode.isBlank()) {
                return ResponseEntity.badRequest().body("Invalid membership plan code");
            }
            if (!seenCodes.add(planCode)) {
                return ResponseEntity.badRequest().body("Duplicate membership plan code: " + planCode);
            }

            MembershipPlan plan = existingByCode.getOrDefault(planCode, new MembershipPlan());
            plan.setClubId(clubId);
            plan.setPlanCode(planCode);

            String planName = safe(row.getPlanName());
            plan.setPlanName(planName.isBlank() ? membershipService.defaultPlanName(planCode) : planName);

            Integer durationDays = row.getDurationDays();
            if (durationDays == null || durationDays <= 0) {
                durationDays = membershipService.defaultDurationDays(planCode);
            }
            plan.setDurationDays(durationDays);

            BigDecimal price = row.getPrice();
            if (price == null && plan.getPrice() != null) {
                price = plan.getPrice();
            }
            plan.setPrice(membershipService.normalizePrice(price));

            BigDecimal discountPercent = row.getDiscountPercent();
            if (discountPercent == null && plan.getDiscountPercent() != null) {
                discountPercent = plan.getDiscountPercent();
            }
            BigDecimal normalizedDiscount = membershipService.normalizeDiscount(discountPercent);
            plan.setDiscountPercent(normalizedDiscount);
            plan.setEnabled(membershipService.normalizeEnabled(
                    row.getEnabled(),
                    membershipService.normalizeEnabled(plan.getEnabled(), true)
            ));

            String description = safe(row.getDescription());
            plan.setDescription(description.isBlank()
                    ? membershipService.defaultDescription(planCode, normalizedDiscount)
                    : description);

            saved.add(membershipPlanRepo.save(plan));
        }

        saved.sort(Comparator.comparingInt(MembershipPlan::getDurationDays));
        return ResponseEntity.ok(saved.stream().map(this::toPlanResponse).toList());
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
                            safe(plan.getPlanName()),
                            membershipService.normalizePrice(plan.getPrice()),
                            membershipService.normalizeDiscount(plan.getDiscountPercent()),
                            membership.getStartDate(),
                            membership.getEndDate(),
                            membershipService.effectiveStatus(membership, today),
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
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can purchase memberships");
        }

        MembershipPlan plan = membershipPlanRepo.findById(planId).orElse(null);
        if (plan == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Membership plan not found");
        }
        if (!Boolean.TRUE.equals(plan.getEnabled())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("This membership plan is currently unavailable");
        }

        Club club = clubRepo.findById(plan.getClubId()).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        Optional<MembershipService.ActiveMembershipContext> activeMembership = membershipService.findActiveMembership(
                me.getUserId(),
                plan.getClubId(),
                LocalDate.now()
        );
        if (activeMembership.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("You already have an active membership for this club until " + activeMembership.get().membership().getEndDate());
        }

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(Math.max(1, Optional.ofNullable(plan.getDurationDays()).orElse(30)) - 1L);

        UserMembership membership = new UserMembership();
        membership.setUserId(me.getUserId());
        membership.setPlanId(plan.getPlanId());
        membership.setStartDate(startDate);
        membership.setEndDate(endDate);
        membership.setStatus("ACTIVE");
        UserMembership savedMembership = userMembershipRepo.save(membership);

        TransactionRecord tx = new TransactionRecord();
        tx.setUserId(me.getUserId());
        tx.setUserMembershipId(savedMembership.getUserMembershipId());
        tx.setAmount(membershipService.normalizePrice(plan.getPrice()));
        tx.setPaymentMethod("MOCK_CARD");
        tx.setStatus("PAID");
        transactionRepo.save(tx);

        return ResponseEntity.ok(new MembershipPurchaseResponse(
                savedMembership.getUserMembershipId(),
                plan.getPlanId(),
                plan.getClubId(),
                safe(club.getClubName()),
                safe(plan.getPlanCode()),
                safe(plan.getPlanName()),
                membershipService.normalizePrice(plan.getPrice()),
                membershipService.normalizeDiscount(plan.getDiscountPercent()),
                savedMembership.getStartDate(),
                savedMembership.getEndDate(),
                membershipService.effectiveStatus(savedMembership, LocalDate.now())
        ));
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

        LocalDate today = LocalDate.now();
        return memberships.stream()
                .map(membership -> {
                    MembershipPlan plan = planById.get(membership.getPlanId());
                    if (plan == null) return null;
                    Club club = clubById.get(plan.getClubId());
                    return new MyMembershipResponse(
                            membership.getUserMembershipId(),
                            plan.getClubId(),
                            club == null ? "" : safe(club.getClubName()),
                            plan.getPlanId(),
                            safe(plan.getPlanCode()),
                            safe(plan.getPlanName()),
                            membershipService.normalizePrice(plan.getPrice()),
                            membershipService.normalizeDiscount(plan.getDiscountPercent()),
                            membership.getStartDate(),
                            membership.getEndDate(),
                            membershipService.effectiveStatus(membership, today),
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
                safe(plan.getPlanName()),
                membershipService.normalizePrice(plan.getPrice()),
                Optional.ofNullable(plan.getDurationDays()).orElse(0),
                membershipService.normalizeDiscount(plan.getDiscountPercent()),
                membershipService.normalizeEnabled(plan.getEnabled(), true),
                safe(plan.getDescription())
        );
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
            case "SCHEDULED" -> 1;
            case "EXPIRED" -> 2;
            default -> 3;
        };
    }

    private static String safe(String raw) {
        return raw == null ? "" : raw.trim();
    }
}
