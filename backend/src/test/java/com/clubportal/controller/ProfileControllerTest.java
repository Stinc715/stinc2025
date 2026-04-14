package com.clubportal.controller;

import com.clubportal.config.PasswordPolicyProperties;
import com.clubportal.model.ProfileDeletionRequest;
import com.clubportal.model.User;
import com.clubportal.repository.UserRepository;
import com.clubportal.security.JwtUtil;
import com.clubportal.security.StreamAuthCookieService;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.PasswordPolicyService;
import com.clubportal.service.ProfileDataRightsService;
import com.clubportal.service.ProfileEmailVerificationService;
import com.clubportal.service.SecurityEventService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        ProfileDataRightsService profileDataRightsService = mock(ProfileDataRightsService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
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
                streamAuthCookieService,
                profileDataRightsService,
                securityEventService
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
        ProfileDataRightsService profileDataRightsService = mock(ProfileDataRightsService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
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
                streamAuthCookieService,
                profileDataRightsService,
                securityEventService
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
        ProfileDataRightsService profileDataRightsService = mock(ProfileDataRightsService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
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
                streamAuthCookieService,
                profileDataRightsService,
                securityEventService
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

    @Test
    void exportProfileDataReturnsServicePayload() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        ProfileEmailVerificationService profileEmailVerificationService = mock(ProfileEmailVerificationService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);
        ProfileDataRightsService profileDataRightsService = mock(ProfileDataRightsService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();
        PasswordPolicyService passwordPolicyService = new PasswordPolicyService(new PasswordPolicyProperties());

        User me = new User();
        me.setUserId(11);
        me.setEmail("member@example.com");

        when(currentUserService.requireUser()).thenReturn(me);
        when(profileDataRightsService.buildExport(me)).thenReturn(Map.of("generatedAt", "2026-04-13T00:00:00Z"));

        ProfileController controller = new ProfileController(
                currentUserService,
                userRepository,
                jwtUtil,
                passwordUtil,
                passwordPolicyService,
                profileEmailVerificationService,
                emailSenderService,
                userAvatarService,
                streamAuthCookieService,
                profileDataRightsService,
                securityEventService
        );

        ResponseEntity<?> response = controller.exportProfileData(new MockHttpServletRequest("GET", "/api/profile/export"));

        assertEquals(200, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("2026-04-13T00:00:00Z", body.get("generatedAt"));
    }

    @Test
    void createDeletionRequestReturnsAcceptedPayload() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        ProfileEmailVerificationService profileEmailVerificationService = mock(ProfileEmailVerificationService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);
        ProfileDataRightsService profileDataRightsService = mock(ProfileDataRightsService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();
        PasswordPolicyService passwordPolicyService = new PasswordPolicyService(new PasswordPolicyProperties());

        User me = new User();
        me.setUserId(11);
        me.setEmail("member@example.com");

        ProfileDeletionRequest request = new ProfileDeletionRequest();
        request.setRequestId(7);
        request.setStatus("PENDING");

        when(currentUserService.requireUser()).thenReturn(me);
        when(profileDataRightsService.submitDeletionRequest(me, "Please delete my account"))
                .thenReturn(new ProfileDataRightsService.DeletionRequestSubmission(request, true));
        when(profileDataRightsService.toDeletionRequestPayload(request))
                .thenReturn(Map.of("requestId", 7, "status", "PENDING"));

        ProfileController controller = new ProfileController(
                currentUserService,
                userRepository,
                jwtUtil,
                passwordUtil,
                passwordPolicyService,
                profileEmailVerificationService,
                emailSenderService,
                userAvatarService,
                streamAuthCookieService,
                profileDataRightsService,
                securityEventService
        );

        ResponseEntity<?> response = controller.createDeletionRequest(
                Map.of("reason", "Please delete my account"),
                new MockHttpServletRequest("POST", "/api/profile/deletion-request")
        );

        assertEquals(202, response.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(Boolean.TRUE, body.get("submitted"));
        assertEquals(Boolean.TRUE, body.get("created"));
        assertTrue(String.valueOf(body.get("message")).contains("manual review"));
        @SuppressWarnings("unchecked")
        Map<String, Object> deletionRequest = (Map<String, Object>) body.get("deletionRequest");
        assertEquals(7, deletionRequest.get("requestId"));
        assertEquals("PENDING", deletionRequest.get("status"));
    }

    @Test
    void rotateSessionReturnsFreshTokenAndConfiguredTtls() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtUtil jwtUtil = mock(JwtUtil.class);
        PasswordEncryptionUtil passwordUtil = mock(PasswordEncryptionUtil.class);
        ProfileEmailVerificationService profileEmailVerificationService = mock(ProfileEmailVerificationService.class);
        VerificationEmailSenderService emailSenderService = mock(VerificationEmailSenderService.class);
        UserAvatarService userAvatarService = mock(UserAvatarService.class);
        ProfileDataRightsService profileDataRightsService = mock(ProfileDataRightsService.class);
        SecurityEventService securityEventService = mock(SecurityEventService.class);
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();
        PasswordPolicyService passwordPolicyService = new PasswordPolicyService(new PasswordPolicyProperties());

        User me = new User();
        me.setUserId(11);
        me.setUsername("Member");
        me.setEmail("member@example.com");

        when(currentUserService.requireUser()).thenReturn(me);
        when(userRepository.save(me)).thenReturn(me);
        when(jwtUtil.extractAuthProvider("password-token")).thenReturn("password");
        when(jwtUtil.generateToken("member@example.com", "user", 2, "password")).thenReturn("rotated-token");

        ProfileController controller = new ProfileController(
                currentUserService,
                userRepository,
                jwtUtil,
                passwordUtil,
                passwordPolicyService,
                profileEmailVerificationService,
                emailSenderService,
                userAvatarService,
                streamAuthCookieService,
                profileDataRightsService,
                securityEventService
        );

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/profile/session/rotate");
        request.setCookies(new MockCookie(StreamAuthCookieService.AUTH_TOKEN_COOKIE, "password-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        ResponseEntity<?> entity = controller.rotateSession(request, response);

        assertEquals(200, entity.getStatusCode().value());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) entity.getBody();
        assertEquals(Boolean.TRUE, body.get("rotated"));
        assertEquals("rotated-token", body.get("token"));
        assertEquals(604800L, body.get("authTokenTtlSeconds"));
        assertEquals(86400L, body.get("streamTokenTtlSeconds"));
    }
}
