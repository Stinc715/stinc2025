package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyMembershipResponse(
        Integer userMembershipId,
        Integer clubId,
        String clubName,
        Integer planId,
        String planCode,
        String planName,
        BigDecimal planPrice,
        BigDecimal discountPercent,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        LocalDateTime createdAt
) {
}
