package com.clubportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ChatSendResponse(
        Integer clubId,
        String clubName,
        Integer userId,
        String userName,
        long unreadCount,
        List<ChatMessageResponse> messages,
        Integer sessionId,
        String chatMode,
        LocalDateTime handoffRequestedAt,
        String handoffReason,
        int clubUnreadCount,
        boolean fullThread
) {
}
