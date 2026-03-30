package com.clubportal.model;

import java.util.Locale;

public enum MessageSenderType {
    USER,
    CLUB,
    ASSISTANT,
    SYSTEM;

    public static MessageSenderType fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            return USER;
        }
        try {
            return MessageSenderType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return USER;
        }
    }
}
