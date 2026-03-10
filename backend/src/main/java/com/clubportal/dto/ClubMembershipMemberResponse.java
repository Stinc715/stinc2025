package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClubMembershipMemberResponse(
        Integer userMembershipId,
        Integer userId,
        String memberName,
        String email,
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
