package com.clubportal.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtUtilTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Test
    void generatesTokenWithExpectedClaims() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);

        String token = jwtUtil.generateToken("member@example.com", "user", 3);

        assertEquals("member@example.com", jwtUtil.extractEmail(token));
        assertEquals("user", jwtUtil.extractRole(token));
        assertEquals(3, jwtUtil.extractSessionVersion(token));
        assertEquals("password", jwtUtil.extractAuthProvider(token));
    }

    @Test
    void normalizesMissingOrInvalidSessionVersionToOne() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);

        String token = jwtUtil.generateToken("member@example.com", "user", 0);

        assertEquals(1, jwtUtil.extractSessionVersion(token));
    }

    @Test
    void preservesExplicitGoogleAuthProviderClaim() {
        JwtUtil jwtUtil = new JwtUtil(SECRET);

        String token = jwtUtil.generateToken("member@example.com", "user", 2, JwtUtil.AUTH_PROVIDER_GOOGLE);

        assertEquals("google", jwtUtil.extractAuthProvider(token));
    }
}
