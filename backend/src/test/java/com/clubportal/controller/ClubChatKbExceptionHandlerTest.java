package com.clubportal.controller;

import com.clubportal.dto.ClubChatKbEntryUpsertRequest;
import com.clubportal.model.User;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.service.ClubChatKbEmbeddingBackfillService;
import com.clubportal.service.ClubChatKbOperationException;
import com.clubportal.service.ClubChatKbService;
import com.clubportal.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ClubChatKbExceptionHandlerTest {

    @Test
    void embeddingFailureReturnsStructuredBusinessErrorBody() throws Exception {
        ClubRepository clubRepository = mock(ClubRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubChatKbService clubChatKbService = mock(ClubChatKbService.class);
        ClubChatKbEmbeddingBackfillService backfillService = mock(ClubChatKbEmbeddingBackfillService.class);

        when(clubRepository.existsById(2)).thenReturn(true);
        when(currentUserService.requireUser()).thenReturn(user(22, User.Role.CLUB));
        when(clubAdminRepository.existsByUserIdAndClubId(22, 2)).thenReturn(true);
        when(clubChatKbService.updateEntry(eq(2), eq(7), any(ClubChatKbEntryUpsertRequest.class)))
                .thenThrow(ClubChatKbOperationException.embeddingFailed(2, 7, "OPENAI_API_KEY is not configured", null));

        ClubChatKbController controller = new ClubChatKbController(
                clubRepository,
                clubAdminRepository,
                currentUserService,
                clubChatKbService,
                backfillService
        );

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ClubChatKbExceptionHandler())
                .build();

        mockMvc.perform(put("/api/my/clubs/2/chat-kb/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "questionTitle": "Can I bring my own racket?",
                                  "answerText": "Yes."
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("CHAT_KB_EMBEDDING_FAILED"))
                .andExpect(jsonPath("$.message").value("OPENAI_API_KEY is not configured"))
                .andExpect(jsonPath("$.clubId").value(2))
                .andExpect(jsonPath("$.entryId").value(7));
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
