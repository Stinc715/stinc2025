package com.clubportal.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ClubTimeslotBookingResponse(
        Integer timeslotId,
        Integer venueId,
        String venueName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer maxCapacity,
        BigDecimal price,
        long bookedCount,
        List<ClubTimeslotBookingMemberResponse> members
) {
}

