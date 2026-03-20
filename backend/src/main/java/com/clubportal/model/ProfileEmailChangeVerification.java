package com.clubportal.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "profile_email_change_verification", uniqueConstraints = {
        @UniqueConstraint(name = "uk_profile_email_change_user", columnNames = "user_id"),
        @UniqueConstraint(name = "uk_profile_email_change_email", columnNames = "pending_email")
})
public class ProfileEmailChangeVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Integer verificationId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "pending_email", nullable = false, length = 120)
    private String pendingEmail;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Integer getVerificationId() { return verificationId; }
    public void setVerificationId(Integer verificationId) { this.verificationId = verificationId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getPendingEmail() { return pendingEmail; }
    public void setPendingEmail(String pendingEmail) { this.pendingEmail = pendingEmail; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
