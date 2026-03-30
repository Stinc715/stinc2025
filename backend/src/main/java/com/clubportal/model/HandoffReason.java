package com.clubportal.model;

import java.util.Locale;

public enum HandoffReason {
    USER_REQUEST,
    REFUND,
    PAYMENT_ISSUE,
    HARASSMENT,
    POLICY_EXCEPTION,
    OTHER;

    public static HandoffReason fromRaw(String raw) {
        if (raw == null || raw.isBlank()) {
            return OTHER;
        }
        try {
            return HandoffReason.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return OTHER;
        }
    }
}
