package com.clubportal.dto;

import java.time.LocalDateTime;

public record ChatConversationSummaryResponse(
        Integer clubId,
        Integer userId,
        String userName,
        String userEmail,
        Integer lastMessageId,
        String lastSender,
        String lastMessageText,
        LocalDateTime lastMessageAt,
        long unreadCount,
        long totalMessages
) {
}

