package com.clubportal.dto;

import java.time.LocalDateTime;

public record ClubCommunityAnswerResponse(
        Integer answerId,
        Integer authorUserId,
        String authorName,
        String responderType,
        String answerText,
        boolean canEdit,
        boolean canDelete,
        LocalDateTime createdAt
) {
}
