package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MyBookingResponse(
        Integer bookingId,
        String orderNo,
        Integer timeslotId,
        String status,
        LocalDateTime bookingTime,
        Integer clubId,
        String clubName,
        Integer venueId,
        String venueName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer maxCapacity,
        BigDecimal price,
        BigDecimal basePrice,
        String membershipPlanName,
        BigDecimal membershipDiscountPercent,
        boolean membershipApplied,
        String bookingVerificationCode
) {
}
