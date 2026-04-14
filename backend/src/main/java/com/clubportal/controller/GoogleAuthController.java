package com.clubportal.controller;

import com.clubportal.dto.AuthResponse;
import com.clubportal.dto.GoogleLoginRequest;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.security.JwtUtil;
import com.clubportal.service.GoogleAuthService;
import com.clubportal.service.GoogleLoginPolicyException;
import com.clubportal.service.SecurityEventService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthController.class);

    private final GoogleAuthService googleAuthService;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;
    private final StreamAuthCookieService streamAuthCookieService;
    private final SecurityEventService securityEventService;

    public GoogleAuthController(GoogleAuthService googleAuthService,
                                UserRepository userRepo,
                                JwtUtil jwtUtil,
                                StreamAuthCookieService streamAuthCookieService,
                                SecurityEventService securityEventService) {
        this.googleAuthService = googleAuthService;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
        this.streamAuthCookieService = streamAuthCookieService;
        this.securityEventService = securityEventService;
    }

    /**
     * POST /api/auth/google
     * { "credential": "eyJhbGciOiJSUzI1NiIsImtpZCI6..." }
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest req,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        try {
            if (req == null || req.getCredential() == null || req.getCredential().isBlank()) {
                return ResponseEntity.badRequest().body("Missing credential");
            }
            String credential = req.getCredential().trim();
            int segments = credential.split("\\.").length;
            log.info("Google credential received: len={}, segments={}", credential.length(), segments);
            if (segments != 3) {
                return ResponseEntity.status(401).body("Invalid Google credential format");
            }

            AuthResponse auth = googleAuthService.loginWithGoogleIdToken(credential);
            User user = userRepo.findByEmailIgnoreCase(auth.getEmail()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401).body("Account not found");
            }
            int nextSessionVersion = user.bumpSessionVersion();
            User savedUser = userRepo.save(user);
            String token = jwtUtil.generateToken(savedUser.getEmail(), auth.getRole(), nextSessionVersion, auth.getAuthProvider());
            streamAuthCookieService.writeAuthCookies(request, response, token);
            securityEventService.record(request, "GOOGLE_LOGIN_SUCCESS", "INFO", savedUser, Map.of(
                    "role", auth.getRole(),
                    "authProvider", auth.getAuthProvider()
            ));
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "id", savedUser.getUserId(),
                    "email", savedUser.getEmail(),
                    "fullName", savedUser.getUsername(),
                    "role", savedUser.getRole() == null ? "user" : savedUser.getRole().toAccountType(),
                    "authProvider", auth.getAuthProvider(),
                    "canChangePassword", false
            ));
        } catch (GoogleLoginPolicyException ex) {
            securityEventService.recordForEmail(request, "GOOGLE_LOGIN_POLICY_BLOCKED", "WARN", "", Map.of(
                    "reason", ex.getMessage()
            ));
            return ResponseEntity.status(403).body(ex.getMessage());
        } catch (IllegalArgumentException ex) {
            String message = ex.getMessage() == null || ex.getMessage().isBlank()
                    ? "Google authentication failed"
                    : ex.getMessage();
            log.warn("Google login validation failed: {}", message);
            securityEventService.recordForEmail(request, "GOOGLE_LOGIN_FAILED", "WARN", "", Map.of(
                    "reason", message
            ));
            return ResponseEntity.status(401).body(message);
        } catch (Exception e) {
            log.error("Google login error: {} - {}", e.getClass().getName(), e.getMessage(), e);
            securityEventService.recordForEmail(request, "GOOGLE_LOGIN_ERROR", "HIGH", "", Map.of(
                    "reason", e.getClass().getSimpleName()
            ));
            return ResponseEntity.internalServerError().body("Authentication service temporarily unavailable");
        }
    }
}
