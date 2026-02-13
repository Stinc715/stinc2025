package com.clubportal.controller;

import com.clubportal.dto.AuthResponse;
import com.clubportal.dto.GoogleLoginRequest;
import com.clubportal.security.JwtUtil;
import com.clubportal.service.GoogleAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;
    private final JwtUtil jwtUtil;

    public GoogleAuthController(GoogleAuthService googleAuthService, JwtUtil jwtUtil) {
        this.googleAuthService = googleAuthService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 前端把 Google Identity Services 返回的 ID Token 放在 body.credential 里传过来
     * POST /api/auth/google
     * { "credential": "eyJhbGciOiJSUzI1NiIsImtpZCI6..." }
     */
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest req) {
        try {
            if (req == null || req.getCredential() == null || req.getCredential().isBlank()) {
                return ResponseEntity.badRequest().body("Missing credential");
            }
            AuthResponse auth = googleAuthService.loginWithGoogleIdToken(req.getCredential());
            String token = jwtUtil.generateToken(auth.getEmail(), auth.getRole());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "id", auth.getId(),
                    "email", auth.getEmail(),
                    "fullName", auth.getFullName(),
                    "role", auth.getRole()
            ));
        } catch (IllegalArgumentException ex) {
            // 返回安全的错误信息，不暴露内部细节
            System.err.println("Google login validation failed: " + ex.getMessage());
            return ResponseEntity.status(401).body("Google authentication failed");
        } catch (Exception e) {
            // 日志记录技术细节，向客户端返回通用错误
            System.err.println("Google login error: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace(System.err);
            return ResponseEntity.internalServerError().body("Authentication service temporarily unavailable");
        }
    }
}
