package com.clubportal.dto;

import java.time.LocalDateTime;

public record ChatSessionResponse(
        Integer sessionId,
        String chatMode,
        LocalDateTime handoffRequestedAt,
        String handoffReason,
        int clubUnreadCount
) {
}
