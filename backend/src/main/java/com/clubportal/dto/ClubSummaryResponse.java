package com.clubportal.dto;

import java.util.List;

public record ClubSummaryResponse(
        Integer id,
        Integer clubId,
        String name,
        String description,
        String category,
        List<String> tags,
        String coverImageUrl
) {
}
