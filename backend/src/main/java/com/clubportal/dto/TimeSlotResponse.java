package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TimeSlotResponse(
        Integer timeslotId,
        Integer venueId,
        Integer clubId,
        String venueName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer maxCapacity,
        BigDecimal price,
        long bookedCount,
        long remaining,
        BigDecimal basePrice,
        String membershipPlanName,
        String membershipBenefitType,
        BigDecimal membershipDiscountPercent,
        Integer membershipIncludedBookings,
        Integer membershipRemainingBookings,
        boolean membershipApplied
) {
}
