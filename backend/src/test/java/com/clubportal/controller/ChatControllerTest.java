package com.clubportal.controller;

import com.clubportal.dto.ChatMessageCreateRequest;
import com.clubportal.dto.ChatSendResponse;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.Club;
import com.clubportal.model.User;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.ChatMessageService;
import com.clubportal.service.ChatRealtimeService;
import com.clubportal.service.ChatSessionService;
import com.clubportal.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Test
    void sendUserMessageReturnsLightweightPayloadIncludingAssistantReply() {
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ClubRepository clubRepository = mock(ClubRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        ClubAdminRepository clubAdminRepository = mock(ClubAdminRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ChatMessageService chatMessageService = mock(ChatMessageService.class);
        ChatRealtimeService chatRealtimeService = mock(ChatRealtimeService.class);
        ChatSessionService chatSessionService = mock(ChatSessionService.class);

        ChatController controller = new ChatController(
                currentUserService,
                clubRepository,
                userRepository,
                clubAdminRepository,
                chatMessageRepository,
                chatMessageService,
                chatRealtimeService,
                chatSessionService
        );

        Club club = new Club();
        club.setClubId(12);
        club.setClubName("Riverside Badminton Club");

        User me = new User();
        me.setUserId(45);
        me.setUsername("Alice");
        me.setRole(User.Role.USER);

        ChatSession session = new ChatSession();
        session.setSessionId(5);
        session.setClubId(12);
        session.setUserId(45);
        session.setChatMode(ChatMode.AI);

        ChatMessage userMessage = new ChatMessage();
        userMessage.setMessageId(101);
        userMessage.setClubId(12);
        userMessage.setUserId(45);
        userMessage.setSender("USER");
        userMessage.setMessageText("Is tomorrow's 7pm slot still available?");
        userMessage.setCreatedAt(LocalDateTime.of(2026, 3, 24, 16, 0));

        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setMessageId(102);
        assistantMessage.setClubId(12);
        assistantMessage.setUserId(45);
        assistantMessage.setSender("ASSISTANT");
        assistantMessage.setMessageText("From the currently published schedule, the 7:00 pm to 8:00 pm Court A slot has 2 places remaining and is showing GBP 8.00.");
        assistantMessage.setAnswerSource("CLUB_FAQ");
        assistantMessage.setMatchedFaqId(9);
        assistantMessage.setCreatedAt(LocalDateTime.of(2026, 3, 24, 16, 0, 1));

        ChatMessageService.ChatSendResult sendResult = new ChatMessageService.ChatSendResult(
                assistantMessage,
                List.of(userMessage, assistantMessage),
                assistantMessage.getMessageId(),
                true,
                false
        );

        when(clubRepository.findById(12)).thenReturn(java.util.Optional.of(club));
        when(currentUserService.requireUser()).thenReturn(me);
        when(chatMessageService.sendUserMessage(12, 45, "Is tomorrow's 7pm slot still available?")).thenReturn(sendResult);
        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatMessageRepository.countByClubIdAndUserIdAndSenderAndReadByUserFalse(12, 45, "CLUB")).thenReturn(0L);
        doNothing().when(chatRealtimeService).afterCommit(any());

        ResponseEntity<?> response = controller.sendUserMessage(12, new ChatMessageCreateRequest("Is tomorrow's 7pm slot still available?"));

        assertEquals(200, response.getStatusCode().value());
        ChatSendResponse body = assertInstanceOf(ChatSendResponse.class, response.getBody());
        assertEquals(2, body.messages().size());
        assertEquals(false, body.fullThread());
        assertEquals("user", body.messages().get(0).sender());
        assertEquals("assistant", body.messages().get(1).sender());
        assertEquals("CLUB_FAQ", body.messages().get(1).answerSource());
        assertEquals(Integer.valueOf(9), body.messages().get(1).matchedFaqId());
        assertEquals("From the currently published schedule, the 7:00 pm to 8:00 pm Court A slot has 2 places remaining and is showing GBP 8.00.",
                body.messages().get(1).text());
    }
}
