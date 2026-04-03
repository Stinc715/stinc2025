package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.PasswordResetService;
import com.clubportal.service.VerificationEmailSenderService;
import com.clubportal.util.PasswordEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth/password-reset")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String GENERIC_RESET_REQUEST_MESSAGE =
            "If an account exists for this email, a password reset link will be sent shortly.";

    private final PasswordResetService passwordResetService;
    private final VerificationEmailSenderService emailSenderService;
    private final UserRepository userRepo;
    private final PasswordEncryptionUtil passwordUtil;
    private final PasswordPolicyService passwordPolicyService;

    @Value("${app.public.base-url:}")
    private String publicBaseUrl;

    public PasswordResetController(PasswordResetService passwordResetService,
                                   VerificationEmailSenderService emailSenderService,
                                   UserRepository userRepo,
                                   PasswordEncryptionUtil passwordUtil,
                                   PasswordPolicyService passwordPolicyService) {
        this.passwordResetService = passwordResetService;
        this.emailSenderService = emailSenderService;
        this.userRepo = userRepo;
        this.passwordUtil = passwordUtil;
        this.passwordPolicyService = passwordPolicyService;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, Object> body) {
        String email = normalizeEmail(body.get("email"));
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        User user = userRepo.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            log.info("Password reset requested for non-existent email={}", email);
            return genericResetRequestAccepted();
        }

        PasswordResetService.IssueResetResult issued = passwordResetService.issueReset(email);
        if (!issued.success()) {
            log.info("Password reset request rate-limited for email={} retryAfterSeconds={}",
                    email, issued.retryAfterSeconds());
            return genericResetRequestAccepted();
        }

        String resetLink;
        try {
            resetLink = buildResetLink(issued.token());
        } catch (Exception ex) {
            passwordResetService.clearForEmail(email);
            log.error("Password reset link generation failed for email={}", email, ex);
            return genericResetRequestAccepted();
        }
        VerificationEmailSenderService.SendEmailResult sent =
                emailSenderService.sendPasswordResetLink(email, resetLink, issued.expiresAt());
        if (!sent.success()) {
            passwordResetService.clearForEmail(email);
            log.warn("Password reset email dispatch failed for email={} reasonCode={} message={}",
                    email, safe(sent.reasonCode()), safe(sent.message()));
            return genericResetRequestAccepted();
        }

        long expiresInSeconds = Math.max(1, Duration.between(Instant.now(), issued.expiresAt()).getSeconds());
        log.info("Issued password reset for email={} resetLinkBase={} expiresInSeconds={}",
                email, safe(resolveTrustedPublicBaseUrl()), expiresInSeconds);
        return genericResetRequestAccepted();
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestParam("token") String token) {
        PasswordResetService.TokenValidationResult result = passwordResetService.validateToken(token);
        if (!result.valid()) {
            return ResponseEntity.badRequest().body("Invalid or expired password reset link");
        }

        long expiresInSeconds = Math.max(1, Duration.between(Instant.now(), result.expiresAt()).getSeconds());
        return ResponseEntity.ok(Map.of(
                "valid", true,
                "expiresInSeconds", expiresInSeconds
        ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPasswordReset(@RequestBody Map<String, Object> body) {
        String token = safe(body.get("token"));
        String password = raw(body.get("password"));

        if (token.isBlank()) {
            return ResponseEntity.badRequest().body("Missing token");
        }
        if (password.isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        String passwordValidationMessage = passwordPolicyService.validate(password).orElse(null);
        if (passwordValidationMessage != null) {
            return ResponseEntity.badRequest().body(passwordValidationMessage);
        }

        PasswordResetService.ConsumeResetResult result = passwordResetService.consumeToken(token);
        if (!result.success()) {
            return ResponseEntity.badRequest().body("Invalid or expired password reset link");
        }

        User user = userRepo.findByEmailIgnoreCase(result.email()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Account not found");
        }

        user.setPasswordHash(passwordUtil.encodePassword(password));
        user.bumpSessionVersion();
        userRepo.save(user);
        log.info("Password reset completed for email={} userId={}", user.getEmail(), user.getUserId());

        return ResponseEntity.ok(Map.of(
                "reset", true
        ));
    }

    private String buildResetLink(String token) {
        String base = resolveTrustedPublicBaseUrl();
        return base + "/reset-password.html?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String resolveTrustedPublicBaseUrl() {
        String configured = safe(publicBaseUrl);
        if (isPublicHttpUrl(configured)) {
            return trimTrailingSlash(configured);
        }
        throw new IllegalStateException("APP_PUBLIC_BASE_URL is not configured with a valid http(s) URL");
    }

    private static boolean isPublicHttpUrl(String value) {
        String text = safe(value);
        return text.startsWith("http://") || text.startsWith("https://");
    }

    private static String trimTrailingSlash(String value) {
        String text = safe(value);
        while (text.endsWith("/")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String raw(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String normalizeEmail(Object value) {
        return safe(value).toLowerCase();
    }

    private ResponseEntity<Map<String, Object>> genericResetRequestAccepted() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", GENERIC_RESET_REQUEST_MESSAGE
        ));
    }
}
