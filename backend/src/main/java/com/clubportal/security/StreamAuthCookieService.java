package com.clubportal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class StreamAuthCookieService {

    public static final String AUTH_TOKEN_COOKIE = "club_portal_auth_token";
    public static final String STREAM_TOKEN_COOKIE = "club_portal_stream_token";

    @org.springframework.beans.factory.annotation.Value("${app.security.session.auth-token-ttl-days:7}")
    private long authTokenTtlDays = 7;

    @org.springframework.beans.factory.annotation.Value("${app.security.session.stream-token-ttl-days:1}")
    private long streamTokenTtlDays = 1;

    public void writeAuthCookies(HttpServletRequest request, HttpServletResponse response, String token) {
        writeAuthToken(request, response, token);
        writeStreamToken(request, response, token);
    }

    public void writeAuthToken(HttpServletRequest request, HttpServletResponse response, String token) {
        if (response == null || token == null || token.isBlank()) {
            return;
        }
        response.addHeader("Set-Cookie", buildCookie(AUTH_TOKEN_COOKIE, token.trim(), request, authTokenTtl()).toString());
    }

    public void writeStreamToken(HttpServletRequest request, HttpServletResponse response, String token) {
        if (response == null || token == null || token.isBlank()) {
            return;
        }
        response.addHeader("Set-Cookie", buildCookie(STREAM_TOKEN_COOKIE, token.trim(), request, streamTokenTtl()).toString());
    }

    public void clearAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        if (response == null) {
            return;
        }
        response.addHeader("Set-Cookie", buildCookie(AUTH_TOKEN_COOKIE, "", request, Duration.ZERO).toString());
        response.addHeader("Set-Cookie", buildCookie(STREAM_TOKEN_COOKIE, "", request, Duration.ZERO).toString());
    }

    private static ResponseCookie buildCookie(String name,
                                              String token,
                                              HttpServletRequest request,
                                              Duration ttl) {
        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Lax")
                .path("/")
                .maxAge(ttl)
                .build();
    }

    private static boolean isSecureRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && forwardedProto.equalsIgnoreCase("https")) {
            return true;
        }
        return request.isSecure();
    }

    public long getAuthTokenTtlSeconds() {
        return authTokenTtl().getSeconds();
    }

    public long getStreamTokenTtlSeconds() {
        return streamTokenTtl().getSeconds();
    }

    private Duration authTokenTtl() {
        return Duration.ofDays(Math.max(1, authTokenTtlDays));
    }

    private Duration streamTokenTtl() {
        return Duration.ofDays(Math.max(1, streamTokenTtlDays));
    }
}
