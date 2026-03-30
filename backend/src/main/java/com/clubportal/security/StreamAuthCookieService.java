package com.clubportal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class StreamAuthCookieService {

    public static final String STREAM_TOKEN_COOKIE = "club_portal_stream_token";
    private static final Duration STREAM_TOKEN_TTL = Duration.ofDays(1);

    public void writeStreamToken(HttpServletRequest request, HttpServletResponse response, String token) {
        if (response == null || token == null || token.isBlank()) {
            return;
        }
        response.addHeader("Set-Cookie", buildCookie(token.trim(), request).toString());
    }

    private static ResponseCookie buildCookie(String token, HttpServletRequest request) {
        return ResponseCookie.from(STREAM_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(isSecureRequest(request))
                .sameSite("Lax")
                .path("/")
                .maxAge(STREAM_TOKEN_TTL)
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
}
