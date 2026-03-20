package com.clubportal.repository;

import com.clubportal.model.RegistrationEmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RegistrationEmailVerificationRepository extends JpaRepository<RegistrationEmailVerification, Integer> {
    Optional<RegistrationEmailVerification> findByEmailIgnoreCase(String email);
    List<RegistrationEmailVerification> findByExpiresAtBeforeAndVerifiedUntilBefore(Instant expiresAt, Instant verifiedUntil);
}
