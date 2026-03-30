package com.clubportal.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Integer messageId,
        Integer clubId,
        Integer userId,
        String sender,
        String text,
        String authorName,
        String answerSource,
        Integer matchedFaqId,
        boolean handoffSuggested,
        boolean readByClub,
        boolean readByUser,
        LocalDateTime createdAt
) {
}
