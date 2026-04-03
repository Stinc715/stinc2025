package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ClubChatContextDto(
        ClubInfo club,
        List<VisibleTimeslot> visibleTimeslots,
        List<MembershipPlanInfo> membershipPlans,
        ViewerInfo viewer,
        PolicyInfo policy
) {
    public ClubChatContextDto {
        club = club == null ? new ClubInfo(null, "", "", "", List.of(), "", "", "", "", "") : club;
        visibleTimeslots = visibleTimeslots == null ? List.of() : List.copyOf(visibleTimeslots);
        membershipPlans = membershipPlans == null ? List.of() : List.copyOf(membershipPlans);
        viewer = viewer == null ? new ViewerInfo(false, "", null, "", null) : viewer;
        policy = policy == null ? new PolicyInfo("GBP", "UTC", 500) : policy;
    }

    public record ClubInfo(
            Integer clubId,
            String name,
            String description,
            String category,
            List<String> tags,
            String location,
            String openingStart,
            String openingEnd,
            String email,
            String phone
    ) {
        public ClubInfo {
            tags = tags == null ? List.of() : List.copyOf(tags);
            name = safe(name);
            description = safe(description);
            category = safe(category);
            location = safe(location);
            openingStart = safe(openingStart);
            openingEnd = safe(openingEnd);
            email = safe(email);
            phone = safe(phone);
        }
    }

    public record VisibleTimeslot(
            Integer timeslotId,
            Integer venueId,
            String venueName,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Integer maxCapacity,
            long bookedCount,
            long remaining,
            BigDecimal price,
            BigDecimal basePrice,
            String membershipPlanName,
            String membershipBenefitType,
            BigDecimal membershipDiscountPercent,
            Integer membershipIncludedBookings,
            Integer membershipRemainingBookings,
            boolean membershipApplied
    ) {
        public VisibleTimeslot {
            venueName = safe(venueName);
            membershipPlanName = safe(membershipPlanName);
            membershipBenefitType = safe(membershipBenefitType);
        }
    }

    public record MembershipPlanInfo(
            Integer planId,
            String planCode,
            String benefitType,
            String planName,
            BigDecimal price,
            Integer durationDays,
            BigDecimal discountPercent,
            Integer includedBookings,
            boolean enabled,
            String description
    ) {
        public MembershipPlanInfo {
            planCode = safe(planCode);
            benefitType = safe(benefitType);
            planName = safe(planName);
            description = safe(description);
        }
    }

    public record ViewerInfo(
            boolean loggedIn,
            String role,
            Integer userId,
            String name,
            ActiveMembershipInfo activeMembership
    ) {
        public ViewerInfo {
            role = safe(role);
            name = safe(name);
        }

        public boolean isUserAccount() {
            return loggedIn && "user".equalsIgnoreCase(role);
        }
    }

    public record ActiveMembershipInfo(
            Integer userMembershipId,
            Integer clubId,
            String clubName,
            Integer planId,
            String planCode,
            String benefitType,
            String planName,
            BigDecimal planPrice,
            BigDecimal discountPercent,
            Integer includedBookings,
            Integer remainingBookings,
            LocalDate startDate,
            LocalDate endDate,
            String status
    ) {
        public ActiveMembershipInfo {
            clubName = safe(clubName);
            planCode = safe(planCode);
            benefitType = safe(benefitType);
            planName = safe(planName);
            status = safe(status);
        }
    }

    public record PolicyInfo(
            String currency,
            String timezone,
            int messageMaxChars
    ) {
        public PolicyInfo {
            currency = safe(currency);
            timezone = safe(timezone);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
