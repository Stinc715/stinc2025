package com.clubportal.service;

import com.clubportal.model.MembershipPlan;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.UserMembershipRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class MembershipServiceTest {

    @Test
    void catalogPlansHideLegacyStandardPlans() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        MembershipService service = new MembershipService(membershipPlanRepo, userMembershipRepo);

        MembershipPlan standard = new MembershipPlan();
        standard.setPlanCode("MONTHLY");
        standard.setDurationDays(30);
        standard.setEnabled(true);

        MembershipPlan custom = new MembershipPlan();
        custom.setPlanId(91);
        custom.setPlanCode("CUSTOM_ABC123");
        custom.setPlanName("Drop-in Saver");
        custom.setDurationDays(45);
        custom.setEnabled(true);

        List<MembershipPlan> plans = service.catalogPlans(List.of(standard, custom));

        assertEquals(1, plans.size());
        assertEquals("CUSTOM_ABC123", plans.get(0).getPlanCode());
    }

    @Test
    void bookingPackMembershipWithNoCreditsIsExhausted() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        MembershipService service = new MembershipService(membershipPlanRepo, userMembershipRepo);

        MembershipPlan plan = new MembershipPlan();
        plan.setBenefitType(MembershipService.BENEFIT_BOOKING_PACK);
        plan.setIncludedBookings(30);
        plan.setDiscountPercent(BigDecimal.ZERO);

        UserMembership membership = new UserMembership();
        membership.setStatus("ACTIVE");
        membership.setStartDate(LocalDate.of(2026, 4, 1));
        membership.setEndDate(LocalDate.of(2026, 4, 30));
        membership.setRemainingBookings(0);

        assertEquals("EXHAUSTED", service.effectiveStatus(membership, plan, LocalDate.of(2026, 4, 2)));
    }
}
