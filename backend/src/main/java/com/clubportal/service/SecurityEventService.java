package com.clubportal.service;

import com.clubportal.model.SecurityEventLog;
import com.clubportal.model.User;
import com.clubportal.repository.SecurityEventLogRepository;
import com.clubportal.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SecurityEventService {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventService.class);
    private static final String SEVERITY_INFO = "INFO";
    private static final String SEVERITY_WARN = "WARN";
    private static final String SEVERITY_HIGH = "HIGH";
    private static final String SEVERITY_CRITICAL = "CRITICAL";

    private final SecurityEventLogRepository securityEventLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @Value("${app.security.audit.enabled:true}")
    private boolean auditEnabled = true;

    @Value("${app.security.alerts.enabled:true}")
    private boolean alertsEnabled = true;

    @Value("${app.security.alerts.email-to:}")
    private String alertEmailTo;

    @Value("${app.public.base-url:}")
    private String publicBaseUrl;

    public SecurityEventService(SecurityEventLogRepository securityEventLogRepository,
                                UserRepository userRepository,
                                ObjectMapper objectMapper,
                                MailService mailService) {
        this.securityEventLogRepository = securityEventLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.mailService = mailService;
    }

    public void record(HttpServletRequest request,
                       String eventType,
                       String severity,
                       User actor,
                       Map<String, Object> details) {
        recordInternal(request, eventType, severity, actor, null, details);
    }

    public void recordForEmail(HttpServletRequest request,
                               String eventType,
                               String severity,
                               String actorEmail,
                               Map<String, Object> details) {
        recordInternal(request, eventType, severity, null, actorEmail, details);
    }

    private void recordInternal(HttpServletRequest request,
                                String eventType,
                                String severity,
                                User actor,
                                String actorEmail,
                                Map<String, Object> details) {
        try {
            SecurityEventLog event = new SecurityEventLog();
            event.setEventType(limit(normalizeEventType(eventType), 80));
            event.setSeverity(normalizeSeverity(severity));

            if (actor != null) {
                event.setUserId(actor.getUserId());
                event.setEmailSnapshot(limit(normalizeEmail(actor.getEmail()), 120));
            } else {
                String normalizedEmail = normalizeEmail(actorEmail);
                if (!normalizedEmail.isBlank()) {
                    event.setEmailSnapshot(limit(normalizedEmail, 120));
                    userRepository.findByEmailIgnoreCase(normalizedEmail)
                            .map(User::getUserId)
                            .ifPresent(event::setUserId);
                }
            }

            event.setSourceIp(limit(resolveClientIp(request), 64));
            event.setUserAgent(limit(readHeader(request, "User-Agent"), 255));
            event.setRequestMethod(limit(readMethod(request), 16));
            event.setRequestPath(limit(readPath(request), 255));
            event.setDetailsJson(serializeDetails(details));

            boolean alertDispatched = shouldAlert(event.getSeverity()) && dispatchAlert(event);
            event.setAlertDispatched(alertDispatched);

            logEvent(event);

            if (auditEnabled) {
                securityEventLogRepository.save(event);
            }
        } catch (Exception ex) {
            log.error("SECURITY_EVENT_WRITE_FAILED type={} severity={}", eventType, severity, ex);
        }
    }

    private void logEvent(SecurityEventLog event) {
        String message = "SECURITY_EVENT type={} severity={} userId={} email={} ip={} method={} path={} details={}";
        if (SEVERITY_CRITICAL.equals(event.getSeverity())) {
            log.error(message,
                    event.getEventType(),
                    event.getSeverity(),
                    event.getUserId(),
                    event.getEmailSnapshot(),
                    event.getSourceIp(),
                    event.getRequestMethod(),
                    event.getRequestPath(),
                    event.getDetailsJson());
            return;
        }
        if (SEVERITY_WARN.equals(event.getSeverity()) || SEVERITY_HIGH.equals(event.getSeverity())) {
            log.warn(message,
                    event.getEventType(),
                    event.getSeverity(),
                    event.getUserId(),
                    event.getEmailSnapshot(),
                    event.getSourceIp(),
                    event.getRequestMethod(),
                    event.getRequestPath(),
                    event.getDetailsJson());
            return;
        }
        log.info(message,
                event.getEventType(),
                event.getSeverity(),
                event.getUserId(),
                event.getEmailSnapshot(),
                event.getSourceIp(),
                event.getRequestMethod(),
                event.getRequestPath(),
                event.getDetailsJson());
    }

    private boolean dispatchAlert(SecurityEventLog event) {
        if (!alertsEnabled) {
            return false;
        }
        String recipient = limit(safe(alertEmailTo), 120);
        if (recipient.isBlank()) {
            return false;
        }
        String subject = "Club Booking Portal security alert: " + event.getEventType();
        String body = buildAlertBody(event);
        try {
            mailService.sendPlainText(recipient, subject, body);
            log.warn("SECURITY_ALERT_DISPATCHED type={} severity={} recipient={}",
                    event.getEventType(),
                    event.getSeverity(),
                    recipient);
            return true;
        } catch (Exception ex) {
            log.error("SECURITY_ALERT_DISPATCH_FAILED type={} severity={} recipient={}",
                    event.getEventType(),
                    event.getSeverity(),
                    recipient,
                    ex);
            return false;
        }
    }

    private String buildAlertBody(SecurityEventLog event) {
        String baseUrl = safe(publicBaseUrl);
        return """
                A high-severity security event was recorded by Club Booking Portal.

                Event type: %s
                Severity: %s
                User ID: %s
                Email: %s
                Source IP: %s
                Method: %s
                Path: %s
                Details: %s

                Review the application logs and security_event_log table for follow-up.
                Deployment URL: %s
                """.formatted(
                safe(event.getEventType()),
                safe(event.getSeverity()),
                event.getUserId() == null ? "" : event.getUserId(),
                safe(event.getEmailSnapshot()),
                safe(event.getSourceIp()),
                safe(event.getRequestMethod()),
                safe(event.getRequestPath()),
                safe(event.getDetailsJson()),
                baseUrl
        );
    }

    private String serializeDetails(Map<String, Object> details) {
        Map<String, Object> payload = new LinkedHashMap<>();
        if (details != null) {
            details.forEach((key, value) -> {
                String normalizedKey = safe(key);
                if (!normalizedKey.isBlank()) {
                    payload.put(normalizedKey, value);
                }
            });
        }
        if (payload.isEmpty()) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            log.warn("SECURITY_EVENT_DETAILS_SERIALIZATION_FAILED", ex);
            return limit(payload.toString(), 4000);
        }
    }

    private static boolean shouldAlert(String severity) {
        return SEVERITY_HIGH.equals(severity) || SEVERITY_CRITICAL.equals(severity);
    }

    private static String normalizeEventType(String eventType) {
        String normalized = safe(eventType).toUpperCase().replaceAll("[^A-Z0-9_]+", "_");
        return normalized.isBlank() ? "UNKNOWN_EVENT" : normalized;
    }

    private static String normalizeSeverity(String severity) {
        String normalized = safe(severity).toUpperCase();
        return switch (normalized) {
            case SEVERITY_WARN, SEVERITY_HIGH, SEVERITY_CRITICAL -> normalized;
            default -> SEVERITY_INFO;
        };
    }

    private static String normalizeEmail(String email) {
        return safe(email).toLowerCase();
    }

    private static String readHeader(HttpServletRequest request, String name) {
        return request == null ? "" : safe(request.getHeader(name));
    }

    private static String readMethod(HttpServletRequest request) {
        return request == null ? "" : safe(request.getMethod());
    }

    private static String readPath(HttpServletRequest request) {
        return request == null ? "" : safe(request.getRequestURI());
    }

    private static String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwardedFor = safe(request.getHeader("X-Forwarded-For"));
        if (!forwardedFor.isBlank()) {
            return limit(forwardedFor.split(",")[0].trim(), 64);
        }
        return safe(request.getRemoteAddr());
    }

    private static String limit(String value, int maxLength) {
        String normalized = safe(value);
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength);
    }

    private static String safe(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
