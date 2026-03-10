package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MembershipPurchaseResponse(
        Integer userMembershipId,
        Integer planId,
        Integer clubId,
        String clubName,
        String planCode,
        String planName,
        BigDecimal amount,
        BigDecimal discountPercent,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
}
