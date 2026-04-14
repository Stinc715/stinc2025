package com.clubportal.controller;

import com.clubportal.dto.RegisterRequest;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.service.LoginThrottleService;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.RegistrationEmailVerificationService;
import com.clubportal.service.SecurityEventService;
import com.clubportal.util.PasswordEncryptionUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepo;
    private final PasswordEncryptionUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final RegistrationEmailVerificationService verificationService;
    private final PasswordPolicyService passwordPolicyService;
    private final StreamAuthCookieService streamAuthCookieService;
    private final LoginThrottleService loginThrottleService;
    private final SecurityEventService securityEventService;

    public AuthController(UserRepository userRepo,
                          PasswordEncryptionUtil passwordUtil,
                          JwtUtil jwtUtil,
                          RegistrationEmailVerificationService verificationService,
                          PasswordPolicyService passwordPolicyService,
                          StreamAuthCookieService streamAuthCookieService,
                          LoginThrottleService loginThrottleService,
                          SecurityEventService securityEventService) {
        this.userRepo = userRepo;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.verificationService = verificationService;
        this.passwordPolicyService = passwordPolicyService;
        this.streamAuthCookieService = streamAuthCookieService;
        this.loginThrottleService = loginThrottleService;
        this.securityEventService = securityEventService;
    }

    private static User.Role resolveRoleForRegistration(String role) {
        if (role == null || role.isBlank()) return User.Role.USER;

        String r = role.trim().toUpperCase();
        if (r.equals("CLUB") || r.equals("CLUB_LEADER") || r.equals("CLUBLEADER")) return User.Role.CLUB;
        if (r.equals("STUDENT") || r.equals("USER") || r.equals("MEMBER")) return User.Role.USER;

        // Never allow users to self-assign elevated roles (e.g. ADMIN) via public registration.
        return User.Role.USER;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        try {
            if (req.getFullName() == null || req.getFullName().isBlank()
                    || req.getEmail() == null || req.getEmail().isBlank()
                    || req.getPassword() == null || req.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body("Missing fields");
            }

            String normalizedEmail = normalizeEmail(req.getEmail());
            if (normalizedEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Missing email");
            }
            String passwordValidationMessage = passwordPolicyService.validate(req.getPassword()).orElse(null);
            if (passwordValidationMessage != null) {
                return ResponseEntity.badRequest().body(passwordValidationMessage);
            }

            if (!verificationService.isVerifiedForRegistration(normalizedEmail)) {
                return ResponseEntity.badRequest().body("Email verification required");
            }

            User.Role requestedRole = resolveRoleForRegistration(req.getRole());

            // One email can only own one account identity.
            if (!userRepo.findAllByEmailIgnoreCase(normalizedEmail).isEmpty()) {
                return ResponseEntity.status(409).body("Email already exists");
            }

            User u = new User();
            u.setUsername(req.getFullName().trim());
            u.setEmail(normalizedEmail);
            u.setPasswordHash(passwordUtil.encodePassword(req.getPassword()));
            u.setRole(requestedRole);

            int nextSessionVersion = u.bumpSessionVersion();
            User saved = userRepo.save(u);
            verificationService.consumeVerification(normalizedEmail);
            String role = saved.getRole() == null ? "user" : saved.getRole().toAccountType();
            String token = jwtUtil.generateToken(saved.getEmail(), role, nextSessionVersion, JwtUtil.AUTH_PROVIDER_PASSWORD);
            streamAuthCookieService.writeAuthCookies(request, response, token);
            securityEventService.record(request, "ACCOUNT_REGISTERED", "INFO", saved, Map.of(
                    "role", role,
                    "authProvider", JwtUtil.AUTH_PROVIDER_PASSWORD
            ));
            return ResponseEntity.ok(authPayload(saved, role, token, JwtUtil.AUTH_PROVIDER_PASSWORD));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("Email already exists");
        } catch (Exception e) {
            log.error("Unexpected registration failure for email={}", normalizeEmail(req.getEmail()), e);
            return ResponseEntity.internalServerError().body("Registration failed. Please try again later.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> body,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        String email = normalizeEmail(body.get("email"));
        String password = body.get("password");
        String clientIp = resolveClientIp(request);
        String blockedMessage = loginThrottleService.getBlockMessage(email, clientIp).orElse(null);
        if (blockedMessage != null) {
            securityEventService.recordForEmail(request, "LOGIN_BLOCKED", "HIGH", email, Map.of(
                    "clientIp", clientIp,
                    "reason", "throttle_limit_reached"
            ));
            return ResponseEntity.status(429).body(blockedMessage);
        }
        if (email.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        List<User> candidates = userRepo.findAllByEmailIgnoreCase(email);
        User user = findMatchingUser(candidates, password);
        if (user == null) {
            String trimmedPassword = password.trim();
            if (!trimmedPassword.equals(password)) {
                user = findMatchingUser(candidates, trimmedPassword);
            }
        }
        if (user == null) {
            loginThrottleService.recordFailure(email, clientIp);
            log.warn("Login failed for email={} candidates={}", email, candidates.size());
            securityEventService.recordForEmail(request, "LOGIN_FAILED", "WARN", email, Map.of(
                    "clientIp", clientIp,
                    "candidateCount", candidates.size()
            ));
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String role = (user.getRole() == null) ? "user" : user.getRole().toAccountType();
        int nextSessionVersion = user.bumpSessionVersion();
        User savedUser = userRepo.save(user);
        loginThrottleService.recordSuccess(email, clientIp);
        String token = jwtUtil.generateToken(savedUser.getEmail(), role, nextSessionVersion, JwtUtil.AUTH_PROVIDER_PASSWORD);
        streamAuthCookieService.writeAuthCookies(request, response, token);
        securityEventService.record(request, "LOGIN_SUCCESS", "INFO", savedUser, Map.of(
                "role", role,
                "authProvider", JwtUtil.AUTH_PROVIDER_PASSWORD
        ));

        return ResponseEntity.ok(authPayload(savedUser, role, token, JwtUtil.AUTH_PROVIDER_PASSWORD));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        securityEventService.recordForEmail(request, "LOGOUT", "INFO", resolveEmailFromToken(request), Map.of());
        streamAuthCookieService.clearAuthCookies(request, response);
        return ResponseEntity.ok(java.util.Map.of("logout", true));
    }

    private static java.util.Map<String, Object> authPayload(User user, String role, String token, String authProvider) {
        return java.util.Map.of(
                "token", token,
                "id", user.getUserId(),
                "fullName", user.getUsername(),
                "email", user.getEmail(),
                "role", role,
                "authProvider", authProvider,
                "canChangePassword", JwtUtil.AUTH_PROVIDER_PASSWORD.equalsIgnoreCase(authProvider)
        );
    }

    private User findMatchingUser(List<User> candidates, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) return null;
        return candidates.stream()
                .filter(u -> safePasswordMatch(rawPassword, u.getPasswordHash()))
                .sorted(byRolePriority())
                .findFirst()
                .orElse(null);
    }

    private boolean safePasswordMatch(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) return false;
        try {
            return passwordUtil.matches(rawPassword, encodedPassword);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String remoteAddr = request.getRemoteAddr();
        return remoteAddr == null ? "" : remoteAddr.trim();
    }

    private String resolveEmailFromToken(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String header = request.getHeader("Authorization");
        String token = "";
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7).trim();
        }
        if (token.isBlank() && request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (cookie == null) continue;
                if (!StreamAuthCookieService.AUTH_TOKEN_COOKIE.equals(cookie.getName())
                        && !StreamAuthCookieService.STREAM_TOKEN_COOKIE.equals(cookie.getName())) {
                    continue;
                }
                token = cookie.getValue() == null ? "" : cookie.getValue().trim();
                if (!token.isBlank()) {
                    break;
                }
            }
        }
        if (token.isBlank()) {
            return "";
        }
        try {
            return normalizeEmail(jwtUtil.extractEmail(token));
        } catch (Exception ignored) {
            return "";
        }
    }

    private static Comparator<User> byRolePriority() {
        return Comparator
                .comparingInt((User u) -> roleRank(u.getRole()))
                .thenComparing(User::getUserId, java.util.Comparator.nullsLast(Integer::compareTo));
    }

    private static int roleRank(User.Role role) {
        if (role == User.Role.CLUB || role == User.Role.ADMIN) return 0;
        return 1;
    }
}
