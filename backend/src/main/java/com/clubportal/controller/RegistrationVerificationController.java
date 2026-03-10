package com.clubportal.controller;

import com.clubportal.repository.UserRepository;
import com.clubportal.service.RegistrationEmailVerificationService;
import com.clubportal.service.VerificationEmailSenderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/auth")
public class RegistrationVerificationController {

    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern CODE_RE = Pattern.compile("^\\d{6}$");

    private final RegistrationEmailVerificationService verificationService;
    private final VerificationEmailSenderService emailSenderService;
    private final UserRepository userRepo;

    public RegistrationVerificationController(RegistrationEmailVerificationService verificationService,
                                              VerificationEmailSenderService emailSenderService,
                                              UserRepository userRepo) {
        this.verificationService = verificationService;
        this.emailSenderService = emailSenderService;
        this.userRepo = userRepo;
    }

    @PostMapping({"/send-code", "/register/email-code/send"})
    public ResponseEntity<?> sendCode(@RequestBody Map<String, Object> body) {
        String email = normalizeEmail(body.get("email"));
        if (email.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        if (!userRepo.findAllByEmailIgnoreCase(email).isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        RegistrationEmailVerificationService.SendCodeResult issued = verificationService.issueCode(email);
        if (!issued.success()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                    "message", "Please wait before requesting another verification code",
                    "retryAfterSeconds", issued.retryAfterSeconds()
            ));
        }

        VerificationEmailSenderService.SendEmailResult sent =
                emailSenderService.sendRegistrationCode(email, issued.code(), issued.expiresAt());
        if (!sent.success()) {
            verificationService.consumeVerification(email);
            if (VerificationEmailSenderService.REASON_RECIPIENT_REJECTED.equals(sent.reasonCode())) {
                return ResponseEntity.badRequest().body(sent.message());
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(sent.message());
        }

        long expiresInSeconds = Math.max(1, Duration.between(Instant.now(), issued.expiresAt()).getSeconds());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "expiresInSeconds", expiresInSeconds
        ));
    }

    @PostMapping({"/verify-code", "/register/email-code/verify"})
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, Object> body) {
        String email = normalizeEmail(body.get("email"));
        String code = safe(body.get("code"));

        if (email.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        if (!CODE_RE.matcher(code).matches()) {
            return ResponseEntity.badRequest().body("Verification code must be 6 digits");
        }

        boolean ok = verificationService.verifyCode(email, code);
        if (!ok) {
            return ResponseEntity.badRequest().body("Invalid or expired verification code");
        }

        return ResponseEntity.ok(Map.of(
                "verified", true,
                "verificationValidForSeconds", verificationService.verificationTtlSeconds()
        ));
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static String normalizeEmail(Object value) {
        return safe(value).toLowerCase();
    }
}
