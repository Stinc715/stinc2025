package com.clubportal.repository;

import com.clubportal.model.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Integer> {
    List<MembershipPlan> findByClubId(Integer clubId);
    List<MembershipPlan> findByClubIdOrderByDurationDaysAsc(Integer clubId);
    List<MembershipPlan> findByClubIdAndEnabledTrueOrderByDurationDaysAsc(Integer clubId);
    Optional<MembershipPlan> findFirstByClubIdAndPlanCodeIgnoreCase(Integer clubId, String planCode);
}
