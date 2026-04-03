package com.clubportal.repository;

import com.clubportal.model.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Integer> {
    List<UserMembership> findByUserId(Integer userId);
    List<UserMembership> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<UserMembership> findByPlanIdInOrderByCreatedAtDesc(Collection<Integer> planIds);
    boolean existsByPlanId(Integer planId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from UserMembership m where m.userMembershipId = :id")
    Optional<UserMembership> findByIdForUpdate(@Param("id") Integer id);
}
