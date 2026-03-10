package com.clubportal.repository;

import com.clubportal.model.ClubAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClubAdminRepository extends JpaRepository<ClubAdmin, Integer> {
    boolean existsByUserIdAndClubId(Integer userId, Integer clubId);
    List<ClubAdmin> findByUserId(Integer userId);
}
