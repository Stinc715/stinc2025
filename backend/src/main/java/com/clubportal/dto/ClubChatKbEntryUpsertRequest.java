package com.clubportal.dto;

import java.util.List;

public record ClubChatKbEntryUpsertRequest(
        String questionTitle,
        String answerText,
        List<String> triggerKeywords,
        List<String> exampleQuestions,
        String language,
        Integer priority,
        Boolean enabled
) {
}
