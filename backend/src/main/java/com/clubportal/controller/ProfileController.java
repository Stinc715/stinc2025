package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.security.JwtUtil;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.ProfileEmailVerificationService;
import com.clubportal.service.UserAvatarService;
import com.clubportal.service.VerificationEmailSenderService;
import com.clubportal.util.PasswordEncryptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_RE = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$");

    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncryptionUtil passwordUtil;
    private final ProfileEmailVerificationService profileEmailVerificationService;
    private final VerificationEmailSenderService emailSenderService;
    private final UserAvatarService userAvatarService;
    private final StreamAuthCookieService streamAuthCookieService;

    public ProfileController(CurrentUserService currentUserService,
                             UserRepository userRepo,
                             JwtUtil jwtUtil,
                             PasswordEncryptionUtil passwordUtil,
                             ProfileEmailVerificationService profileEmailVerificationService,
                             VerificationEmailSenderService emailSenderService,
                             UserAvatarService userAvatarService,
                             StreamAuthCookieService streamAuthCookieService) {
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordUtil = passwordUtil;
        this.profileEmailVerificationService = profileEmailVerificationService;
        this.emailSenderService = emailSenderService;
        this.userAvatarService = userAvatarService;
        this.streamAuthCookieService = streamAuthCookieService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request,
                                        HttpServletResponse response) {
        User me = currentUserService.requireUser();
        String token = jwtUtil.generateToken(me.getEmail(), roleValue(me), me.getSessionVersionOrDefault());
        streamAuthCookieService.writeStreamToken(request, response, token);
        return ResponseEntity.ok(toProfilePayload(me, null));
    }

    @PatchMapping
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, Object> body,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        User me = currentUserService.requireUser();

        String nextName = safe(body.get("displayName"));
        if (nextName.isBlank()) nextName = safe(body.get("fullName"));
        if (nextName.isBlank()) nextName = safe(body.get("name"));
        if (nextName.isBlank()) {
            return ResponseEntity.badRequest().body("Display name is required");
        }
        if (nextName.length() > 120) {
            return ResponseEntity.badRequest().body("Display name must be 120 characters or fewer");
        }

        me.setUsername(nextName);
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), saved.getSessionVersionOrDefault());
        streamAuthCookieService.writeStreamToken(request, response, token);
        return ResponseEntity.ok(toProfilePayload(saved, null));
    }

    @PostMapping("/email/code")
    public ResponseEntity<?> sendEmailCode(@RequestBody Map<String, Object> body) {
        User me = currentUserService.requireUser();

        String nextEmail = normalizeEmail(body.get("email"));
        if (nextEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(nextEmail).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        if (nextEmail.equalsIgnoreCase(safe(me.getEmail()))) {
            return ResponseEntity.badRequest().body("New email must be different from current email");
        }

        boolean hasOther = userRepo.findAllByEmailIgnoreCase(nextEmail).stream()
                .anyMatch(u -> !u.getUserId().equals(me.getUserId()));
        if (hasOther) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        ProfileEmailVerificationService.SendCodeResult issued =
                profileEmailVerificationService.issueCode(me.getUserId(), nextEmail);
        if (!issued.success()) {
            if (issued.retryAfterSeconds() > 0) {
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of(
                        "message", "Please wait before requesting another verification code",
                        "retryAfterSeconds", issued.retryAfterSeconds()
                ));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT).body(issued.message() == null
                    ? "Email verification is already pending for this address"
                    : issued.message());
        }

        VerificationEmailSenderService.SendEmailResult sent =
                emailSenderService.sendRegistrationCode(nextEmail, issued.code(), issued.expiresAt());
        if (!sent.success()) {
            profileEmailVerificationService.clearForUser(me.getUserId());
            if (VerificationEmailSenderService.REASON_RECIPIENT_REJECTED.equals(sent.reasonCode())) {
                return ResponseEntity.badRequest().body(sent.message());
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(sent.message());
        }

        long expiresInSeconds = Math.max(1, Duration.between(Instant.now(), issued.expiresAt()).getSeconds());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "expiresInSeconds", expiresInSeconds
        ));
    }

    @PostMapping("/email")
    public ResponseEntity<?> verifyAndUpdateEmail(@RequestBody Map<String, Object> body,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) {
        User me = currentUserService.requireUser();

        String nextEmail = normalizeEmail(body.get("email"));
        String code = safe(body.get("code"));

        if (nextEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(nextEmail).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }
        if (!code.matches("^\\d{6}$")) {
            return ResponseEntity.badRequest().body("Verification code must be 6 digits");
        }

        boolean hasOther = userRepo.findAllByEmailIgnoreCase(nextEmail).stream()
                .anyMatch(u -> !u.getUserId().equals(me.getUserId()));
        if (hasOther) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }
        if (!profileEmailVerificationService.verifyCode(me.getUserId(), nextEmail, code)) {
            return ResponseEntity.badRequest().body("Invalid or expired verification code");
        }

        me.setEmail(nextEmail);
        int nextSessionVersion = me.bumpSessionVersion();
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion);
        streamAuthCookieService.writeStreamToken(request, response, token);
        return ResponseEntity.ok(toProfilePayload(saved, token));
    }

    @PatchMapping("/email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, Object> body,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("User accounts must verify a new email code before updating email");
        }

        String nextEmail = normalizeEmail(body.get("email"));
        if (nextEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(nextEmail).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        if (nextEmail.equalsIgnoreCase(safe(me.getEmail()))) {
            String token = jwtUtil.generateToken(me.getEmail(), roleValue(me), me.getSessionVersionOrDefault());
            streamAuthCookieService.writeStreamToken(request, response, token);
            return ResponseEntity.ok(toProfilePayload(me, token));
        }

        boolean hasOther = userRepo.findAllByEmailIgnoreCase(nextEmail).stream()
                .anyMatch(u -> !u.getUserId().equals(me.getUserId()));
        if (hasOther) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        me.setEmail(nextEmail);
        int nextSessionVersion = me.bumpSessionVersion();
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion);
        profileEmailVerificationService.clearForUser(me.getUserId());
        streamAuthCookieService.writeStreamToken(request, response, token);
        return ResponseEntity.ok(toProfilePayload(saved, token));
    }

    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, Object> body,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        User me = currentUserService.requireUser();

        String currentPassword = safe(body.get("currentPassword"));
        String newPassword = safe(body.get("newPassword"));

        if (currentPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Current password is required");
        }
        if (newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("New password is required");
        }
        if (!PASSWORD_RE.matcher(newPassword).matches()) {
            return ResponseEntity.badRequest().body("Password must include uppercase, lowercase, and a number");
        }
        if (!safePasswordMatch(currentPassword, safe(me.getPasswordHash()))) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        me.setPasswordHash(passwordUtil.encodePassword(newPassword));
        int nextSessionVersion = me.bumpSessionVersion();
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion);
        streamAuthCookieService.writeStreamToken(request, response, token);
        return ResponseEntity.ok(Map.of(
                "updated", true,
                "token", token
        ));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile avatar) {
        User me = currentUserService.requireUser();
        UserAvatarService.AvatarUploadResult uploaded = userAvatarService.storeAvatar(me, avatar);
        if (!uploaded.success()) {
            return ResponseEntity.badRequest().body(uploaded.message());
        }
        return ResponseEntity.ok(Map.of(
                "avatarUrl", uploaded.avatarUrl(),
                "avatar", uploaded.avatarUrl()
        ));
    }

    private Map<String, Object> toProfilePayload(User user, String token) {
        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("id", user.getUserId());
        out.put("displayName", safe(user.getUsername()));
        out.put("name", safe(user.getUsername()));
        out.put("fullName", safe(user.getUsername()));
        out.put("email", safe(user.getEmail()));
        out.put("role", user.getRole() == null ? "user" : user.getRole().toAccountType());
        String avatarUrl = userAvatarService.publicAvatarUrl(user);
        if (!avatarUrl.isBlank()) {
            out.put("avatarUrl", avatarUrl);
            out.put("avatar", avatarUrl);
        }
        if (token != null && !token.isBlank()) {
            out.put("token", token);
        }
        return out;
    }

    private static String roleValue(User user) {
        return user.getRole() == null ? "user" : user.getRole().toAccountType();
    }

    private boolean safePasswordMatch(String rawPassword, String encodedPassword) {
        try {
            return passwordUtil.matches(rawPassword, encodedPassword);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String safe(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String normalizeEmail(Object o) {
        return safe(o).toLowerCase();
    }
}
