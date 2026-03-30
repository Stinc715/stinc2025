package com.clubportal.controller;

import com.clubportal.dto.ClubChatKbEntryResponse;
import com.clubportal.dto.ClubChatKbEntryUpsertRequest;
import com.clubportal.model.User;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.service.ClubChatKbEmbeddingBackfillService;
import com.clubportal.service.ClubChatKbEmbeddingBackfillSummary;
import com.clubportal.service.ClubChatKbService;
import com.clubportal.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.*;

class ClubChatKbControllerTest {

    @Test
    void listEntriesRequiresClubAdmin() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbService clubChatKbService = mock(ClubChatKbService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);

        when(clubRepository.existsById(7)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(user(18, User.Role.USER));

        ClubChatKbController controller = new ClubChatKbController(
                clubRepository,
                clubAdminRepository,
                currentUserService,
                clubChatKbService,
                backfillService
        );

        ResponseEntity<?> response = controller.listEntries(7);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(clubChatKbService);
    }

    @Test
    void createEntryReturnsCreatedResponse() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbService clubChatKbService = mock(ClubChatKbService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);

        ClubChatKbEntryUpsertRequest request = new ClubChatKbEntryUpsertRequest(
                "Equipment policy",
                "Yes. You may bring your own racket.",
                List.of("bring your own racket"),
                List.of("Can I bring my own racket?"),
                "EN",
                5,
                true
        );
        ClubChatKbEntryResponse responseBody = new ClubChatKbEntryResponse(
                15,
                4,
                "Equipment policy",
                "Yes. You may bring your own racket.",
                List.of("bring your own racket"),
                List.of("Can I bring my own racket?"),
                "EN",
                5,
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(clubRepository.existsById(4)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(user(22, User.Role.CLUB));
        when(clubAdminRepository.existsByUserIdAndClubId(22, 4)).thenReturn(true);
        when(clubChatKbService.createEntry(4, request)).thenReturn(responseBody);

        ClubChatKbController controller = new ClubChatKbController(
                clubRepository,
                clubAdminRepository,
                currentUserService,
                clubChatKbService,
                backfillService
        );

        ResponseEntity<?> response = controller.createEntry(4, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertInstanceOf(ClubChatKbEntryResponse.class, response.getBody());
        assertEquals("Equipment policy", ((ClubChatKbEntryResponse) response.getBody()).questionTitle());
    }

    @Test
    void clubBackfillReturnsSummaryForClubAdmin() {
        ClubRepository clubRepository = mock(ClubRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbService clubChatKbService = mock(ClubChatKbService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);

        ClubChatKbEmbeddingBackfillSummary summary = new ClubChatKbEmbeddingBackfillSummary(
                "CLUB",
                4,
                false,
                true,
                3,
                2,
                0,
                1,
                0,
                List.of()
        );

        when(clubRepository.existsById(4)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(user(22, User.Role.CLUB));
        when(clubAdminRepository.existsByUserIdAndClubId(22, 4)).thenReturn(true);
        when(backfillService.backfillClub(4, false, true)).thenReturn(summary);

        ClubChatKbController controller = new ClubChatKbController(
                clubRepository,
                clubAdminRepository,
                currentUserService,
                clubChatKbService,
                backfillService
        );

        ResponseEntity<?> response = controller.backfillClub(4, false, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ClubChatKbEmbeddingBackfillSummary.class, response.getBody());
        assertEquals(2, ((ClubChatKbEmbeddingBackfillSummary) response.getBody()).eligibleCount());
    }

    private User user(int userId, User.Role role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setEmail("club@example.com");
        user.setUsername("club");
        user.setPasswordHash("hash");
        return user;
    }
}
