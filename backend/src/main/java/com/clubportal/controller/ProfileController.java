package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    public ProfileController(CurrentUserService currentUserService,
                             UserRepository userRepo,
                             JwtUtil jwtUtil) {
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        User me = currentUserService.requireUser();
        return ResponseEntity.ok(toProfilePayload(me, null));
    }

    @PatchMapping("/email")
    public ResponseEntity<?> updateEmail(@RequestBody Map<String, Object> body) {
        User me = currentUserService.requireUser();

        String nextEmail = normalizeEmail(body.get("email"));
        if (nextEmail.isBlank()) {
            return ResponseEntity.badRequest().body("Missing email");
        }
        if (!EMAIL_RE.matcher(nextEmail).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        if (nextEmail.equalsIgnoreCase(safe(me.getEmail()))) {
            String token = jwtUtil.generateToken(me.getEmail(), roleValue(me), me.getSessionVersionOrDefault());
            return ResponseEntity.ok(toProfilePayload(me, token));
        }

        boolean hasOther = userRepo.findAllByEmailIgnoreCase(nextEmail).stream()
                .anyMatch(u -> !u.getUserId().equals(me.getUserId()));
        if (hasOther) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
        }

        me.setEmail(nextEmail);
        User saved = userRepo.save(me);
        String token = jwtUtil.generateToken(saved.getEmail(), roleValue(saved), saved.getSessionVersionOrDefault());
        return ResponseEntity.ok(toProfilePayload(saved, token));
    }

    private static Map<String, Object> toProfilePayload(User user, String token) {
        java.util.Map<String, Object> out = new java.util.HashMap<>();
        out.put("id", user.getUserId());
        out.put("displayName", safe(user.getUsername()));
        out.put("name", safe(user.getUsername()));
        out.put("fullName", safe(user.getUsername()));
        out.put("email", safe(user.getEmail()));
        out.put("role", user.getRole() == null ? "user" : user.getRole().toAccountType());
        if (token != null && !token.isBlank()) {
            out.put("token", token);
        }
        return out;
    }

    private static String roleValue(User user) {
        return user.getRole() == null ? "user" : user.getRole().toAccountType();
    }

    private static String safe(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static String normalizeEmail(Object o) {
        return safe(o).toLowerCase();
    }
}
