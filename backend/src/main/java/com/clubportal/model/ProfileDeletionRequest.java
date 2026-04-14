package com.clubportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "profile_deletion_request",
        indexes = {
                @Index(name = "idx_profile_deletion_request_user_status", columnList = "user_id,status,requested_at")
        }
)
public class ProfileDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Integer requestId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "email_snapshot", nullable = false, length = 120)
    private String emailSnapshot;

    @Column(name = "display_name_snapshot", nullable = false, length = 120)
    private String displayNameSnapshot;

    @Column(name = "role_snapshot", nullable = false, length = 20)
    private String roleSnapshot;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_note", length = 500)
    private String resolutionNote;

    @PrePersist
    public void prePersist() {
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
        if (status == null || status.isBlank()) {
            status = "PENDING";
        }
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
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

    public String getDisplayNameSnapshot() {
        return displayNameSnapshot;
    }

    public void setDisplayNameSnapshot(String displayNameSnapshot) {
        this.displayNameSnapshot = displayNameSnapshot;
    }

    public String getRoleSnapshot() {
        return roleSnapshot;
    }

    public void setRoleSnapshot(String roleSnapshot) {
        this.roleSnapshot = roleSnapshot;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionNote() {
        return resolutionNote;
    }

    public void setResolutionNote(String resolutionNote) {
        this.resolutionNote = resolutionNote;
    }
}
