package com.clubportal.service;

import com.clubportal.model.MembershipPlan;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.UserMembershipRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MembershipService {

    public static final String PLAN_MONTHLY = "MONTHLY";
    public static final String PLAN_QUARTERLY = "QUARTERLY";
    public static final String PLAN_HALF_YEAR = "HALF_YEAR";
    public static final String PLAN_YEARLY = "YEARLY";
    public static final String BENEFIT_DISCOUNT = "DISCOUNT";
    public static final String BENEFIT_BOOKING_PACK = "BOOKING_PACK";
    public static final List<String> STANDARD_PLAN_CODES = List.of(PLAN_MONTHLY, PLAN_QUARTERLY, PLAN_HALF_YEAR, PLAN_YEARLY);

    private final MembershipPlanRepository membershipPlanRepo;
    private final UserMembershipRepository userMembershipRepo;

    public MembershipService(MembershipPlanRepository membershipPlanRepo,
                             UserMembershipRepository userMembershipRepo) {
        this.membershipPlanRepo = membershipPlanRepo;
        this.userMembershipRepo = userMembershipRepo;
    }

    public Optional<ActiveMembershipContext> findActiveMembership(Integer userId, Integer clubId, LocalDate onDate) {
        if (userId == null || clubId == null) return Optional.empty();

        List<UserMembership> memberships = userMembershipRepo.findByUserIdOrderByCreatedAtDesc(userId);
        if (memberships.isEmpty()) return Optional.empty();

        Set<Integer> planIds = memberships.stream()
                .map(UserMembership::getPlanId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (planIds.isEmpty()) return Optional.empty();

        Map<Integer, MembershipPlan> planById = membershipPlanRepo.findAllById(planIds).stream()
                .collect(Collectors.toMap(MembershipPlan::getPlanId, plan -> plan));

        return memberships.stream()
                .filter(membership -> {
                    MembershipPlan plan = planById.get(membership.getPlanId());
                    return "ACTIVE".equals(effectiveStatus(membership, plan, onDate));
                })
                .map(membership -> {
                    MembershipPlan plan = planById.get(membership.getPlanId());
                    if (plan == null || !clubId.equals(plan.getClubId())) return null;
                    return new ActiveMembershipContext(membership, plan);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparing((ActiveMembershipContext ctx) -> ctx.membership().getEndDate(), Comparator.nullsLast(LocalDate::compareTo))
                        .reversed()
                        .thenComparing(ctx -> normalizeRemainingBookings(ctx.membership().getRemainingBookings()), Comparator.reverseOrder())
                        .thenComparing(ctx -> normalizeDiscount(ctx.plan().getDiscountPercent()), Comparator.reverseOrder())
                        .thenComparing(ctx -> ctx.membership().getCreatedAt(), Comparator.nullsLast(java.time.LocalDateTime::compareTo)))
                .findFirst();
    }

    public String effectiveStatus(UserMembership membership, LocalDate onDate) {
        if (membership == null) return "INACTIVE";
        String raw = safeUpper(membership.getStatus());
        if ("CANCELLED".equals(raw) || "INACTIVE".equals(raw)) return raw;
        LocalDate date = onDate == null ? LocalDate.now() : onDate;
        if (membership.getStartDate() != null && membership.getStartDate().isAfter(date)) return "SCHEDULED";
        if (membership.getEndDate() != null && membership.getEndDate().isBefore(date)) return "EXPIRED";
        return "ACTIVE";
    }

    public String effectiveStatus(UserMembership membership, MembershipPlan plan, LocalDate onDate) {
        String status = effectiveStatus(membership, onDate);
        if (!"ACTIVE".equals(status)) {
            return status;
        }
        if (isBookingPack(plan) && normalizeRemainingBookings(membership == null ? null : membership.getRemainingBookings()) <= 0) {
            return "EXHAUSTED";
        }
        return status;
    }

    public BigDecimal calculateDiscountedPrice(BigDecimal basePrice, BigDecimal discountPercent) {
        BigDecimal base = normalizePrice(basePrice);
        BigDecimal discount = normalizeDiscount(discountPercent);
        if (discount.signum() <= 0) return base;
        BigDecimal multiplier = BigDecimal.valueOf(100).subtract(discount)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
        return base.multiply(multiplier).setScale(2, RoundingMode.HALF_UP).max(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    public BigDecimal calculateBookingPrice(BigDecimal basePrice, ActiveMembershipContext ctx) {
        BigDecimal base = normalizePrice(basePrice);
        if (ctx == null || ctx.plan() == null || ctx.membership() == null) {
            return base;
        }
        if (isBookingPack(ctx.plan())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return calculateDiscountedPrice(base, ctx.plan().getDiscountPercent());
    }

    public BigDecimal normalizePrice(BigDecimal raw) {
        if (raw == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return raw.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal normalizeDiscount(BigDecimal raw) {
        if (raw == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalized = raw.setScale(2, RoundingMode.HALF_UP);
        if (normalized.signum() < 0) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (normalized.compareTo(BigDecimal.valueOf(100)) > 0) return BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
        return normalized;
    }

    public boolean normalizeEnabled(Boolean raw, boolean fallback) {
        return raw == null ? fallback : raw;
    }

    public String normalizePlanCode(String raw) {
        String code = safeUpper(raw);
        if (code.isBlank()) return "";
        String sanitized = code.replaceAll("[^A-Z0-9_]+", "_");
        return sanitized.length() <= 20 ? sanitized : sanitized.substring(0, 20);
    }

    public String normalizeStandardPlanCode(String raw) {
        String code = normalizePlanCode(raw);
        return STANDARD_PLAN_CODES.contains(code) ? code : "";
    }

    public boolean isStandardPlanCode(String raw) {
        return !normalizeStandardPlanCode(raw).isBlank();
    }

    public boolean isCatalogPlan(MembershipPlan plan) {
        return plan != null && !isStandardPlanCode(plan.getPlanCode());
    }

    public boolean isSellableCatalogPlan(MembershipPlan plan) {
        return isCatalogPlan(plan) && Boolean.TRUE.equals(plan.getEnabled());
    }

    public List<MembershipPlan> catalogPlans(List<MembershipPlan> plans) {
        return sortPlans(plans).stream()
                .filter(this::isCatalogPlan)
                .toList();
    }

    public boolean isBookingPack(MembershipPlan plan) {
        return BENEFIT_BOOKING_PACK.equals(normalizeBenefitType(plan == null ? null : plan.getBenefitType()));
    }

    public String normalizeBenefitType(String raw) {
        String key = safeUpper(raw);
        if (BENEFIT_BOOKING_PACK.equals(key)) return BENEFIT_BOOKING_PACK;
        return BENEFIT_DISCOUNT;
    }

    public Integer normalizeIncludedBookings(Integer raw) {
        if (raw == null || raw < 0) return 0;
        return raw;
    }

    public Integer normalizeRemainingBookings(Integer raw) {
        if (raw == null || raw < 0) return 0;
        return raw;
    }

    public String newCustomPlanCode() {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        return normalizePlanCode("CUSTOM_" + token);
    }

    public String defaultPlanName(String planCode) {
        return switch (normalizeStandardPlanCode(planCode)) {
            case PLAN_MONTHLY -> "Monthly Pass";
            case PLAN_QUARTERLY -> "Quarterly Pass";
            case PLAN_HALF_YEAR -> "Half-year Pass";
            case PLAN_YEARLY -> "Yearly Pass";
            default -> "Membership";
        };
    }

    public int defaultDurationDays(String planCode) {
        return switch (normalizeStandardPlanCode(planCode)) {
            case PLAN_MONTHLY -> 30;
            case PLAN_QUARTERLY -> 90;
            case PLAN_HALF_YEAR -> 180;
            case PLAN_YEARLY -> 365;
            default -> 30;
        };
    }

    public String defaultDescription(String planCode, BigDecimal discountPercent) {
        return defaultDescription(BENEFIT_DISCOUNT, planCode, discountPercent, 0, defaultDurationDays(planCode));
    }

    public String defaultDescription(String benefitType,
                                     String planCode,
                                     BigDecimal discountPercent,
                                     Integer includedBookings,
                                     Integer durationDays) {
        String normalizedBenefitType = normalizeBenefitType(benefitType);
        if (BENEFIT_BOOKING_PACK.equals(normalizedBenefitType)) {
            int credits = Math.max(1, normalizeIncludedBookings(includedBookings));
            return "Includes " + credits + " prepaid booking" + (credits == 1 ? "" : "s") + " for this club.";
        }
        String label = switch (normalizeStandardPlanCode(planCode)) {
            case PLAN_MONTHLY -> "month";
            case PLAN_QUARTERLY -> "quarter";
            case PLAN_HALF_YEAR -> "half-year term";
            case PLAN_YEARLY -> "year";
            default -> {
                int days = Math.max(1, durationDays == null ? 30 : durationDays);
                yield days + "-day term";
            }
        };
        BigDecimal discount = normalizeDiscount(discountPercent);
        if (discount.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return "Book eligible venue slots for free during this " + label + ".";
        }
        if (discount.signum() > 0) {
            return "Save " + discount.stripTrailingZeros().toPlainString() + "% on eligible bookings during this " + label + ".";
        }
        return "Access member pricing and benefits during this " + label + ".";
    }

    @Transactional
    public List<MembershipPlan> ensureStandardPlansForClub(Integer clubId) {
        List<MembershipPlan> existing = membershipPlanRepo.findByClubIdOrderByDurationDaysAsc(clubId);
        Map<String, MembershipPlan> existingByCode = existing.stream()
                .filter(plan -> !normalizeStandardPlanCode(plan.getPlanCode()).isBlank())
                .collect(Collectors.toMap(
                        plan -> normalizeStandardPlanCode(plan.getPlanCode()),
                        plan -> plan,
                        (left, right) -> left
                ));

        List<MembershipPlan> created = new java.util.ArrayList<>(existing);
        for (String code : STANDARD_PLAN_CODES) {
            if (existingByCode.containsKey(code)) continue;
            MembershipPlan plan = new MembershipPlan();
            plan.setClubId(clubId);
            plan.setPlanCode(code);
            plan.setBenefitType(BENEFIT_DISCOUNT);
            plan.setPlanName(defaultPlanName(code));
            plan.setDurationDays(defaultDurationDays(code));
            plan.setPrice(defaultPlanPrice(code));
            plan.setDiscountPercent(defaultPlanDiscount(code));
            plan.setIncludedBookings(0);
            plan.setEnabled(false);
            plan.setDescription(defaultDescription(plan.getBenefitType(), code, plan.getDiscountPercent(), plan.getIncludedBookings(), plan.getDurationDays()));
            created.add(membershipPlanRepo.save(plan));
        }
        return sortPlans(created);
    }

    public BigDecimal defaultPlanPrice(String planCode) {
        return switch (normalizeStandardPlanCode(planCode)) {
            case PLAN_MONTHLY -> BigDecimal.valueOf(49).setScale(2, RoundingMode.HALF_UP);
            case PLAN_QUARTERLY -> BigDecimal.valueOf(129).setScale(2, RoundingMode.HALF_UP);
            case PLAN_HALF_YEAR -> BigDecimal.valueOf(199).setScale(2, RoundingMode.HALF_UP);
            case PLAN_YEARLY -> BigDecimal.valueOf(299).setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        };
    }

    public BigDecimal defaultPlanDiscount(String planCode) {
        return switch (normalizeStandardPlanCode(planCode)) {
            case PLAN_MONTHLY -> BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_UP);
            case PLAN_QUARTERLY -> BigDecimal.valueOf(45).setScale(2, RoundingMode.HALF_UP);
            case PLAN_HALF_YEAR -> BigDecimal.valueOf(70).setScale(2, RoundingMode.HALF_UP);
            case PLAN_YEARLY -> BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_UP);
            default -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        };
    }

    public String defaultCustomPlanName(String benefitType) {
        return BENEFIT_BOOKING_PACK.equals(normalizeBenefitType(benefitType)) ? "Booking Pack" : "Custom Membership";
    }

    public List<MembershipPlan> sortPlans(List<MembershipPlan> plans) {
        if (plans == null || plans.isEmpty()) {
            return List.of();
        }
        return plans.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator
                        .comparingInt(this::planDisplayOrder)
                        .thenComparing(MembershipPlan::getDurationDays, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(MembershipPlan::getCreatedAt, Comparator.nullsLast(java.time.LocalDateTime::compareTo))
                        .thenComparing(MembershipPlan::getPlanId, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    private int planDisplayOrder(MembershipPlan plan) {
        if (plan == null) return Integer.MAX_VALUE;
        return switch (normalizeStandardPlanCode(plan.getPlanCode())) {
            case PLAN_MONTHLY -> 0;
            case PLAN_QUARTERLY -> 1;
            case PLAN_HALF_YEAR -> 2;
            case PLAN_YEARLY -> 3;
            default -> 100;
        };
    }

    private static String safeUpper(String raw) {
        return String.valueOf(raw == null ? "" : raw).trim().toUpperCase();
    }

    public record ActiveMembershipContext(
            UserMembership membership,
            MembershipPlan plan
    ) {
    }
}
