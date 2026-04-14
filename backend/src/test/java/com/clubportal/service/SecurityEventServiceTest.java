package com.clubportal.service;

import com.clubportal.model.SecurityEventLog;
import com.clubportal.model.User;
import com.clubportal.repository.SecurityEventLogRepository;
import com.clubportal.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityEventServiceTest {

    @Test
    void recordsAuditEventsWithSerializedRequestContext() {
        SecurityEventLogRepository securityEventLogRepository = mock(SecurityEventLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MailService mailService = mock(MailService.class);
        SecurityEventService service = new SecurityEventService(
                securityEventLogRepository,
                userRepository,
                new ObjectMapper(),
                mailService
        );
        ReflectionTestUtils.setField(service, "auditEnabled", true);
        ReflectionTestUtils.setField(service, "alertsEnabled", false);

        User user = new User();
        user.setUserId(11);
        user.setEmail("member@example.com");
        when(userRepository.findByEmailIgnoreCase("member@example.com")).thenReturn(Optional.of(user));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/profile/export");
        request.addHeader("X-Forwarded-For", "203.0.113.10, 10.0.0.1");
        request.addHeader("User-Agent", "JUnit Browser");

        service.recordForEmail(request, "profile_export_requested", "info", "member@example.com", Map.of(
                "scope", "account"
        ));

        verify(securityEventLogRepository).save(any(SecurityEventLog.class));
    }

    @Test
    void highSeverityEventsCanDispatchAlerts() {
        SecurityEventLogRepository securityEventLogRepository = mock(SecurityEventLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MailService mailService = mock(MailService.class);
        SecurityEventService service = new SecurityEventService(
                securityEventLogRepository,
                userRepository,
                new ObjectMapper(),
                mailService
        );
        ReflectionTestUtils.setField(service, "auditEnabled", true);
        ReflectionTestUtils.setField(service, "alertsEnabled", true);
        ReflectionTestUtils.setField(service, "alertEmailTo", "security@example.com");
        ReflectionTestUtils.setField(service, "publicBaseUrl", "https://club-portal.xyz");

        service.recordForEmail(
                new MockHttpServletRequest("POST", "/api/login"),
                "login_blocked",
                "HIGH",
                "blocked@example.com",
                Map.of("reason", "throttle_limit_reached")
        );

        verify(mailService).sendPlainText(any(), any(), any());
        verify(securityEventLogRepository).save(any(SecurityEventLog.class));
    }

    @Test
    void unknownSeverityFallsBackToInfo() {
        SecurityEventLogRepository securityEventLogRepository = mock(SecurityEventLogRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        MailService mailService = mock(MailService.class);
        SecurityEventService service = new SecurityEventService(
                securityEventLogRepository,
                userRepository,
                new ObjectMapper(),
                mailService
        );
        ReflectionTestUtils.setField(service, "auditEnabled", true);
        ReflectionTestUtils.setField(service, "alertsEnabled", false);

        SecurityEventLog log = new SecurityEventLog();
        when(securityEventLogRepository.save(any(SecurityEventLog.class))).thenAnswer(invocation -> {
            SecurityEventLog saved = invocation.getArgument(0);
            log.setSeverity(saved.getSeverity());
            log.setEventType(saved.getEventType());
            return saved;
        });

        service.recordForEmail(new MockHttpServletRequest("GET", "/api/test"), "strange event", "custom", "", Map.of());

        assertEquals("INFO", log.getSeverity());
        assertEquals("STRANGE_EVENT", log.getEventType());
        assertFalse(Boolean.TRUE.equals(log.getAlertDispatched()));
    }
}
