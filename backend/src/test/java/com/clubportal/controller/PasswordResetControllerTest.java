package com.clubportal.controller;

import com.clubportal.config.PasswordPolicyProperties;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.PasswordResetService;
import com.clubportal.service.VerificationEmailSenderService;
import com.clubportal.util.PasswordEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordResetControllerTest {

    private static PasswordPolicyService passwordPolicyService() {
        return new PasswordPolicyService(new PasswordPolicyProperties());
    }

    @Test
    void requestPasswordResetUsesConfiguredBaseUrlInsteadOfRequestHeaders() {
        PasswordResetService passwordResetService = mock(PasswordResetService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);

        PasswordResetController controller = new PasswordResetController(
                passwordResetService,
                emailSenderService,
                userRepository,
                passwordUtil,
                passwordPolicyService()
        );
        ReflectionTestUtils.setField(controller, "publicBaseUrl", "https://www.club-portal.xyz");

        User user = new User();
        user.setUserId(8);
        user.setEmail("member@example.com");

        when(userRepository.findByEmailIgnoreCase("member@example.com")).thenReturn(Optional.of(user));
        when(passwordResetService.issueReset("member@example.com"))
                .thenReturn(PasswordResetService.IssueResetResult.sent("reset-token", Instant.parse("2026-04-01T12:00:00Z"), 60));
        when(emailSenderService.sendPasswordResetLink(any(), any(), any()))
                .thenReturn(VerificationEmailSenderService.SendEmailResult.ok());

        ResponseEntity<?> response = controller.requestPasswordReset(Map.of("email", "member@example.com"));

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertEquals("If an account exists for this email, a password reset link will be sent shortly.", body.get("message"));
        verify(emailSenderService).sendPasswordResetLink(
                org.mockito.ArgumentMatchers.eq("member@example.com"),
                org.mockito.ArgumentMatchers.eq("https://www.club-portal.xyz/reset-password.html?token=reset-token"),
                any()
        );
    }

    @Test
    void requestPasswordResetForUnknownEmailReturnsGenericSuccessWithoutIssuingToken() {
        PasswordResetService passwordResetService = mock(PasswordResetService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);

        PasswordResetController controller = new PasswordResetController(
                passwordResetService,
                emailSenderService,
                userRepository,
                passwordUtil,
                passwordPolicyService()
        );

        when(userRepository.findByEmailIgnoreCase("nobody@example.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.requestPasswordReset(Map.of("email", "nobody@example.com"));

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertTrue(String.valueOf(body.get("message")).contains("If an account exists"));
        verify(passwordResetService, never()).issueReset(any());
        verify(emailSenderService, never()).sendPasswordResetLink(any(), any(), any());
    }

    @Test
    void requestPasswordResetForRateLimitedKnownEmailStillReturnsGenericSuccess() {
        PasswordResetService passwordResetService = mock(PasswordResetService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);

        PasswordResetController controller = new PasswordResetController(
                passwordResetService,
                emailSenderService,
                userRepository,
                passwordUtil,
                passwordPolicyService()
        );

        User user = new User();
        user.setUserId(9);
        user.setEmail("member@example.com");

        when(userRepository.findByEmailIgnoreCase("member@example.com")).thenReturn(Optional.of(user));
        when(passwordResetService.issueReset("member@example.com"))
                .thenReturn(PasswordResetService.IssueResetResult.rateLimited(42));

        ResponseEntity<?> response = controller.requestPasswordReset(Map.of("email", "member@example.com"));

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(true, body.get("success"));
        assertTrue(String.valueOf(body.get("message")).contains("If an account exists"));
        verify(emailSenderService, never()).sendPasswordResetLink(any(), any(), any());
    }

    @Test
    void confirmPasswordResetRejectsWeakPasswordWithSharedPolicyMessage() {
        PasswordResetService passwordResetService = mock(PasswordResetService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);

        PasswordResetController controller = new PasswordResetController(
                passwordResetService,
                emailSenderService,
                userRepository,
                passwordUtil,
                passwordPolicyService()
        );

        ResponseEntity<?> response = controller.confirmPasswordReset(Map.of(
                "token", "reset-token",
                "password", "Aa1"
        ));

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Password must be at least 8 characters and include uppercase, lowercase, and a number",
                response.getBody()
        );
        verify(passwordResetService, never()).consumeToken(any());
    }
}
