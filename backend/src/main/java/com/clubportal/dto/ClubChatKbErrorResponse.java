package com.clubportal.dto;

import java.time.OffsetDateTime;

public record ClubChatKbErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Integer clubId,
        Integer entryId
) {
}
