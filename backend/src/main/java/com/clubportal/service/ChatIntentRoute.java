package com.clubportal.service;

import com.clubportal.dto.ClubChatContextDto;

public record ChatIntentRoute(
        ChatIntentType intentType,
        ClubChatContextDto.VisibleTimeslot matchedTimeslot
) {
}
