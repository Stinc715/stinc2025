package com.clubportal.dto;

public record CheckoutSessionCreateRequest(
        String type,
        Integer timeslotId,
        Integer planId
) {
}
