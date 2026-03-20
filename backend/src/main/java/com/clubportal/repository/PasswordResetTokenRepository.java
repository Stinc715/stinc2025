package com.clubportal.repository;

import com.clubportal.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByEmailIgnoreCase(String email);
    Optional<PasswordResetToken> findByResetToken(String resetToken);
    List<PasswordResetToken> findByExpiresAtBefore(Instant expiresAt);
}
