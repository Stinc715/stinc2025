package com.clubportal.controller;

import com.clubportal.config.AuthLoginThrottleProperties;
import com.clubportal.config.PasswordPolicyProperties;
import com.clubportal.dto.RegisterRequest;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.service.LoginThrottleService;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.RegistrationEmailVerificationService;
import com.clubportal.service.SecurityEventService;
import com.clubportal.util.PasswordEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    private static PasswordPolicyService passwordPolicyService() {
        return new PasswordPolicyService(new PasswordPolicyProperties());
    }

    private static LoginThrottleService loginThrottleService() {
        return loginThrottleService(5);
    }

    private static LoginThrottleService loginThrottleService(int maxFailures) {
        AuthLoginThrottleProperties properties = new AuthLoginThrottleProperties();
        properties.setMaxFailures(maxFailures);
        properties.setWindowSeconds(900);
        properties.setBlockSeconds(900);
        return new LoginThrottleService(
                properties,
                Clock.fixed(Instant.parse("2026-04-03T08:00:00Z"), ZoneOffset.UTC)
        );
    }

    @Test
    void registerReturnsTokenAndWritesAuthAndRealtimeCookies() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RegistrationEmailVerificationService verificationService = mock(RegistrationEmailVerificationService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        AuthController controller = new AuthController(
                userRepository,
                passwordUtil,
                jwtUtil,
                verificationService,
                passwordPolicyService(),
                streamAuthCookieService,
                loginThrottleService(),
                securityEventService
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
        when(jwtUtil.generateToken("member@example.com", "user", 2, "password")).thenReturn("jwt-token");

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/register");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ResponseEntity<?> response = controller.register(request, servletRequest, servletResponse);

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("jwt-token", body.get("token"));
        assertEquals("member@example.com", body.get("email"));
        assertTrue(servletResponse.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains(StreamAuthCookieService.AUTH_TOKEN_COOKIE + "=jwt-token")));
        assertTrue(servletResponse.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains(StreamAuthCookieService.STREAM_TOKEN_COOKIE + "=jwt-token")));
        verify(verificationService).consumeVerification("member@example.com");
    }

    @Test
    void logoutClearsAuthCookies() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RegistrationEmailVerificationService verificationService = mock(RegistrationEmailVerificationService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        AuthController controller = new AuthController(
                userRepository,
                passwordUtil,
                jwtUtil,
                verificationService,
                passwordPolicyService(),
                streamAuthCookieService,
                loginThrottleService(),
                securityEventService
        );

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/auth/logout");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ResponseEntity<?> response = controller.logout(servletRequest, servletResponse);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(servletResponse.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains(StreamAuthCookieService.AUTH_TOKEN_COOKIE + "=")
                        && value.contains("Max-Age=0")));
        assertTrue(servletResponse.getHeaders("Set-Cookie").stream()
                .anyMatch(value -> value.contains(StreamAuthCookieService.STREAM_TOKEN_COOKIE + "=")
                        && value.contains("Max-Age=0")));
    }

    @Test
    void registerDoesNotLeakUnexpectedExceptionDetails() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RegistrationEmailVerificationService verificationService = mock(RegistrationEmailVerificationService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        AuthController controller = new AuthController(
                userRepository,
                passwordUtil,
                jwtUtil,
                verificationService,
                passwordPolicyService(),
                streamAuthCookieService,
                loginThrottleService(),
                securityEventService
        );

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Member");
        request.setEmail("member@example.com");
        request.setPassword("Password1");

        when(verificationService.isVerifiedForRegistration("member@example.com")).thenReturn(true);
        when(userRepository.findAllByEmailIgnoreCase("member@example.com")).thenReturn(List.of());
        when(passwordUtil.encodePassword("Password1")).thenReturn("encoded-password");
        doThrow(new RuntimeException("database exploded")).when(userRepository).save(any(User.class));

        MockHttpServletRequest servletRequest = new MockHttpServletRequest("POST", "/api/register");
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ResponseEntity<?> response = controller.register(request, servletRequest, servletResponse);

        assertEquals(500, response.getStatusCode().value());
        String body = String.valueOf(response.getBody());
        assertEquals("Registration failed. Please try again later.", body);
        assertFalse(body.contains("database exploded"));
    }

    @Test
    void registerRejectsWeakPasswordWithSharedPolicyMessage() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RegistrationEmailVerificationService verificationService = mock(RegistrationEmailVerificationService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        AuthController controller = new AuthController(
                userRepository,
                passwordUtil,
                jwtUtil,
                verificationService,
                passwordPolicyService(),
                streamAuthCookieService,
                loginThrottleService(),
                securityEventService
        );

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test Member");
        request.setEmail("member@example.com");
        request.setPassword("Aa1");

        when(verificationService.isVerifiedForRegistration("member@example.com")).thenReturn(true);

        ResponseEntity<?> response = controller.register(
                request,
                new MockHttpServletRequest("POST", "/api/register"),
                new MockHttpServletResponse()
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Password must be at least 8 characters and include uppercase, lowercase, and a number",
                response.getBody()
        );
    }

    @Test
    void loginThrottlesRepeatedFailuresByEmailAndIp() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        RegistrationEmailVerificationService verificationService = mock(RegistrationEmailVerificationService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        AuthController controller = new AuthController(
                userRepository,
                passwordUtil,
                jwtUtil,
                verificationService,
                passwordPolicyService(),
                streamAuthCookieService,
                loginThrottleService(2),
                securityEventService
        );

        when(userRepository.findAllByEmailIgnoreCase("member@example.com")).thenReturn(List.of());

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setRemoteAddr("203.0.113.8");
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<?> first = controller.login(Map.of("email", "member@example.com", "password", "WrongPass1"), request, response);
        ResponseEntity<?> second = controller.login(Map.of("email", "member@example.com", "password", "WrongPass1"), request, response);
        ResponseEntity<?> third = controller.login(Map.of("email", "member@example.com", "password", "WrongPass1"), request, response);

        assertEquals(401, first.getStatusCode().value());
        assertEquals(401, second.getStatusCode().value());
        assertEquals(429, third.getStatusCode().value());
        assertEquals("Too many login attempts. Please try again later.", third.getBody());
    }
}
