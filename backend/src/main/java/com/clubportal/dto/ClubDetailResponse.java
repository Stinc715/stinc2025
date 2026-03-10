package com.clubportal.dto;

import java.util.List;

public record ClubDetailResponse(
        Integer id,
        Integer clubId,
        String name,
        String description,
        String category,
        String email,
        String phone,
        String location,
        String placeId,
        Double locationLat,
        Double locationLng,
        String openingStart,
        String openingEnd,
        Integer courtsCount,
        List<String> tags
) {
}
