package com.clubportal.dto;

public record VenueResponse(
        Integer venueId,
        Integer clubId,
        String name,
        String location,
        Integer capacity
) {
}

