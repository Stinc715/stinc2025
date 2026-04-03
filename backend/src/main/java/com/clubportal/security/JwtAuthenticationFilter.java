package com.clubportal.security;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String AUTH_FAILURE_ATTRIBUTE = JwtAuthenticationFilter.class.getName() + ".AUTH_FAILURE";

    private final JwtUtil jwtUtil;
    private final UserRepository userRepo;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepo) {
        this.jwtUtil = jwtUtil;
        this.userRepo = userRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && !token.isBlank()) {

            try {
                String email = jwtUtil.extractEmail(token);
                Integer tokenSessionVersion = jwtUtil.extractSessionVersion(token);
                User user = userRepo.findByEmailIgnoreCase(email).orElse(null);
                if (user == null || tokenSessionVersion == null
                        || user.getSessionVersionOrDefault() != tokenSessionVersion) {
                    SecurityContextHolder.clearContext();
                    request.setAttribute(AUTH_FAILURE_ATTRIBUTE, Boolean.TRUE);
                    filterChain.doFilter(request, response);
                    return;
                }

                String role = user.getRole() == null ? "user" : user.getRole().toAccountType();

                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
                if (role != null && !role.isBlank()) {
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.trim().toUpperCase()));
                }

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                authorities
                        );

                auth.setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);

            } catch (Exception ex) {
                log.debug("JWT rejected for uri={} reason={}", request.getRequestURI(), ex.getMessage());
                SecurityContextHolder.clearContext();
                request.setAttribute(AUTH_FAILURE_ATTRIBUTE, Boolean.TRUE);
                filterChain.doFilter(request, response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private static String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        String authCookieToken = readCookieValue(request, StreamAuthCookieService.AUTH_TOKEN_COOKIE);
        if (authCookieToken != null) {
            return authCookieToken;
        }

        if (isChatStreamRequest(request)) {
            String streamCookieToken = readCookieValue(request, StreamAuthCookieService.STREAM_TOKEN_COOKIE);
            if (streamCookieToken != null) {
                return streamCookieToken;
            }
        }
        return null;
    }

    private static String readCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookieName == null || cookieName.isBlank()) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie == null) continue;
            if (!cookieName.equals(cookie.getName())) continue;
            String value = cookie.getValue();
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isChatStreamRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "GET".equalsIgnoreCase(request.getMethod())
                && uri != null
                && uri.startsWith("/api/")
                && uri.endsWith("/chat/stream");
    }
}
