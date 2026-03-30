package com.clubportal.security;

import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesChatStreamUsingStreamCookie() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userRepository);

        User user = new User();
        user.setEmail("member@example.com");
        user.setRole(User.Role.USER);
        user.setSessionVersion(2);

        when(jwtUtil.extractEmail("chat-token")).thenReturn("member@example.com");
        when(jwtUtil.extractSessionVersion("chat-token")).thenReturn(2);
        when(userRepository.findByEmailIgnoreCase("member@example.com")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/clubs/12/chat/stream");
        request.setCookies(new MockCookie(StreamAuthCookieService.STREAM_TOKEN_COOKIE, "chat-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals("member@example.com", SecurityContextHolder.getContext().getAuthentication().getName());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(auth -> "ROLE_USER".equals(auth.getAuthority())));
        verify(userRepository).findByEmailIgnoreCase("member@example.com");
    }

    @Test
    void ignoresStreamCookieOutsideRealtimeChatEndpoints() throws Exception {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil, userRepository);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/payments/checkout-sessions/test");
        request.setCookies(new MockCookie(StreamAuthCookieService.STREAM_TOKEN_COOKIE, "chat-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verifyNoInteractions(jwtUtil, userRepository);
    }
}
