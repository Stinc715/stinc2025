package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.security.JwtUtil;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.ProfileDataRightsService;
import com.clubportal.service.ProfileEmailVerificationService;
import com.clubportal.service.SecurityEventService;
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

    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final PasswordEncryptionUtil passwordUtil;
    private final PasswordPolicyService passwordPolicyService;
    private final ProfileEmailVerificationService profileEmailVerificationService;
    private final VerificationEmailSenderService emailSenderService;
    private final UserAvatarService userAvatarService;
    private final StreamAuthCookieService streamAuthCookieService;
    private final ProfileDataRightsService profileDataRightsService;
    private final SecurityEventService securityEventService;

    public ProfileController(CurrentUserService currentUserService,
                             UserRepository userRepo,
                             JwtUtil jwtUtil,
                             PasswordEncryptionUtil passwordUtil,
                             PasswordPolicyService passwordPolicyService,
                             ProfileEmailVerificationService profileEmailVerificationService,
                             VerificationEmailSenderService emailSenderService,
                             UserAvatarService userAvatarService,
                             StreamAuthCookieService streamAuthCookieService,
                             ProfileDataRightsService profileDataRightsService,
                             SecurityEventService securityEventService) {
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.passwordUtil = passwordUtil;
        this.passwordPolicyService = passwordPolicyService;
        this.profileEmailVerificationService = profileEmailVerificationService;
        this.emailSenderService = emailSenderService;
        this.userAvatarService = userAvatarService;
        this.streamAuthCookieService = streamAuthCookieService;
        this.profileDataRightsService = profileDataRightsService;
        this.securityEventService = securityEventService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request,
                                        HttpServletResponse response) {
        User me = currentUserService.requireUser();
        String authProvider = resolveAuthProvider(request);
        String token = jwtUtil.generateToken(me.getEmail(), roleValue(me), me.getSessionVersionOrDefault(), authProvider);
        streamAuthCookieService.writeAuthCookies(request, response, token);
        return ResponseEntity.ok(toProfilePayload(me, null, authProvider));
    }

    @GetMapping("/export")
    public ResponseEntity<?> exportProfileData(HttpServletRequest request) {
        User me = currentUserService.requireUser();
        securityEventService.record(request, "PROFILE_EXPORT_REQUESTED", "INFO", me, Map.of());
        return ResponseEntity.ok(profileDataRightsService.buildExport(me));
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
        String authProvider = resolveAuthProvider(request);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), saved.getSessionVersionOrDefault(), authProvider);
        streamAuthCookieService.writeAuthCookies(request, response, token);
        return ResponseEntity.ok(toProfilePayload(saved, null, authProvider));
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
        String authProvider = resolveAuthProvider(request);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion, authProvider);
        streamAuthCookieService.writeAuthCookies(request, response, token);
        securityEventService.record(request, "PROFILE_EMAIL_UPDATED", "INFO", saved, Map.of(
                "authProvider", authProvider
        ));
        return ResponseEntity.ok(toProfilePayload(saved, token, authProvider));
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
            String authProvider = resolveAuthProvider(request);
            String token = jwtUtil.generateToken(me.getEmail(), roleValue(me), me.getSessionVersionOrDefault(), authProvider);
            streamAuthCookieService.writeAuthCookies(request, response, token);
            return ResponseEntity.ok(toProfilePayload(me, token, authProvider));
        }

        boolean hasOther = userRepo.findAllByEmailIgnoreCase(nextEmail).stream()
                .anyMatch(u -> !u.getUserId().equals(me.getUserId()));
        if (hasOther) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        me.setEmail(nextEmail);
        int nextSessionVersion = me.bumpSessionVersion();
        User saved = userRepo.save(me);
        String authProvider = resolveAuthProvider(request);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion, authProvider);
        profileEmailVerificationService.clearForUser(me.getUserId());
        streamAuthCookieService.writeAuthCookies(request, response, token);
        securityEventService.record(request, "PROFILE_EMAIL_UPDATED", "INFO", saved, Map.of(
                "authProvider", authProvider
        ));
        return ResponseEntity.ok(toProfilePayload(saved, token, authProvider));
    }

    @PatchMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, Object> body,
                                            HttpServletRequest request,
                                            HttpServletResponse response) {
        User me = currentUserService.requireUser();
        String authProvider = resolveAuthProvider(request);

        if (!canChangePassword(authProvider)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Google sign-in accounts cannot change password here");
        }

        String currentPassword = raw(body.get("currentPassword"));
        String newPassword = raw(body.get("newPassword"));

        if (currentPassword.isBlank()) {
            return ResponseEntity.badRequest().body("Current password is required");
        }
        if (newPassword.isBlank()) {
            return ResponseEntity.badRequest().body("New password is required");
        }
        String passwordValidationMessage = passwordPolicyService.validate(newPassword).orElse(null);
        if (passwordValidationMessage != null) {
            return ResponseEntity.badRequest().body(passwordValidationMessage);
        }
        if (!safePasswordMatch(currentPassword, safe(me.getPasswordHash()))) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        me.setPasswordHash(passwordUtil.encodePassword(newPassword));
        int nextSessionVersion = me.bumpSessionVersion();
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion, authProvider);
        streamAuthCookieService.writeAuthCookies(request, response, token);
        securityEventService.record(request, "PASSWORD_CHANGED", "INFO", saved, Map.of(
                "authProvider", authProvider
        ));
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

    @PostMapping("/session/rotate")
    public ResponseEntity<?> rotateSession(HttpServletRequest request,
                                           HttpServletResponse response) {
        User me = currentUserService.requireUser();
        String authProvider = resolveAuthProvider(request);
        int nextSessionVersion = me.bumpSessionVersion();
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), nextSessionVersion, authProvider);
        streamAuthCookieService.writeAuthCookies(request, response, token);
        securityEventService.record(request, "SESSION_ROTATED", "INFO", saved, Map.of(
                "authProvider", authProvider
        ));
        return ResponseEntity.ok(Map.of(
                "rotated", true,
                "token", token,
                "authTokenTtlSeconds", streamAuthCookieService.getAuthTokenTtlSeconds(),
                "streamTokenTtlSeconds", streamAuthCookieService.getStreamTokenTtlSeconds()
        ));
    }

    @PostMapping("/deletion-request")
    public ResponseEntity<?> createDeletionRequest(@RequestBody(required = false) Map<String, Object> body,
                                                   HttpServletRequest request) {
        User me = currentUserService.requireUser();
        String reason = body == null ? "" : safe(body.get("reason"));
        ProfileDataRightsService.DeletionRequestSubmission submission =
                profileDataRightsService.submitDeletionRequest(me, reason);
        securityEventService.record(request, "PROFILE_DELETION_REQUESTED", "WARN", me, Map.of(
                "created", submission.created()
        ));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "submitted", true,
                "created", submission.created(),
                "message", submission.created()
                        ? "Your deletion request has been recorded for manual review."
                        : "A deletion request is already pending for this account.",
                "deletionRequest", profileDataRightsService.toDeletionRequestPayload(submission.request())
        ));
    }

    private Map<String, Object> toProfilePayload(User user, String token, String authProvider) {
        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("id", user.getUserId());
        out.put("displayName", safe(user.getUsername()));
        out.put("name", safe(user.getUsername()));
        out.put("fullName", safe(user.getUsername()));
        out.put("email", safe(user.getEmail()));
        out.put("role", user.getRole() == null ? "user" : user.getRole().toAccountType());
        out.put("authProvider", authProvider);
        out.put("canChangePassword", canChangePassword(authProvider));
        String avatarUrl = safe(userAvatarService.publicAvatarUrl(user));
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

    private String resolveAuthProvider(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token.isBlank()) {
            return JwtUtil.AUTH_PROVIDER_PASSWORD;
        }
        try {
            return jwtUtil.extractAuthProvider(token);
        } catch (Exception ignored) {
            return JwtUtil.AUTH_PROVIDER_PASSWORD;
        }
    }

    private static String resolveToken(HttpServletRequest request) {
        if (request == null) return "";
        String header = safe(request.getHeader("Authorization"));
        if (header.startsWith("Bearer ")) {
            return header.substring(7).trim();
        }
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) return "";
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if (cookie == null) continue;
            String name = safe(cookie.getName());
            if (!StreamAuthCookieService.AUTH_TOKEN_COOKIE.equals(name)
                    && !StreamAuthCookieService.STREAM_TOKEN_COOKIE.equals(name)) {
                continue;
            }
            String value = safe(cookie.getValue());
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static boolean canChangePassword(String authProvider) {
        return !JwtUtil.AUTH_PROVIDER_GOOGLE.equalsIgnoreCase(safe(authProvider));
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

    private static String raw(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static String normalizeEmail(Object o) {
        return safe(o).toLowerCase();
    }
}
