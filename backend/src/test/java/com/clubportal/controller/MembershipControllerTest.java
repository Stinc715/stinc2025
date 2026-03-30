package com.clubportal.controller;

import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.MembershipPlanRepository;
import com.clubportal.repository.TransactionRecordRepository;
import com.clubportal.repository.UserMembershipRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.MembershipService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MembershipControllerTest {

    @Test
    void listPublicMembershipPlansDoesNotAutoCreateMissingPlans() {
        MembershipPlanRepository membershipPlanRepo = mock(MembershipPlanRepository.class);
        UserMembershipRepository userMembershipRepo = mock(UserMembershipRepository.class);
        TransactionRecordRepository transactionRecordRepo = mock(TransactionRecordRepository.class);
        ClubRepository clubRepo = mock(ClubRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepo = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        MembershipService membershipService = mock(MembershipService.class);

        when(clubRepo.existsById(24)).thenReturn(true);
        when(membershipPlanRepo.findByClubIdAndEnabledTrueOrderByDurationDaysAsc(24)).thenReturn(List.of());

        MembershipController controller = new MembershipController(
                membershipPlanRepo,
                userMembershipRepo,
                transactionRecordRepo,
                clubRepo,
                userRepo,
                clubAdminRepo,
                currentUserService,
                membershipService
        );

        ResponseEntity<?> response = controller.listPublicMembershipPlans(24);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(List.class, response.getBody());
        assertTrue(((List<?>) response.getBody()).isEmpty());
        verifyNoInteractions(membershipService);
    }
}
