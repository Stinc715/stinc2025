package com.clubportal.controller;

import com.clubportal.dto.RegisterRequest;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.service.RegistrationEmailVerificationService;
import com.clubportal.util.PasswordEncryptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepo;
    private final PasswordEncryptionUtil passwordUtil;
    private final JwtUtil jwtUtil;
    private final RegistrationEmailVerificationService verificationService;

    public AuthController(UserRepository userRepo,
                          PasswordEncryptionUtil passwordUtil,
                          JwtUtil jwtUtil,
                          RegistrationEmailVerificationService verificationService) {
        this.userRepo = userRepo;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
        this.verificationService = verificationService;
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
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
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

            User saved = userRepo.save(u);
            verificationService.consumeVerification(normalizedEmail);
            return ResponseEntity.ok(java.util.Map.of(
                    "id", saved.getUserId(),
                    "fullName", saved.getUsername(),
                    "email", saved.getEmail(),
                    "role", saved.getRole() == null ? "user" : saved.getRole().toAccountType()
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("Email already exists");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> body) {
        String email = normalizeEmail(body.get("email"));
        String password = body.get("password");
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
            log.warn("Login failed for email={} candidates={}", email, candidates.size());
            return ResponseEntity.status(401).body("Invalid email or password");
        }

        String role = (user.getRole() == null) ? "user" : user.getRole().toAccountType();
        int nextSessionVersion = user.bumpSessionVersion();
        User savedUser = userRepo.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail(), role, nextSessionVersion);

        var resp = java.util.Map.of(
                "token", token,
                "id", savedUser.getUserId(),
                "fullName", savedUser.getUsername(),
                "email", savedUser.getEmail(),
                "role", role
        );
        return ResponseEntity.ok(resp);
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
