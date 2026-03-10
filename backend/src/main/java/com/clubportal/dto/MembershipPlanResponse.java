package com.clubportal.dto;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Integer planId,
        Integer clubId,
        String planCode,
        String planName,
        BigDecimal price,
        Integer durationDays,
        BigDecimal discountPercent,
        Boolean enabled,
        String description
) {
}
