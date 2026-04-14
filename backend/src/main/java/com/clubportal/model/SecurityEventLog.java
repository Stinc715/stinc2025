package com.clubportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "security_event_log",
        indexes = {
                @Index(name = "idx_security_event_created", columnList = "created_at"),
                @Index(name = "idx_security_event_type_created", columnList = "event_type,created_at"),
                @Index(name = "idx_security_event_user_created", columnList = "user_id,created_at")
        }
)
public class SecurityEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Integer eventId;

    @Column(name = "event_type", nullable = false, length = 80)
    private String eventType;

    @Column(name = "severity", nullable = false, length = 16)
    private String severity;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "email_snapshot", length = 120)
    private String emailSnapshot;

    @Column(name = "source_ip", length = 64)
    private String sourceIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "request_method", length = 16)
    private String requestMethod;

    @Column(name = "request_path", length = 255)
    private String requestPath;

    @Lob
    @Column(name = "details_json")
    private String detailsJson;

    @Column(name = "alert_dispatched", nullable = false)
    private Boolean alertDispatched = Boolean.FALSE;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (alertDispatched == null) {
            alertDispatched = Boolean.FALSE;
        }
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getEmailSnapshot() {
        return emailSnapshot;
    }

    public void setEmailSnapshot(String emailSnapshot) {
        this.emailSnapshot = emailSnapshot;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getDetailsJson() {
        return detailsJson;
    }

    public void setDetailsJson(String detailsJson) {
        this.detailsJson = detailsJson;
    }

    public Boolean getAlertDispatched() {
        return alertDispatched;
    }

    public void setAlertDispatched(Boolean alertDispatched) {
        this.alertDispatched = alertDispatched;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
