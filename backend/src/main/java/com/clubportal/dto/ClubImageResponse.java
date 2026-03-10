package com.clubportal.dto;

public record ClubImageResponse(
        Integer imageId,
        String url,
        String originalName,
        Integer sortOrder,
        Boolean primary
) {
}
