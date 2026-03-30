package com.clubportal.service;

public enum ClubChatKbMatchRejectReason {
    NONE,
    NO_FAQ,
    LOW_SIMILARITY,
    AMBIGUOUS_MATCH,
    EMBEDDING_FAILED,
    INVALID_USER_QUESTION,
    NO_VALID_EMBEDDINGS
}
