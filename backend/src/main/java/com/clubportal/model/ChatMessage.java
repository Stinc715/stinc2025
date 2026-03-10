package com.clubportal.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "chat_message",
        indexes = {
                @Index(name = "idx_chat_message_club_user_created", columnList = "club_id,user_id,created_at"),
                @Index(name = "idx_chat_message_club_unread", columnList = "club_id,sender,read_by_club,created_at"),
                @Index(name = "idx_chat_message_user_unread", columnList = "user_id,sender,read_by_user,created_at")
        }
)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "club_id", nullable = false)
    private Integer clubId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    // Allowed values: USER, CLUB
    @Column(name = "sender", nullable = false, length = 10)
    private String sender;

    @Column(name = "message_text", nullable = false, length = 1000)
    private String messageText;

    @Column(name = "read_by_club", nullable = false)
    private boolean readByClub;

    @Column(name = "read_by_user", nullable = false)
    private boolean readByUser;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Integer getMessageId() { return messageId; }
    public void setMessageId(Integer messageId) { this.messageId = messageId; }
    public Integer getClubId() { return clubId; }
    public void setClubId(Integer clubId) { this.clubId = clubId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public boolean isReadByClub() { return readByClub; }
    public void setReadByClub(boolean readByClub) { this.readByClub = readByClub; }
    public boolean isReadByUser() { return readByUser; }
    public void setReadByUser(boolean readByUser) { this.readByUser = readByUser; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

