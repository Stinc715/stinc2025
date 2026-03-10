package com.clubportal.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RegistrationEmailVerificationService {

    private final SecureRandom random = new SecureRandom();
    private final Map<String, PendingCode> pendingCodes = new ConcurrentHashMap<>();
    private final Map<String, Instant> verifiedEmails = new ConcurrentHashMap<>();

    @Value("${app.auth.register.code-ttl-seconds:600}")
    private long codeTtlSeconds;

    @Value("${app.auth.register.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.auth.register.verified-ttl-seconds:1800}")
    private long verifiedTtlSeconds;

    public SendCodeResult issueCode(String normalizedEmail) {
        Instant now = Instant.now();
        cleanupExpired(normalizedEmail, now);

        PendingCode existing = pendingCodes.get(normalizedEmail);
        if (existing != null) {
            long elapsed = now.getEpochSecond() - existing.sentAt().getEpochSecond();
            long retryAfter = resendCooldownSeconds - elapsed;
            if (retryAfter > 0) {
                return SendCodeResult.rateLimited(retryAfter);
            }
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        PendingCode next = new PendingCode(code, now, now.plusSeconds(Math.max(60, codeTtlSeconds)));
        pendingCodes.put(normalizedEmail, next);
        verifiedEmails.remove(normalizedEmail);
        return SendCodeResult.sent(code, next.expiresAt(), resendCooldownSeconds);
    }

    public boolean verifyCode(String normalizedEmail, String code) {
        if (code == null || code.isBlank()) return false;

        Instant now = Instant.now();
        cleanupExpired(normalizedEmail, now);

        PendingCode existing = pendingCodes.get(normalizedEmail);
        if (existing == null) return false;
        if (!existing.code().equals(code.trim())) return false;
        if (existing.expiresAt().isBefore(now)) {
            pendingCodes.remove(normalizedEmail);
            return false;
        }

        pendingCodes.remove(normalizedEmail);
        verifiedEmails.put(normalizedEmail, now.plusSeconds(Math.max(60, verifiedTtlSeconds)));
        return true;
    }

    public boolean isVerifiedForRegistration(String normalizedEmail) {
        Instant now = Instant.now();
        cleanupExpired(normalizedEmail, now);
        Instant verifiedUntil = verifiedEmails.get(normalizedEmail);
        return verifiedUntil != null && verifiedUntil.isAfter(now);
    }

    public void consumeVerification(String normalizedEmail) {
        verifiedEmails.remove(normalizedEmail);
        pendingCodes.remove(normalizedEmail);
    }

    public long verificationTtlSeconds() {
        return Math.max(60, verifiedTtlSeconds);
    }

    private void cleanupExpired(String normalizedEmail, Instant now) {
        PendingCode pending = pendingCodes.get(normalizedEmail);
        if (pending != null && !pending.expiresAt().isAfter(now)) {
            pendingCodes.remove(normalizedEmail);
        }
        Instant verifiedUntil = verifiedEmails.get(normalizedEmail);
        if (verifiedUntil != null && !verifiedUntil.isAfter(now)) {
            verifiedEmails.remove(normalizedEmail);
        }
    }

    private record PendingCode(String code, Instant sentAt, Instant expiresAt) {}

    public record SendCodeResult(boolean success, String code, Instant expiresAt, long retryAfterSeconds) {
        public static SendCodeResult sent(String code, Instant expiresAt, long retryAfterSeconds) {
            return new SendCodeResult(true, code, expiresAt, Math.max(0, retryAfterSeconds));
        }

        public static SendCodeResult rateLimited(long retryAfterSeconds) {
            return new SendCodeResult(false, null, null, Math.max(1, retryAfterSeconds));
        }
    }
}
