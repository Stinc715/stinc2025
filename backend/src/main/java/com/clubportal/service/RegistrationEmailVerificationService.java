package com.clubportal.service;

import com.clubportal.model.RegistrationEmailVerification;
import com.clubportal.repository.RegistrationEmailVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;

@Service
public class RegistrationEmailVerificationService {

    private final SecureRandom random = new SecureRandom();
    private final RegistrationEmailVerificationRepository verificationRepo;

    @Value("${app.auth.register.code-ttl-seconds:600}")
    private long codeTtlSeconds;

    @Value("${app.auth.register.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.auth.register.verified-ttl-seconds:1800}")
    private long verifiedTtlSeconds;

    public RegistrationEmailVerificationService(RegistrationEmailVerificationRepository verificationRepo) {
        this.verificationRepo = verificationRepo;
    }

    public SendCodeResult issueCode(String normalizedEmail) {
        String email = normalizeEmail(normalizedEmail);
        if (email.isBlank()) {
            return SendCodeResult.rateLimited(1);
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        RegistrationEmailVerification row = verificationRepo.findByEmailIgnoreCase(email).orElse(null);
        if (row != null && row.getSentAt() != null && row.getExpiresAt() != null && row.getExpiresAt().isAfter(now)) {
            long elapsed = now.getEpochSecond() - row.getSentAt().getEpochSecond();
            long retryAfter = resendCooldownSeconds - elapsed;
            if (retryAfter > 0) {
                return SendCodeResult.rateLimited(retryAfter);
            }
        }

        if (row == null) {
            row = new RegistrationEmailVerification();
            row.setEmail(email);
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = now.plusSeconds(Math.max(60, codeTtlSeconds));
        row.setVerificationCode(code);
        row.setSentAt(now);
        row.setExpiresAt(expiresAt);
        row.setVerifiedUntil(null);
        verificationRepo.save(row);
        return SendCodeResult.sent(code, expiresAt, resendCooldownSeconds);
    }

    public boolean verifyCode(String normalizedEmail, String code) {
        String email = normalizeEmail(normalizedEmail);
        String normalizedCode = safe(code);
        if (email.isBlank() || normalizedCode.isBlank()) return false;

        Instant now = Instant.now();
        cleanupExpired(now);

        RegistrationEmailVerification row = verificationRepo.findByEmailIgnoreCase(email).orElse(null);
        if (row == null) return false;
        if (!normalizedCode.equals(row.getVerificationCode())) return false;
        if (row.getExpiresAt() == null || !row.getExpiresAt().isAfter(now)) {
            verificationRepo.delete(row);
            return false;
        }

        row.setVerificationCode(null);
        row.setExpiresAt(null);
        row.setVerifiedUntil(now.plusSeconds(Math.max(60, verifiedTtlSeconds)));
        verificationRepo.save(row);
        return true;
    }

    public boolean isVerifiedForRegistration(String normalizedEmail) {
        String email = normalizeEmail(normalizedEmail);
        if (email.isBlank()) return false;

        Instant now = Instant.now();
        cleanupExpired(now);

        RegistrationEmailVerification row = verificationRepo.findByEmailIgnoreCase(email).orElse(null);
        return row != null && row.getVerifiedUntil() != null && row.getVerifiedUntil().isAfter(now);
    }

    public void consumeVerification(String normalizedEmail) {
        String email = normalizeEmail(normalizedEmail);
        if (email.isBlank()) return;
        verificationRepo.findByEmailIgnoreCase(email).ifPresent(verificationRepo::delete);
    }

    public long verificationTtlSeconds() {
        return Math.max(60, verifiedTtlSeconds);
    }

    private void cleanupExpired(Instant now) {
        List<RegistrationEmailVerification> expired = verificationRepo.findByExpiresAtBeforeAndVerifiedUntilBefore(now, now);
        if (!expired.isEmpty()) {
            verificationRepo.deleteAll(expired);
        }
        verificationRepo.findAll().stream()
                .filter(row -> shouldDelete(row, now))
                .forEach(verificationRepo::delete);
    }

    private static boolean shouldDelete(RegistrationEmailVerification row, Instant now) {
        boolean codeExpired = row.getVerificationCode() == null
                || row.getExpiresAt() == null
                || !row.getExpiresAt().isAfter(now);
        boolean verifiedExpired = row.getVerifiedUntil() == null
                || !row.getVerifiedUntil().isAfter(now);
        return codeExpired && verifiedExpired;
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeEmail(String value) {
        return safe(value).toLowerCase();
    }

    public record SendCodeResult(boolean success, String code, Instant expiresAt, long retryAfterSeconds) {
        public static SendCodeResult sent(String code, Instant expiresAt, long retryAfterSeconds) {
            return new SendCodeResult(true, code, expiresAt, Math.max(0, retryAfterSeconds));
        }

        public static SendCodeResult rateLimited(long retryAfterSeconds) {
            return new SendCodeResult(false, null, null, Math.max(1, retryAfterSeconds));
        }
    }
}
