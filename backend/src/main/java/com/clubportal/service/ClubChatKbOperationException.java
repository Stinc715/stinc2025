package com.clubportal.service;

import org.springframework.http.HttpStatus;

public class ClubChatKbOperationException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final Integer clubId;
    private final Integer entryId;

    public ClubChatKbOperationException(HttpStatus status,
                                        String code,
                                        String message,
                                        Integer clubId,
                                        Integer entryId,
                                        Throwable cause) {
        super(message, cause);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
        this.code = code == null ? "CHAT_KB_ERROR" : code;
        this.clubId = clubId;
        this.entryId = entryId;
    }

    public static ClubChatKbOperationException embeddingFailed(Integer clubId,
                                                               Integer entryId,
                                                               String message,
                                                               Throwable cause) {
        return new ClubChatKbOperationException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "CHAT_KB_EMBEDDING_FAILED",
                safe(message, "Failed to generate FAQ embedding"),
                clubId,
                entryId,
                cause
        );
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public Integer getClubId() {
        return clubId;
    }

    public Integer getEntryId() {
        return entryId;
    }

    private static String safe(String value, String fallback) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isBlank() ? fallback : normalized;
    }
}
