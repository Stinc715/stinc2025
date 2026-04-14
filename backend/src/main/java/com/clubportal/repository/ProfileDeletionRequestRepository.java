package com.clubportal.repository;

import com.clubportal.model.ProfileDeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileDeletionRequestRepository extends JpaRepository<ProfileDeletionRequest, Integer> {
    Optional<ProfileDeletionRequest> findFirstByUserIdAndStatusOrderByRequestedAtDesc(Integer userId, String status);
    List<ProfileDeletionRequest> findByUserIdOrderByRequestedAtDesc(Integer userId);
}
