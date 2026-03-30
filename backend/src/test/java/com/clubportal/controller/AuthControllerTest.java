package com.clubportal.controller;

import com.clubportal.dto.RegisterRequest;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.service.RegistrationEmailVerificationService;
import com.clubportal.util.PasswordEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void registerReturnsTokenAndWritesRealtimeCookie() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RegistrationEmailVerificationService verificationService = mock(RegistrationEmailVerificationService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        AuthController controller = new AuthController(
                userRepository,
                passwordUtil,
                jwtUtil,
                verificationService,
                streamAuthCookieService
        );

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Member");
        request.setEmail("member@example.com");
        request.setPassword("Password1");

        User saved = new User();
        saved.setUserId(42);
        saved.setUsername("Test Member");
        saved.setEmail("member@example.com");
        saved.setRole(User.Role.USER);
        saved.setSessionVersion(2);

        when(verificationService.isVerifiedForRegistration("member@example.com")).thenReturn(true);
        when(userRepository.findAllByEmailIgnoreCase("member@example.com")).thenReturn(List.of());
        when(passwordUtil.encodePassword("Password1")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtUtil.generateToken("member@example.com", "user", 2)).thenReturn("jwt-token");

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/register");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ResponseEntity<?> response = controller.register(request, servletRequest, servletResponse);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("jwt-token", body.get("token"));
        assertEquals("member@example.com", body.get("email"));
        assertTrue(servletResponse.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains(StreamAuthCookieService.STREAM_TOKEN_COOKIE + "=jwt-token")));
        verify(verificationService).consumeVerification("member@example.com");
    }
}
