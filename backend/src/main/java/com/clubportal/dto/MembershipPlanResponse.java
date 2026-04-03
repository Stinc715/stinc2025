package com.clubportal.dto;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Integer planId,
        Integer clubId,
        String planCode,
        String benefitType,
        String planName,
        BigDecimal price,
        Integer durationDays,
        BigDecimal discountPercent,
        Integer includedBookings,
        Boolean enabled,
        Boolean standardPlan,
        String description
) {
}
