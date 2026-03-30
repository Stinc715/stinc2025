package com.clubportal.service;

import com.clubportal.model.MembershipPlan;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.UserMembershipRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MembershipServiceTest {

    @Test
    void ensureStandardPlansForClubCreatesDisabledPlansByDefault() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        when(membershipPlanRepo.findByClubIdOrderByDurationDaysAsc(12)).thenReturn(List.of());
        when(membershipPlanRepo.save(any(MembershipPlan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MembershipService service = new MembershipService(membershipPlanRepo, userMembershipRepo);
        List<MembershipPlan> plans = service.ensureStandardPlansForClub(12);

        assertEquals(4, plans.size());
        assertTrue(plans.stream().allMatch(plan -> Boolean.FALSE.equals(plan.getEnabled())));
        assertEquals(List.of("MONTHLY", "QUARTERLY", "HALF_YEAR", "YEARLY"),
                plans.stream().map(MembershipPlan::getPlanCode).toList());
        verify(membershipPlanRepo, times(4)).save(any(MembershipPlan.class));
    }
}
