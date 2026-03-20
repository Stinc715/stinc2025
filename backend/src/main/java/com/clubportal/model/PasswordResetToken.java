package com.clubportal.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "password_reset_token", uniqueConstraints = {
        @UniqueConstraint(name = "uk_password_reset_token_token", columnNames = "reset_token"),
        @UniqueConstraint(name = "uk_password_reset_token_email", columnNames = "email")
})
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "password_reset_id")
    private Integer passwordResetId;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "reset_token", nullable = false, length = 255)
    private String resetToken;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Integer getPasswordResetId() { return passwordResetId; }
    public void setPasswordResetId(Integer passwordResetId) { this.passwordResetId = passwordResetId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
