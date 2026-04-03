package com.clubportal.controller;

import com.clubportal.config.PasswordPolicyProperties;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.ProfileEmailVerificationService;
import com.clubportal.service.UserAvatarService;
import com.clubportal.service.VerificationEmailSenderService;
import com.clubportal.util.PasswordEncryptionUtil;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProfileControllerTest {

    @Test
    void updatePasswordRejectsWeakPasswordWithSharedPolicyMessage() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        ProfileEmailVerificationService profileEmailVerificationService = mock(ProfileEmailVerificationService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();
        PasswordPolicyService passwordPolicyService = new PasswordPolicyService(new PasswordPolicyProperties());

        User me = new User();
        me.setUserId(11);
        me.setEmail("member@example.com");
        me.setPasswordHash("encoded-password");

        when(currentUserService.requireUser()).thenReturn(me);

        ProfileController controller = new ProfileController(
                currentUserService,
                userRepository,
                jwtUtil,
                passwordUtil,
                passwordPolicyService,
                profileEmailVerificationService,
                emailSenderService,
                userAvatarService,
                streamAuthCookieService
        );

        ResponseEntity<?> response = controller.updatePassword(
                Map.of(
                        "currentPassword", "OldPassword1",
                        "newPassword", "Aa1"
                ),
                new MockHttpServletRequest("PATCH", "/api/profile/password"),
                new MockHttpServletResponse()
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(
                "Password must be at least 8 characters and include uppercase, lowercase, and a number",
                response.getBody()
        );
    }

    @Test
    void getProfileExposesGoogleProviderAndPasswordCapability() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        ProfileEmailVerificationService profileEmailVerificationService = mock(ProfileEmailVerificationService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();
        PasswordPolicyService passwordPolicyService = new PasswordPolicyService(new PasswordPolicyProperties());

        User me = new User();
        me.setUserId(11);
        me.setUsername("Member");
        me.setEmail("member@example.com");

        when(currentUserService.requireUser()).thenReturn(me);
        when(jwtUtil.extractAuthProvider("google-token")).thenReturn("google");
        when(jwtUtil.generateToken("member@example.com", "user", 1, "google")).thenReturn("rotated-google-token");

        ProfileController controller = new ProfileController(
                currentUserService,
                userRepository,
                jwtUtil,
                passwordUtil,
                passwordPolicyService,
                profileEmailVerificationService,
                emailSenderService,
                userAvatarService,
                streamAuthCookieService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/profile");
        request.setCookies(new MockCookie(StreamAuthCookieService.AUTH_TOKEN_COOKIE, "google-token"));
        ResponseEntity<?> response = controller.getProfile(request, new MockHttpServletResponse());

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("google", body.get("authProvider"));
        assertEquals(Boolean.FALSE, body.get("canChangePassword"));
    }

    @Test
    void updatePasswordRejectsGoogleSignInSessions() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        ProfileEmailVerificationService profileEmailVerificationService = mock(ProfileEmailVerificationService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();
        PasswordPolicyService passwordPolicyService = new PasswordPolicyService(new PasswordPolicyProperties());

        User me = new User();
        me.setUserId(11);
        me.setEmail("member@example.com");

        when(currentUserService.requireUser()).thenReturn(me);
        when(jwtUtil.extractAuthProvider("google-token")).thenReturn("google");

        ProfileController controller = new ProfileController(
                currentUserService,
                userRepository,
                jwtUtil,
                passwordUtil,
                passwordPolicyService,
                profileEmailVerificationService,
                emailSenderService,
                userAvatarService,
                streamAuthCookieService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("PATCH", "/api/profile/password");
        request.setCookies(new MockCookie(StreamAuthCookieService.AUTH_TOKEN_COOKIE, "google-token"));

        ResponseEntity<?> response = controller.updatePassword(
                Map.of(
                        "currentPassword", "OldPassword1",
                        "newPassword", "NewPassword1"
                ),
                request,
                new MockHttpServletResponse()
        );

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Google sign-in accounts cannot change password here", response.getBody());
    }
}
