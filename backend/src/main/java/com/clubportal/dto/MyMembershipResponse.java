package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MyMembershipResponse(
        Integer userMembershipId,
        String orderNo,
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
        String status,
        LocalDateTime createdAt
) {
}
