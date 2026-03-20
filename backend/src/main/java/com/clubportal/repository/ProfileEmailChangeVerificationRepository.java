package com.clubportal.repository;

import com.clubportal.model.ProfileEmailChangeVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProfileEmailChangeVerificationRepository extends JpaRepository<ProfileEmailChangeVerification, Integer> {
    Optional<ProfileEmailChangeVerification> findByUserId(Integer userId);
    Optional<ProfileEmailChangeVerification> findByPendingEmailIgnoreCase(String pendingEmail);
    Optional<ProfileEmailChangeVerification> findByUserIdAndPendingEmailIgnoreCase(Integer userId, String pendingEmail);
    List<ProfileEmailChangeVerification> findByExpiresAtBefore(Instant expiresAt);
}
