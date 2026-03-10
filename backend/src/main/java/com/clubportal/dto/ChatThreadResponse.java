package com.clubportal.dto;

import java.util.List;

public record ChatThreadResponse(
        Integer clubId,
        String clubName,
        Integer userId,
        String userName,
        long unreadCount,
        List<ChatMessageResponse> messages
) {
}

