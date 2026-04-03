package com.clubportal.dto;

public record BookingResponse(
        Integer bookingId,
        Integer timeslotId,
        String status,
        String attendanceState
) {
}
