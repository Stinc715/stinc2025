package com.clubportal.controller;

import com.clubportal.dto.RegisterRequest;
import com.clubportal.model.UserAccount;
import com.clubportal.repository.UserAccountRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.util.PasswordEncryptionUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserAccountRepository userRepo;
    private final PasswordEncryptionUtil passwordUtil;
    private final JwtUtil jwtUtil;

    public AuthController(UserAccountRepository userRepo, PasswordEncryptionUtil passwordUtil, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordUtil = passwordUtil;
        this.jwtUtil = jwtUtil;
    }

    private static UserAccount.Role resolveRoleForRegistration(String role) {
        if (role == null || role.isBlank()) return UserAccount.Role.STUDENT;

        String r = role.trim().toUpperCase();
        if (r.equals("CLUB") || r.equals("CLUB_LEADER") || r.equals("CLUBLEADER")) return UserAccount.Role.CLUB_LEADER;
        if (r.equals("STUDENT") || r.equals("USER") || r.equals("MEMBER")) return UserAccount.Role.STUDENT;

        // Never allow users to self-assign elevated roles (e.g. ADMIN) via public registration.
        return UserAccount.Role.STUDENT;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            // 1. 基础校验
            if (req.getFullName() == null || req.getFullName().isBlank()
                    || req.getEmail() == null || req.getEmail().isBlank()
                    || req.getPassword() == null || req.getPassword().isBlank()) {
                return ResponseEntity.badRequest().body("Missing fields");
            }

            UserAccount.Role requestedRole = resolveRoleForRegistration(req.getRole());

            // 2. 重复邮箱检测
            // For club registration, allow promoting an existing STUDENT account to CLUB_LEADER
            // when the password matches (prevents getting stuck after registering on the wrong tab).
            var existingOpt = userRepo.findByEmail(req.getEmail());
            if (existingOpt.isPresent()) {
                UserAccount existing = existingOpt.get();

                if (requestedRole == UserAccount.Role.CLUB_LEADER) {
                    if (!passwordUtil.matches(req.getPassword(), existing.getPasswordHash())) {
                        return ResponseEntity.status(409).body("Email already exists");
                    }

                    // Never overwrite elevated roles; only promote STUDENT -> CLUB_LEADER.
                    if (existing.getRole() == null || existing.getRole() == UserAccount.Role.STUDENT) {
                        existing.setRole(UserAccount.Role.CLUB_LEADER);
                    }
                    if (existing.getFullName() == null || existing.getFullName().isBlank()) {
                        existing.setFullName(req.getFullName());
                    }

                    UserAccount saved = userRepo.save(existing);
                    return ResponseEntity.ok(java.util.Map.of(
                            "id", saved.getId(),
                            "fullName", saved.getFullName(),
                            "email", saved.getEmail(),
                            "role", saved.getRole().name()
                    ));
                }

                return ResponseEntity.status(409).body("Email already exists");
            }

            // 3. 保存 —— 使用 BCrypt 加密密码
            UserAccount u = new UserAccount();
            u.setFullName(req.getFullName());
            u.setEmail(req.getEmail());
            u.setPasswordHash(passwordUtil.encodePassword(req.getPassword())); // 使用 BCrypt 加密
            u.setRole(requestedRole);  // Allow club registration; never self-assign ADMIN

            UserAccount saved = userRepo.save(u);
            return ResponseEntity.ok(java.util.Map.of(
                    "id", saved.getId(),
                    "fullName", saved.getFullName(),
                    "email", saved.getEmail(),
                    "role", saved.getRole().name()
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body("Email already exists");
        } catch (Exception e) {
            // 打印到控制台，便于你定位
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Internal error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody java.util.Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");
        var user = userRepo.findByEmail(email).orElse(null);
        if (user == null || !passwordUtil.matches(password, user.getPasswordHash())) {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
        // 返回给前端的结构（与前端预期一致）
        String role = (user.getRole() == null) ? UserAccount.Role.STUDENT.name() : user.getRole().name();
        String token = jwtUtil.generateToken(user.getEmail(), role);

        var resp = java.util.Map.of(
                "token", token,
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", role
        );
        return ResponseEntity.ok(resp);
    }
}
