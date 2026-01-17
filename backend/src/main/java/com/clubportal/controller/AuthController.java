package com.clubportal.controller;

import com.clubportal.dto.RegisterRequest;
import com.clubportal.model.UserAccount;
import com.clubportal.repository.UserAccountRepository;
import com.clubportal.util.PasswordEncryptionUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserAccountRepository userRepo;
    private final PasswordEncryptionUtil passwordUtil;

    public AuthController(UserAccountRepository userRepo, PasswordEncryptionUtil passwordUtil) {
        this.userRepo = userRepo;
        this.passwordUtil = passwordUtil;
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

            // 2. 重复邮箱检测
            if (userRepo.findByEmail(req.getEmail()).isPresent()) {
                return ResponseEntity.status(409).body("Email already exists");
            }

            // 3. 保存 —— 使用 BCrypt 加密密码
            UserAccount u = new UserAccount();
            u.setFullName(req.getFullName());
            u.setEmail(req.getEmail());
            u.setPasswordHash(passwordUtil.encodePassword(req.getPassword())); // 使用 BCrypt 加密
            u.setRole(UserAccount.Role.STUDENT);  // 显式设置，避免 NULL

            UserAccount saved = userRepo.save(u);
            return ResponseEntity.ok("Registered");
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
        var resp = java.util.Map.of(
                "id", user.getId(),
                "fullName", user.getFullName(),
                "email", user.getEmail(),
                "role", user.getRole().name()
        );
        return ResponseEntity.ok(resp);
    }
}
