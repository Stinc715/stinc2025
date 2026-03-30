package com.clubportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClubChatKbEntryResponse(
        Integer id,
        Integer clubId,
        String questionTitle,
        String answerText,
        List<String> triggerKeywords,
        List<String> exampleQuestions,
        String language,
        Integer priority,
        boolean enabled,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
