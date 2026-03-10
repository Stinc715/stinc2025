package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.PasswordResetService;
import com.clubportal.service.VerificationEmailSenderService;
import com.clubportal.util.PasswordEncryptionUtil;
import jakarta.servlet.http.HttpServletRequest;
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

import java.net.URI;
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
    private static final Pattern PASSWORD_RE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");

    private final PasswordResetService passwordResetService;
    private final VerificationEmailSenderService emailSenderService;
    private final UserRepository userRepo;
    private final PasswordEncryptionUtil passwordUtil;

    @Value("${app.public.base-url:https://www.club-portal.xyz}")
    private String publicBaseUrl;

    public PasswordResetController(PasswordResetService passwordResetService,
                                   VerificationEmailSenderService emailSenderService,
                                   UserRepository userRepo,
                                   PasswordEncryptionUtil passwordUtil) {
        this.passwordResetService = passwordResetService;
        this.emailSenderService = emailSenderService;
        this.userRepo = userRepo;
        this.passwordUtil = passwordUtil;
    }

    @PostMapping("/request")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, Object> body,
                                                  HttpServletRequest request) {
        String email = normalizeEmail(body.get("email"));
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        User user = userRepo.findByEmailIgnoreCase(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No account found for this email");
        }

        PasswordResetService.IssueResetResult issued = passwordResetService.issueReset(email);
        if (!issued.success()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", "Please wait before requesting another reset email",
                    "retryAfterSeconds", issued.retryAfterSeconds()
            ));
        }

        String resetLink = buildResetLink(request, issued.token());
        VerificationEmailSenderService.SendEmailResult sent =
                emailSenderService.sendPasswordResetLink(email, resetLink, issued.expiresAt());
        if (!sent.success()) {
            passwordResetService.clearForEmail(email);
            if (VerificationEmailSenderService.REASON_RECIPIENT_REJECTED.equals(sent.reasonCode())) {
                return ResponseEntity.badRequest().body(sent.message());
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(sent.message());
        }

        long expiresInSeconds = Math.max(1, Duration.between(Instant.now(), issued.expiresAt()).getSeconds());
        log.info("Issued password reset for email={} resetLinkBase={}", email, safe(resolvePublicBaseUrl(request)));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "expiresInSeconds", expiresInSeconds
        ));
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
        String password = safe(body.get("password"));

        if (token.isBlank()) {
            return ResponseEntity.badRequest().body("Missing token");
        }
        if (password.isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (!PASSWORD_RE.matcher(password).matches()) {
            return ResponseEntity.badRequest().body("Password must include uppercase, lowercase, and a number");
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

    private String buildResetLink(HttpServletRequest request, String token) {
        String base = resolvePublicBaseUrl(request);
        return base + "/reset-password.html?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    private String resolvePublicBaseUrl(HttpServletRequest request) {
        String origin = safe(request == null ? null : request.getHeader("Origin"));
        if (isPublicHttpUrl(origin)) {
            return trimTrailingSlash(origin);
        }

        String referer = safe(request == null ? null : request.getHeader("Referer"));
        if (!referer.isBlank()) {
            try {
                URI uri = URI.create(referer);
                if (uri.getScheme() != null && uri.getAuthority() != null) {
                    String candidate = uri.getScheme() + "://" + uri.getAuthority();
                    if (isPublicHttpUrl(candidate)) {
                        return trimTrailingSlash(candidate);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        String configured = safe(publicBaseUrl);
        if (isPublicHttpUrl(configured)) {
            return trimTrailingSlash(configured);
        }

        String scheme = safe(request == null ? null : request.getScheme());
        String serverName = safe(request == null ? null : request.getServerName());
        int port = request == null ? -1 : request.getServerPort();
        if (!scheme.isBlank() && !serverName.isBlank()) {
            boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443)
                    || port <= 0;
            String suffix = defaultPort ? "" : ":" + port;
            return trimTrailingSlash(scheme + "://" + serverName + suffix);
        }

        return "https://www.club-portal.xyz";
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

    private static String normalizeEmail(Object value) {
        return safe(value).toLowerCase();
    }
}
