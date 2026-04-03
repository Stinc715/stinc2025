package com.clubportal.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final long TOKEN_TTL_MS = 86400000L;
    public static final String AUTH_PROVIDER_PASSWORD = "password";
    public static final String AUTH_PROVIDER_GOOGLE = "google";
    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        String normalized = secret == null ? "" : secret.trim();
        if (normalized.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be configured and at least 32 characters long");
        }
        // HS256 requires a sufficiently long secret. Keep config-driven to avoid committing secrets.
        this.key = Keys.hmacShaKeyFor(normalized.getBytes());
    }

    public String generateToken(String email, String role) {
        return generateToken(email, role, 1, AUTH_PROVIDER_PASSWORD);
    }

    public String generateToken(String email, String role, Integer sessionVersion) {
        return generateToken(email, role, sessionVersion, AUTH_PROVIDER_PASSWORD);
    }

    public String generateToken(String email, String role, Integer sessionVersion, String authProvider) {
        int normalizedSessionVersion = (sessionVersion == null || sessionVersion < 1) ? 1 : sessionVersion;
        String normalizedAuthProvider = normalizeAuthProvider(authProvider);
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("sv", normalizedSessionVersion)
                .claim("authProvider", normalizedAuthProvider)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TOKEN_TTL_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        Object raw = extractClaims(token).get("role");
        return raw == null ? null : String.valueOf(raw);
    }

    public Integer extractSessionVersion(String token) {
        Object raw = extractClaims(token).get("sv");
        if (raw == null) return null;
        if (raw instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (Exception ex) {
            return null;
        }
    }

    public String extractAuthProvider(String token) {
        Object raw = extractClaims(token).get("authProvider");
        return normalizeAuthProvider(raw == null ? null : String.valueOf(raw));
    }

    private static String normalizeAuthProvider(String authProvider) {
        String normalized = authProvider == null ? "" : authProvider.trim().toLowerCase();
        if (AUTH_PROVIDER_GOOGLE.equals(normalized)) {
            return AUTH_PROVIDER_GOOGLE;
        }
        return AUTH_PROVIDER_PASSWORD;
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
