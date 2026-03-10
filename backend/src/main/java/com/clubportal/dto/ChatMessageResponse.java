package com.clubportal.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Integer messageId,
        Integer clubId,
        Integer userId,
        String sender,
        String text,
        String authorName,
        boolean readByClub,
        boolean readByUser,
        LocalDateTime createdAt
) {
}

