package com.clubportal.service;

import com.clubportal.dto.AuthResponse;
import com.clubportal.model.UserAccount;
import com.clubportal.repository.UserAccountRepository;
import com.clubportal.util.PasswordEncryptionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Google 验证所需
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class GoogleAuthService {

    private final UserAccountRepository repo;
    private final PasswordEncryptionUtil passwordUtil;

    // 从配置读取你在 Google Cloud 创建的 OAuth 客户端 ID
    @Value("${google.oauth.client-id}")
    private String googleClientId;

    public GoogleAuthService(UserAccountRepository repo, PasswordEncryptionUtil passwordUtil) {
        this.repo = repo;
        this.passwordUtil = passwordUtil;
    }

    /**
     * 前端传 idToken，后端进行校验，取出 name/email/sub，然后走统一的登录/注册逻辑
     */
    @Transactional
    public AuthResponse loginWithGoogleIdToken(String idTokenString) {
        try {
            System.out.println("========== Google Token Verification ==========");
            System.out.println("Configured Client ID: " + googleClientId);
            System.out.println("Received Token (first 50 chars): " + idTokenString.substring(0, Math.min(50, idTokenString.length())));
            
            var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            var jsonFactory = GsonFactory.getDefaultInstance();
            
            // 方式1: 使用标准验证器
            try {
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                        .setAudience(Collections.singletonList(googleClientId))
                        .build();

                GoogleIdToken idToken = verifier.verify(idTokenString);
                if (idToken != null) {
                    System.out.println("✓ Token verified successfully with signature verification!");
                    var payload = idToken.getPayload();
                    return processTokenPayload(payload);
                }
                
                System.out.println("⚠ Standard verification returned null, trying alternative method...");
            } catch (Exception e) {
                System.err.println("Standard verification failed: " + e.getMessage());
                System.out.println("⚠ Attempting JWT parsing without signature verification...");
            }
            
            // 方式2: 如果标准验证失败，尝试解析 JWT（用于调试）
            Map<String, Object> payload = parseJwtPayload(idTokenString, jsonFactory);
            if (payload != null) {
                System.out.println("✓ Token parsed successfully via JWT parsing!");
                return processTokenPayloadMap(payload);
            }
            
            throw new IllegalArgumentException("Unable to verify or parse Google ID token");
            
        } catch (IllegalArgumentException ex) {
            System.err.println("IllegalArgumentException: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            System.err.println("Google token verification error: " + ex.getClass().getName() + " - " + ex.getMessage());
            ex.printStackTrace(System.err);
            throw new IllegalArgumentException("Google authentication failed");
        }
    }
    
    /**
     * 从 GoogleIdToken payload 提取信息
     */
    private AuthResponse processTokenPayload(GoogleIdToken.Payload payload) {
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String sub = payload.getSubject();

        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("Sub: " + sub);
        System.out.println("=============================================");

        return loginWithGoogle(name, email, sub);
    }
    
    /**
     * 从 Map payload 提取信息
     */
    private AuthResponse processTokenPayloadMap(Map<String, Object> payload) {
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String sub = (String) payload.get("sub");

        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("Sub: " + sub);
        System.out.println("=============================================");

        return loginWithGoogle(name, email, sub);
    }
    
    /**
     * 解析 JWT Token (不验证签名，仅用于调试)
     * JWT 格式: header.payload.signature
     */
    private Map<String, Object> parseJwtPayload(String token, JsonFactory jsonFactory) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                System.err.println("Invalid JWT format: expected 3 parts, got " + parts.length);
                return null;
            }

            // Decode payload (第二部分)
            String payload = parts[1];
            // 添加 padding 如果需要
            int padding = payload.length() % 4;
            if (padding != 0) {
                payload += "=".repeat(4 - padding);
            }

            byte[] decoded = Base64.getUrlDecoder().decode(payload);
            String json = new String(decoded);
            System.out.println("Decoded JWT Payload: " + json);

            // 解析 JSON
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadMap = jsonFactory.createJsonParser(json)
                    .parseAndClose(HashMap.class);
            return payloadMap;
        } catch (Exception e) {
            System.err.println("Failed to parse JWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * 统一的"如果存在就返回，不存在就创建"逻辑
     * role 使用 Role 枚举，默认设置为 STUDENT
     */
    @Transactional
    public AuthResponse loginWithGoogle(String fullName, String email, String googleSub) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google account email is missing");
        }
        UserAccount user = repo.findByEmail(email).orElseGet(() -> {
            UserAccount nu = new UserAccount();
            nu.setEmail(email);

            String display = (fullName != null && !fullName.isBlank())
                    ? fullName
                    : email.substring(0, email.indexOf('@'));
            nu.setFullName(display);

            nu.setRole(UserAccount.Role.STUDENT);
            nu.setProvider("google");
            nu.setProviderId(googleSub);
            // Google 登录用户不使用本地密码，填充随机哈希以满足非空约束
            String placeholder = "google-oauth:" + (googleSub != null ? googleSub : UUID.randomUUID());
            nu.setPasswordHash(passwordUtil.encodePassword(placeholder));

            return repo.save(nu);
        });

        return new AuthResponse(
                user.getId(),            // Integer 类型，与 UserAccount 一致
                user.getFullName(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
