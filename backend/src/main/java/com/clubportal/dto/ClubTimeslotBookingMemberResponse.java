package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ClubTimeslotBookingMemberResponse(
        Integer bookingId,
        Integer userId,
        String name,
        String email,
        String status,
        LocalDateTime bookingTime,
        BigDecimal pricePaid,
        String membershipPlanName,
        String membershipStatus,
        String bookingVerificationCode
) {
}
