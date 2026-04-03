package com.clubportal.controller;

import com.clubportal.dto.MyMembershipResponse;
import com.clubportal.dto.MembershipPlanResponse;
import com.clubportal.dto.MembershipPlanUpsertRequest;
import com.clubportal.model.CheckoutSession;
import com.clubportal.model.Club;
import com.clubportal.model.MembershipPlan;
import com.clubportal.model.User;
import com.clubportal.model.UserMembership;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.CheckoutSessionRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.TransactionRecordRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.MembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MembershipControllerTest {

    @Test
    void listPublicMembershipPlansOnlyReturnsClubAddedPlans() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        TransactionRecordRepository transactionRecordRepo = mock(TransactionRecordRepository.class);
        CheckoutSessionRepository checkoutSessionRepo = mock(CheckoutSessionRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MembershipService membershipService = new MembershipService(membershipPlanRepo, userMembershipRepo);

        when(clubRepo.existsById(24)).thenReturn(true);
        MembershipPlan standard = new MembershipPlan();
        standard.setPlanId(11);
        standard.setClubId(24);
        standard.setPlanCode("MONTHLY");
        standard.setPlanName("Monthly Pass");
        standard.setDurationDays(30);
        standard.setEnabled(true);

        MembershipPlan custom = new MembershipPlan();
        custom.setPlanId(12);
        custom.setClubId(24);
        custom.setPlanCode("CUSTOM_PACK");
        custom.setPlanName("30-visit pack");
        custom.setDurationDays(180);
        custom.setEnabled(true);

        when(membershipPlanRepo.findByClubIdAndEnabledTrueOrderByDurationDaysAsc(24)).thenReturn(List.of(standard, custom));

        MembershipController controller = new MembershipController(
                membershipPlanRepo,
                userMembershipRepo,
                transactionRecordRepo,
                checkoutSessionRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                membershipService
        );

        ResponseEntity<?> response = controller.listPublicMembershipPlans(24);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        @SuppressWarnings("unchecked")
        List<MembershipPlanResponse> rows = (List<MembershipPlanResponse>) response.getBody();
        assertEquals(1, rows.size());
        assertEquals("CUSTOM_PACK", rows.get(0).planCode());
        assertEquals("30-visit pack", rows.get(0).planName());
    }

    @Test
    void listClubMembershipPlansOnlyReturnsClubAddedPlans() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        TransactionRecordRepository transactionRecordRepo = mock(TransactionRecordRepository.class);
        CheckoutSessionRepository checkoutSessionRepo = mock(CheckoutSessionRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MembershipService membershipService = new MembershipService(membershipPlanRepo, userMembershipRepo);

        MembershipController controller = new MembershipController(
                membershipPlanRepo,
                userMembershipRepo,
                transactionRecordRepo,
                checkoutSessionRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                membershipService
        );

        User clubUser = new User();
        clubUser.setUserId(10);
        clubUser.setRole(User.Role.CLUB);

        MembershipPlan standard = new MembershipPlan();
        standard.setPlanId(11);
        standard.setClubId(24);
        standard.setPlanCode("YEARLY");
        standard.setPlanName("Yearly Pass");
        standard.setDurationDays(365);
        standard.setEnabled(true);

        MembershipPlan custom = new MembershipPlan();
        custom.setPlanId(12);
        custom.setClubId(24);
        custom.setPlanCode("CUSTOM_MONTH");
        custom.setPlanName("Flex pass");
        custom.setDurationDays(30);
        custom.setEnabled(true);

        when(clubRepo.existsById(24)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(clubUser);
        when(clubAdminRepo.existsByUserIdAndClubId(10, 24)).thenReturn(true);
        when(membershipPlanRepo.findByClubIdOrderByDurationDaysAsc(24)).thenReturn(List.of(standard, custom));

        ResponseEntity<?> response = controller.listClubMembershipPlans(24);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        @SuppressWarnings("unchecked")
        List<MembershipPlanResponse> rows = (List<MembershipPlanResponse>) response.getBody();
        assertEquals(1, rows.size());
        assertEquals("CUSTOM_MONTH", rows.get(0).planCode());
        assertEquals(Boolean.FALSE, rows.get(0).standardPlan());
    }

    @Test
    void listMyMembershipsIncludesOrderNo() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        TransactionRecordRepository transactionRecordRepo = mock(TransactionRecordRepository.class);
        CheckoutSessionRepository checkoutSessionRepo = mock(CheckoutSessionRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MembershipService membershipService = mock(MembershipService.class);

        MembershipController controller = new MembershipController(
                membershipPlanRepo,
                userMembershipRepo,
                transactionRecordRepo,
                checkoutSessionRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                membershipService
        );

        User me = new User();
        me.setUserId(6);
        me.setRole(User.Role.USER);

        UserMembership membership = new UserMembership();
        membership.setUserMembershipId(55);
        membership.setUserId(6);
        membership.setPlanId(9);
        membership.setStartDate(LocalDate.of(2026, 3, 31));
        membership.setEndDate(LocalDate.of(2026, 4, 30));
        membership.setStatus("ACTIVE");
        membership.setCreatedAt(LocalDateTime.of(2026, 3, 31, 10, 15));

        MembershipPlan plan = new MembershipPlan();
        plan.setPlanId(9);
        plan.setClubId(2);
        plan.setPlanCode("MONTHLY");
        plan.setPlanName("Monthly Pass");
        plan.setPrice(new BigDecimal("49.00"));
        plan.setDiscountPercent(new BigDecimal("20"));

        Club club = new Club();
        club.setClubId(2);
        club.setClubName("manba basketball");

        CheckoutSession session = new CheckoutSession();
        session.setUserMembershipId(55);
        session.setOrderNo("MB-20260331-101500-AB12CD");

        when(currentUserService.requireUser()).thenReturn(me);
        when(userMembershipRepo.findByUserIdOrderByCreatedAtDesc(6)).thenReturn(List.of(membership));
        when(membershipPlanRepo.findAllById(java.util.Set.of(9))).thenReturn(List.of(plan));
        when(clubRepo.findAllById(java.util.Set.of(2))).thenReturn(List.of(club));
        when(checkoutSessionRepo.findByUserMembershipIdInAndStatusOrderByCreatedAtDesc(java.util.Set.of(55), "PAID"))
                .thenReturn(List.of(session));
        when(membershipService.normalizePrice(plan.getPrice())).thenReturn(new BigDecimal("49.00"));
        when(membershipService.normalizeDiscount(plan.getDiscountPercent())).thenReturn(new BigDecimal("20.00"));
        when(membershipService.normalizeBenefitType(plan.getBenefitType())).thenReturn(MembershipService.BENEFIT_DISCOUNT);
        when(membershipService.normalizeIncludedBookings(plan.getIncludedBookings())).thenReturn(0);
        when(membershipService.normalizeRemainingBookings(membership.getRemainingBookings())).thenReturn(0);
        when(membershipService.effectiveStatus(eq(membership), eq(plan), any(LocalDate.class))).thenReturn("ACTIVE");

        ResponseEntity<?> response = controller.listMyMemberships();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        @SuppressWarnings("unchecked")
        List<MyMembershipResponse> rows = (List<MyMembershipResponse>) response.getBody();
        assertEquals(1, rows.size());
        assertEquals("MB-20260331-101500-AB12CD", rows.get(0).orderNo());
    }

    @Test
    void createCustomMembershipPlanSupportsBookingPackBenefits() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        TransactionRecordRepository transactionRecordRepo = mock(TransactionRecordRepository.class);
        CheckoutSessionRepository checkoutSessionRepo = mock(CheckoutSessionRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MembershipService membershipService = new MembershipService(membershipPlanRepo, userMembershipRepo);

        MembershipController controller = new MembershipController(
                membershipPlanRepo,
                userMembershipRepo,
                transactionRecordRepo,
                checkoutSessionRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                membershipService
        );

        User clubUser = new User();
        clubUser.setUserId(10);
        clubUser.setRole(User.Role.CLUB);

        MembershipPlanUpsertRequest request = new MembershipPlanUpsertRequest();
        request.setPlanName("30-visit pack");
        request.setPrice(new BigDecimal("45"));
        request.setDurationDays(180);
        request.setBenefitType(MembershipService.BENEFIT_BOOKING_PACK);
        request.setIncludedBookings(30);
        request.setEnabled(true);
        request.setDescription("Includes 30 prepaid bookings for this club.");

        when(clubRepo.existsById(2)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(clubUser);
        when(clubAdminRepo.existsByUserIdAndClubId(10, 2)).thenReturn(true);
        when(membershipPlanRepo.save(any(MembershipPlan.class))).thenAnswer(invocation -> {
            MembershipPlan saved = invocation.getArgument(0);
            saved.setPlanId(91);
            return saved;
        });

        ResponseEntity<?> response = controller.createCustomMembershipPlan(2, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(MembershipPlanResponse.class, response.getBody());
        MembershipPlanResponse plan = (MembershipPlanResponse) response.getBody();
        assertEquals("30-visit pack", plan.planName());
        assertEquals(MembershipService.BENEFIT_BOOKING_PACK, plan.benefitType());
        assertEquals(Integer.valueOf(30), plan.includedBookings());
        assertEquals(new BigDecimal("0.00"), plan.discountPercent());
        assertEquals(Boolean.FALSE, plan.standardPlan());
        assertTrue(plan.planCode().startsWith("CUSTOM_"));
    }
}
