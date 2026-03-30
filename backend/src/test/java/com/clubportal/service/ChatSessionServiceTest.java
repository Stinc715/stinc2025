package com.clubportal.service;

import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.HandoffReason;
import com.clubportal.repository.ChatSessionRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatSessionServiceTest {

    @Test
    void getOrCreateSessionCreatesAiSessionByDefault() {
        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findByClubIdAndUserId(12, 45)).thenReturn(Optional.empty());
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> {
            ChatSession session = invocation.getArgument(0);
            session.setSessionId(99);
            return session;
        });

        ChatSessionService service = new ChatSessionService(repository);
        ChatSession session = service.getOrCreateSession(12, 45);

        assertEquals(99, session.getSessionId());
        assertEquals(12, session.getClubId());
        assertEquals(45, session.getUserId());
        assertEquals(ChatMode.AI, session.getChatMode());
        assertEquals(0, session.getClubUnreadCount());
    }

    @Test
    void requestHandoffMarksSessionAndBumpsClubUnreadCount() {
        ChatSession existing = new ChatSession();
        existing.setSessionId(7);
        existing.setClubId(12);
        existing.setUserId(45);
        existing.setChatMode(ChatMode.AI);
        existing.setClubUnreadCount(0);

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findById(7)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionService service = new ChatSessionService(repository);
        ChatSession updated = service.requestHandoff(7, HandoffReason.USER_REQUEST);

        assertEquals(ChatMode.HANDOFF_REQUESTED, updated.getChatMode());
        assertEquals(HandoffReason.USER_REQUEST, updated.getHandoffReason());
        assertEquals(1, updated.getClubUnreadCount());
        assertNotNull(updated.getHandoffRequestedAt());
    }

    @Test
    void recordUserMessageOnlyCountsForHandoffOrHumanModes() {
        ChatSession aiSession = new ChatSession();
        aiSession.setSessionId(1);
        aiSession.setClubId(12);
        aiSession.setUserId(45);
        aiSession.setChatMode(ChatMode.AI);
        aiSession.setClubUnreadCount(0);

        ChatSession humanSession = new ChatSession();
        humanSession.setSessionId(2);
        humanSession.setClubId(12);
        humanSession.setUserId(46);
        humanSession.setChatMode(ChatMode.HUMAN);
        humanSession.setClubUnreadCount(3);

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findByClubIdAndUserId(12, 45)).thenReturn(Optional.of(aiSession));
        when(repository.findByClubIdAndUserId(12, 46)).thenReturn(Optional.of(humanSession));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionService service = new ChatSessionService(repository);

        ChatSession unchanged = service.recordUserMessage(12, 45);
        ChatSession updated = service.recordUserMessage(12, 46);

        assertEquals(0, unchanged.getClubUnreadCount());
        assertEquals(4, updated.getClubUnreadCount());
    }
}
