package com.clubportal.controller;

import com.clubportal.dto.ChatSessionHandoffRequest;
import com.clubportal.dto.ChatSessionResponse;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.HandoffReason;
import com.clubportal.model.User;
import com.clubportal.service.ChatMessageService;
import com.clubportal.service.ChatRealtimeService;
import com.clubportal.service.ChatSessionService;
import com.clubportal.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatSessionControllerTest {

    @Test
    void requestHandoffCreatesSystemMessageAndReturnsUpdatedSession() {
        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        ChatMessageService chatMessageService = mock(ChatMessageService.class);
        ChatRealtimeService chatRealtimeService = mock(ChatRealtimeService.class);

        ChatSessionController controller = new ChatSessionController(
                chatSessionService,
                currentUserService,
                chatMessageService,
                chatRealtimeService
        );

        User me = new User();
        me.setUserId(6);
        me.setRole(User.Role.USER);

        ChatSession existing = new ChatSession();
        existing.setSessionId(12);
        existing.setClubId(2);
        existing.setUserId(6);
        existing.setChatMode(ChatMode.AI);

        ChatSession updated = new ChatSession();
        updated.setSessionId(12);
        updated.setClubId(2);
        updated.setUserId(6);
        updated.setChatMode(ChatMode.HANDOFF_REQUESTED);
        updated.setHandoffReason(HandoffReason.USER_REQUEST);
        updated.setClubUnreadCount(0);

        ChatMessage systemMessage = new ChatMessage();
        systemMessage.setMessageId(99);

        when(currentUserService.requireUser()).thenReturn(me);
        when(chatSessionService.requireSession(12)).thenReturn(existing);
        when(chatSessionService.requestHandoff(12, HandoffReason.USER_REQUEST)).thenReturn(updated);
        when(chatMessageService.saveSystemMessage(eq(2), eq(6), any(String.class), eq(false), eq(true))).thenReturn(systemMessage);
        doNothing().when(chatRealtimeService).afterCommit(any());

        ResponseEntity<?> response = controller.requestHandoff(12, new ChatSessionHandoffRequest("USER_REQUEST"));

        assertEquals(200, response.getStatusCode().value());
        ChatSessionResponse body = assertInstanceOf(ChatSessionResponse.class, response.getBody());
        assertEquals("HANDOFF_REQUESTED", body.chatMode());
        assertEquals("USER_REQUEST", body.handoffReason());

        verify(chatMessageService).saveSystemMessage(2, 6, "Member requested human support.", false, true);
        verify(chatRealtimeService).afterCommit(any());
    }
}
