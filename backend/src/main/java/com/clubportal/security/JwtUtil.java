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
    private final Key key;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        // HS256 requires a sufficiently long secret. Keep config-driven to avoid committing secrets.
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String email, String role) {
        return generateToken(email, role, 1);
    }

    public String generateToken(String email, String role, Integer sessionVersion) {
        int normalizedSessionVersion = (sessionVersion == null || sessionVersion < 1) ? 1 : sessionVersion;
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .claim("sv", normalizedSessionVersion)
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

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
