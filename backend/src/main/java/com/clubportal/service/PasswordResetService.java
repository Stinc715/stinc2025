package com.clubportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingReset> pendingByEmail = new ConcurrentHashMap<>();
    private final Map<String, PendingReset> pendingByToken = new ConcurrentHashMap<>();

    @Value("${app.auth.password-reset.token-ttl-seconds:3600}")
    private long tokenTtlSeconds;

    @Value("${app.auth.password-reset.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    public IssueResetResult issueReset(String normalizedEmail) {
        Instant now = Instant.now();
        cleanupExpired(now);

        PendingReset existing = pendingByEmail.get(normalizedEmail);
        if (existing != null) {
            long elapsed = now.getEpochSecond() - existing.sentAt().getEpochSecond();
            long retryAfter = resendCooldownSeconds - elapsed;
            if (retryAfter > 0) {
                return IssueResetResult.rateLimited(retryAfter);
            }
            removePending(existing);
        }

        String token = generateToken();
        PendingReset next = new PendingReset(
                normalizedEmail,
                token,
                now,
                now.plusSeconds(Math.max(300, tokenTtlSeconds))
        );
        pendingByEmail.put(normalizedEmail, next);
        pendingByToken.put(token, next);
        return IssueResetResult.sent(token, next.expiresAt(), resendCooldownSeconds);
    }

    public TokenValidationResult validateToken(String token) {
        String normalizedToken = safe(token);
        if (normalizedToken.isBlank()) {
            return TokenValidationResult.invalid();
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        PendingReset pending = pendingByToken.get(normalizedToken);
        if (pending == null || !pending.expiresAt().isAfter(now)) {
            if (pending != null) removePending(pending);
            return TokenValidationResult.invalid();
        }

        return TokenValidationResult.valid(pending.email(), pending.expiresAt());
    }

    public ConsumeResetResult consumeToken(String token) {
        String normalizedToken = safe(token);
        if (normalizedToken.isBlank()) {
            return ConsumeResetResult.invalid();
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        PendingReset pending = pendingByToken.get(normalizedToken);
        if (pending == null || !pending.expiresAt().isAfter(now)) {
            if (pending != null) removePending(pending);
            return ConsumeResetResult.invalid();
        }

        removePending(pending);
        return ConsumeResetResult.consumed(pending.email());
    }

    public void clearForEmail(String normalizedEmail) {
        PendingReset pending = pendingByEmail.remove(safe(normalizedEmail).toLowerCase());
        if (pending != null) {
            pendingByToken.remove(pending.token(), pending);
        }
    }

    public long tokenTtlSeconds() {
        return Math.max(300, tokenTtlSeconds);
    }

    private void cleanupExpired(Instant now) {
        for (PendingReset pending : new ArrayList<>(pendingByToken.values())) {
            if (!pending.expiresAt().isAfter(now)) {
                removePending(pending);
            }
        }
    }

    private void removePending(PendingReset pending) {
        if (pending == null) return;
        pendingByEmail.remove(pending.email(), pending);
        pendingByToken.remove(pending.token(), pending);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record PendingReset(String email, String token, Instant sentAt, Instant expiresAt) {}

    public record IssueResetResult(boolean success, String token, Instant expiresAt, long retryAfterSeconds) {
        public static IssueResetResult sent(String token, Instant expiresAt, long retryAfterSeconds) {
            return new IssueResetResult(true, token, expiresAt, Math.max(0, retryAfterSeconds));
        }

        public static IssueResetResult rateLimited(long retryAfterSeconds) {
            return new IssueResetResult(false, null, null, Math.max(1, retryAfterSeconds));
        }
    }

    public record TokenValidationResult(boolean valid, String email, Instant expiresAt) {
        public static TokenValidationResult valid(String email, Instant expiresAt) {
            return new TokenValidationResult(true, email, expiresAt);
        }

        public static TokenValidationResult invalid() {
            return new TokenValidationResult(false, null, null);
        }
    }

    public record ConsumeResetResult(boolean success, String email) {
        public static ConsumeResetResult consumed(String email) {
            return new ConsumeResetResult(true, email);
        }

        public static ConsumeResetResult invalid() {
            return new ConsumeResetResult(false, null);
        }
    }
}
