package com.clubportal.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "registration_email_verification", uniqueConstraints = {
        @UniqueConstraint(name = "uk_registration_email_verification_email", columnNames = "email")
})
public class RegistrationEmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Integer verificationId;

    @Column(name = "email", nullable = false, length = 120)
    private String email;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "verified_until")
    private Instant verifiedUntil;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Integer getVerificationId() { return verificationId; }
    public void setVerificationId(Integer verificationId) { this.verificationId = verificationId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getVerifiedUntil() { return verifiedUntil; }
    public void setVerifiedUntil(Instant verifiedUntil) { this.verifiedUntil = verifiedUntil; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
