package com.clubportal.controller;

import com.clubportal.dto.ClubChatKbErrorResponse;
import com.clubportal.service.ClubChatKbOperationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@RestControllerAdvice(assignableTypes = {
        ClubChatKbController.class,
        ClubChatKbAdminController.class
})
public class ClubChatKbExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ClubChatKbExceptionHandler.class);

    @ExceptionHandler(ClubChatKbOperationException.class)
    public ResponseEntity<ClubChatKbErrorResponse> handleClubChatKbOperationException(ClubChatKbOperationException ex,
                                                                                      HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        ClubChatKbErrorResponse body = new ClubChatKbErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getCode(),
                safe(ex.getMessage(), "Chat KB operation failed"),
                path(request),
                ex.getClubId(),
                ex.getEntryId()
        );
        log.warn("[CLUB_CHAT_DEBUG] KB_SAVE response_error: path={}, clubId={}, entryId={}, status={}, code={}, message={}",
                body.path(),
                body.clubId(),
                body.entryId(),
                body.status(),
                body.code(),
                body.message());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ClubChatKbErrorResponse> handleResponseStatusException(ResponseStatusException ex,
                                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        ClubChatKbErrorResponse body = new ClubChatKbErrorResponse(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                "CHAT_KB_REQUEST_FAILED",
                safe(ex.getReason(), "Chat KB request failed"),
                path(request),
                null,
                null
        );
        log.warn("[CLUB_CHAT_DEBUG] KB_SAVE response_error: path={}, clubId=null, entryId=null, status={}, code={}, message={}",
                body.path(),
                body.status(),
                body.code(),
                body.message());
        return ResponseEntity.status(status).body(body);
    }

    private static String safe(String value, String fallback) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isBlank() ? fallback : normalized;
    }

    private static String path(HttpServletRequest request) {
        return request == null ? "" : safe(request.getRequestURI(), "");
    }
}
