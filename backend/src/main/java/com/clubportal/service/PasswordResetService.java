package com.clubportal.service;

import com.clubportal.model.PasswordResetToken;
import com.clubportal.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class PasswordResetService {

    private final SecureRandom random = new SecureRandom();
    private final PasswordResetTokenRepository tokenRepo;

    @Value("${app.auth.password-reset.token-ttl-seconds:3600}")
    private long tokenTtlSeconds;

    @Value("${app.auth.password-reset.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    public PasswordResetService(PasswordResetTokenRepository tokenRepo) {
        this.tokenRepo = tokenRepo;
    }

    public IssueResetResult issueReset(String normalizedEmail) {
        String email = normalizeEmail(normalizedEmail);
        if (email.isBlank()) {
            return IssueResetResult.rateLimited(1);
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        PasswordResetToken existing = tokenRepo.findByEmailIgnoreCase(email).orElse(null);
        if (existing != null) {
            long elapsed = now.getEpochSecond() - existing.getSentAt().getEpochSecond();
            long retryAfter = resendCooldownSeconds - elapsed;
            if (retryAfter > 0 && existing.getExpiresAt() != null && existing.getExpiresAt().isAfter(now)) {
                return IssueResetResult.rateLimited(retryAfter);
            }
            tokenRepo.delete(existing);
        }

        String token = generateToken();
        PasswordResetToken row = new PasswordResetToken();
        row.setEmail(email);
        row.setResetToken(token);
        row.setSentAt(now);
        row.setExpiresAt(now.plusSeconds(Math.max(300, tokenTtlSeconds)));
        tokenRepo.save(row);
        return IssueResetResult.sent(token, row.getExpiresAt(), resendCooldownSeconds);
    }

    public TokenValidationResult validateToken(String token) {
        String normalizedToken = safe(token);
        if (normalizedToken.isBlank()) {
            return TokenValidationResult.invalid();
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        PasswordResetToken row = tokenRepo.findByResetToken(normalizedToken).orElse(null);
        if (row == null || row.getExpiresAt() == null || !row.getExpiresAt().isAfter(now)) {
            if (row != null) tokenRepo.delete(row);
            return TokenValidationResult.invalid();
        }

        return TokenValidationResult.valid(row.getEmail(), row.getExpiresAt());
    }

    public ConsumeResetResult consumeToken(String token) {
        String normalizedToken = safe(token);
        if (normalizedToken.isBlank()) {
            return ConsumeResetResult.invalid();
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        PasswordResetToken row = tokenRepo.findByResetToken(normalizedToken).orElse(null);
        if (row == null || row.getExpiresAt() == null || !row.getExpiresAt().isAfter(now)) {
            if (row != null) tokenRepo.delete(row);
            return ConsumeResetResult.invalid();
        }

        tokenRepo.delete(row);
        return ConsumeResetResult.consumed(row.getEmail());
    }

    public void clearForEmail(String normalizedEmail) {
        String email = normalizeEmail(normalizedEmail);
        if (email.isBlank()) return;
        tokenRepo.findByEmailIgnoreCase(email).ifPresent(tokenRepo::delete);
    }

    public long tokenTtlSeconds() {
        return Math.max(300, tokenTtlSeconds);
    }

    private void cleanupExpired(Instant now) {
        List<PasswordResetToken> expired = new ArrayList<>(tokenRepo.findByExpiresAtBefore(now));
        if (!expired.isEmpty()) {
            tokenRepo.deleteAll(expired);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeEmail(String value) {
        return safe(value).toLowerCase();
    }

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
