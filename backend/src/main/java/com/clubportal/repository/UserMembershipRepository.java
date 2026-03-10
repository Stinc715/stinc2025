package com.clubportal.repository;

import com.clubportal.model.UserMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface UserMembershipRepository extends JpaRepository<UserMembership, Integer> {
    List<UserMembership> findByUserId(Integer userId);
    List<UserMembership> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<UserMembership> findByPlanIdInOrderByCreatedAtDesc(Collection<Integer> planIds);
}
