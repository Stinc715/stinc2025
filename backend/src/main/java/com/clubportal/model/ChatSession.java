package com.clubportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_session",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_chat_session_club_user", columnNames = {"club_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_chat_session_club_mode_updated", columnList = "club_id,chat_mode,updated_at"),
                @Index(name = "idx_chat_session_user_mode_updated", columnList = "user_id,chat_mode,updated_at")
        }
)
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Integer sessionId;

    @Column(name = "club_id", nullable = false)
    private Integer clubId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "chat_mode", nullable = false, length = 30)
    private ChatMode chatMode = ChatMode.AI;

    @Column(name = "handoff_requested_at")
    private LocalDateTime handoffRequestedAt;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "handoff_reason", length = 50)
    private HandoffReason handoffReason;

    @Column(name = "club_unread_count", nullable = false)
    private Integer clubUnreadCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (chatMode == null) {
            chatMode = ChatMode.AI;
        }
        if (clubUnreadCount == null) {
            clubUnreadCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
        if (chatMode == null) {
            chatMode = ChatMode.AI;
        }
        if (clubUnreadCount == null) {
            clubUnreadCount = 0;
        }
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getClubId() {
        return clubId;
    }

    public void setClubId(Integer clubId) {
        this.clubId = clubId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public ChatMode getChatMode() {
        return chatMode;
    }

    public void setChatMode(ChatMode chatMode) {
        this.chatMode = chatMode;
    }

    public LocalDateTime getHandoffRequestedAt() {
        return handoffRequestedAt;
    }

    public void setHandoffRequestedAt(LocalDateTime handoffRequestedAt) {
        this.handoffRequestedAt = handoffRequestedAt;
    }

    public HandoffReason getHandoffReason() {
        return handoffReason;
    }

    public void setHandoffReason(HandoffReason handoffReason) {
        this.handoffReason = handoffReason;
    }

    public Integer getClubUnreadCount() {
        return clubUnreadCount;
    }

    public void setClubUnreadCount(Integer clubUnreadCount) {
        this.clubUnreadCount = clubUnreadCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
