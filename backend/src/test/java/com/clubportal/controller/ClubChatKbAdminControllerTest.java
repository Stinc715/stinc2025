package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.service.ClubChatKbEmbeddingBackfillService;
import com.clubportal.service.ClubChatKbEmbeddingBackfillSummary;
import com.clubportal.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClubChatKbAdminControllerTest {

    @Test
    void globalBackfillRequiresSystemAdmin() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);
        when(currentUserService.requireUser()).thenReturn(user(9, User.Role.CLUB));

        ClubChatKbAdminController controller = new ClubChatKbAdminController(currentUserService, backfillService);

        ResponseEntity<?> response = controller.backfillAll(false, false);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(backfillService);
    }

    @Test
    void globalBackfillReturnsSummaryForSystemAdmin() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);
        when(currentUserService.requireUser()).thenReturn(user(1, User.Role.ADMIN));
        when(backfillService.backfillAll(true, true)).thenReturn(new ClubChatKbEmbeddingBackfillSummary(
                "ALL",
                null,
                true,
                true,
                10,
                10,
                0,
                0,
                0,
                List.of()
        ));

        ClubChatKbAdminController controller = new ClubChatKbAdminController(currentUserService, backfillService);

        ResponseEntity<?> response = controller.backfillAll(true, true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ClubChatKbEmbeddingBackfillSummary.class, response.getBody());
        assertEquals(10, ((ClubChatKbEmbeddingBackfillSummary) response.getBody()).eligibleCount());
    }

    @Test
    void clubBackfillReturnsSummaryForSystemAdmin() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);
        when(currentUserService.requireUser()).thenReturn(user(1, User.Role.ADMIN));
        when(backfillService.backfillClub(2, false, false)).thenReturn(new ClubChatKbEmbeddingBackfillSummary(
                "CLUB",
                2,
                false,
                false,
                2,
                2,
                2,
                0,
                0,
                List.of()
        ));

        ClubChatKbAdminController controller = new ClubChatKbAdminController(currentUserService, backfillService);

        ResponseEntity<?> response = controller.backfillClub(2, false, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertInstanceOf(ClubChatKbEmbeddingBackfillSummary.class, response.getBody());
        assertEquals(2, ((ClubChatKbEmbeddingBackfillSummary) response.getBody()).clubId());
    }

    private User user(int userId, User.Role role) {
        User user = new User();
        user.setUserId(userId);
        user.setRole(role);
        user.setEmail("admin@example.com");
        user.setUsername("admin");
        user.setPasswordHash("hash");
        return user;
    }
}
