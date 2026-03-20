package com.clubportal.service;

import com.clubportal.model.ProfileEmailChangeVerification;
import com.clubportal.repository.ProfileEmailChangeVerificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProfileEmailVerificationService {

    private final SecureRandom random = new SecureRandom();
    private final ProfileEmailChangeVerificationRepository verificationRepo;

    @Value("${app.auth.register.code-ttl-seconds:600}")
    private long codeTtlSeconds;

    @Value("${app.auth.register.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    public ProfileEmailVerificationService(ProfileEmailChangeVerificationRepository verificationRepo) {
        this.verificationRepo = verificationRepo;
    }

    public SendCodeResult issueCode(Integer userId, String normalizedEmail) {
        if (userId == null || userId <= 0) {
            return SendCodeResult.conflict("Invalid user");
        }

        String email = normalizeEmail(normalizedEmail);
        if (email.isBlank()) {
            return SendCodeResult.conflict("Missing email");
        }

        Instant now = Instant.now();
        cleanupExpired(now);

        ProfileEmailChangeVerification byPendingEmail = verificationRepo.findByPendingEmailIgnoreCase(email).orElse(null);
        if (byPendingEmail != null && !userId.equals(byPendingEmail.getUserId())) {
            return SendCodeResult.conflict("Another account is already verifying this email");
        }

        ProfileEmailChangeVerification row = verificationRepo.findByUserId(userId).orElse(null);
        if (row != null
                && email.equalsIgnoreCase(row.getPendingEmail())
                && row.getExpiresAt() != null
                && row.getExpiresAt().isAfter(now)
                && row.getSentAt() != null) {
            long elapsed = now.getEpochSecond() - row.getSentAt().getEpochSecond();
            long retryAfter = resendCooldownSeconds - elapsed;
            if (retryAfter > 0) {
                return SendCodeResult.rateLimited(retryAfter);
            }
        }

        if (row == null) {
            row = new ProfileEmailChangeVerification();
            row.setUserId(userId);
        }

        String code = String.format("%06d", random.nextInt(1_000_000));
        Instant expiresAt = now.plusSeconds(Math.max(60, codeTtlSeconds));
        row.setPendingEmail(email);
        row.setVerificationCode(code);
        row.setSentAt(now);
        row.setExpiresAt(expiresAt);
        verificationRepo.save(row);
        return SendCodeResult.sent(code, expiresAt, resendCooldownSeconds);
    }

    public boolean verifyCode(Integer userId, String normalizedEmail, String code) {
        if (userId == null || userId <= 0) return false;
        String email = normalizeEmail(normalizedEmail);
        String normalizedCode = safe(code);
        if (email.isBlank() || normalizedCode.isBlank()) return false;

        Instant now = Instant.now();
        cleanupExpired(now);

        ProfileEmailChangeVerification row = verificationRepo.findByUserIdAndPendingEmailIgnoreCase(userId, email).orElse(null);
        if (row == null) return false;
        if (!normalizedCode.equals(row.getVerificationCode())) return false;
        if (row.getExpiresAt() == null || !row.getExpiresAt().isAfter(now)) {
            verificationRepo.delete(row);
            return false;
        }

        verificationRepo.delete(row);
        return true;
    }

    public void clearForUser(Integer userId) {
        if (userId == null || userId <= 0) return;
        verificationRepo.findByUserId(userId).ifPresent(verificationRepo::delete);
    }

    private void cleanupExpired(Instant now) {
        List<ProfileEmailChangeVerification> expired = new ArrayList<>(verificationRepo.findByExpiresAtBefore(now));
        if (!expired.isEmpty()) {
            verificationRepo.deleteAll(expired);
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private static String normalizeEmail(String value) {
        return safe(value).toLowerCase();
    }

    public record SendCodeResult(boolean success, String code, Instant expiresAt, long retryAfterSeconds, String message) {
        public static SendCodeResult sent(String code, Instant expiresAt, long retryAfterSeconds) {
            return new SendCodeResult(true, code, expiresAt, Math.max(0, retryAfterSeconds), null);
        }

        public static SendCodeResult rateLimited(long retryAfterSeconds) {
            return new SendCodeResult(false, null, null, Math.max(1, retryAfterSeconds), null);
        }

        public static SendCodeResult conflict(String message) {
            return new SendCodeResult(false, null, null, 0, message);
        }
    }
}
