package com.clubportal.service;

import com.clubportal.config.ChatHandoffProperties;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.HandoffReason;
import com.clubportal.repository.ChatSessionRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        ChatSessionService service = new ChatSessionService(repository, handoffProperties());
        ChatSession session = service.getOrCreateSession(12, 45);

        assertEquals(99, session.getSessionId());
        assertEquals(12, session.getClubId());
        assertEquals(45, session.getUserId());
        assertEquals(ChatMode.AI, session.getChatMode());
        assertEquals(0, session.getClubUnreadCount());
    }

    @Test
    void requestHandoffMarksSessionWithoutInflatingClubUnreadCount() {
        ChatSession existing = new ChatSession();
        existing.setSessionId(7);
        existing.setClubId(12);
        existing.setUserId(45);
        existing.setChatMode(ChatMode.AI);
        existing.setClubUnreadCount(2);

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findById(7)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionService service = new ChatSessionService(repository, handoffProperties());
        ChatSession updated = service.requestHandoff(7, HandoffReason.USER_REQUEST);

        assertEquals(ChatMode.HANDOFF_REQUESTED, updated.getChatMode());
        assertEquals(HandoffReason.USER_REQUEST, updated.getHandoffReason());
        assertEquals(2, updated.getClubUnreadCount());
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
        aiSession.setUpdatedAt(LocalDateTime.now().minusMinutes(5));

        ChatSession humanSession = new ChatSession();
        humanSession.setSessionId(2);
        humanSession.setClubId(12);
        humanSession.setUserId(46);
        humanSession.setChatMode(ChatMode.HUMAN);
        humanSession.setClubUnreadCount(3);
        humanSession.setUpdatedAt(LocalDateTime.now().minusMinutes(5));

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findByClubIdAndUserId(12, 45)).thenReturn(Optional.of(aiSession));
        when(repository.findByClubIdAndUserId(12, 46)).thenReturn(Optional.of(humanSession));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionService service = new ChatSessionService(repository, handoffProperties());

        ChatSession unchanged = service.recordUserMessage(12, 45);
        ChatSession updated = service.recordUserMessage(12, 46);

        assertEquals(0, unchanged.getClubUnreadCount());
        assertEquals(4, updated.getClubUnreadCount());
    }

    @Test
    void getOrCreateSessionResetsExpiredHumanSessionBackToAi() {
        ChatSession existing = new ChatSession();
        existing.setSessionId(21);
        existing.setClubId(12);
        existing.setUserId(45);
        existing.setChatMode(ChatMode.HUMAN);
        existing.setHandoffReason(HandoffReason.USER_REQUEST);
        existing.setHandoffRequestedAt(LocalDateTime.now().minusHours(3));
        existing.setUpdatedAt(LocalDateTime.now().minusHours(2));
        existing.setClubUnreadCount(2);

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findByClubIdAndUserId(12, 45)).thenReturn(Optional.of(existing));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionService service = new ChatSessionService(repository, handoffProperties());
        ChatSession session = service.getOrCreateSession(12, 45);

        assertEquals(ChatMode.AI, session.getChatMode());
        assertEquals(0, session.getClubUnreadCount());
        assertNull(session.getHandoffRequestedAt());
        assertNull(session.getHandoffReason());
    }

    @Test
    void getOrCreateSessionKeepsRecentHumanSessionActive() {
        ChatSession existing = new ChatSession();
        existing.setSessionId(22);
        existing.setClubId(12);
        existing.setUserId(45);
        existing.setChatMode(ChatMode.HUMAN);
        existing.setUpdatedAt(LocalDateTime.now().minusMinutes(20));
        existing.setClubUnreadCount(2);

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findByClubIdAndUserId(12, 45)).thenReturn(Optional.of(existing));

        ChatSessionService service = new ChatSessionService(repository, handoffProperties());
        ChatSession session = service.getOrCreateSession(12, 45);

        assertEquals(ChatMode.HUMAN, session.getChatMode());
        assertEquals(2, session.getClubUnreadCount());
        verify(repository, never()).save(any(ChatSession.class));
    }

    @Test
    void getOrCreateSessionsNormalizesExpiredHumanModesInConversationLists() {
        ChatSession stale = new ChatSession();
        stale.setSessionId(31);
        stale.setClubId(12);
        stale.setUserId(45);
        stale.setChatMode(ChatMode.HANDOFF_REQUESTED);
        stale.setUpdatedAt(LocalDateTime.now().minusHours(5));
        stale.setClubUnreadCount(1);

        ChatSession fresh = new ChatSession();
        fresh.setSessionId(32);
        fresh.setClubId(12);
        fresh.setUserId(46);
        fresh.setChatMode(ChatMode.HUMAN);
        fresh.setUpdatedAt(LocalDateTime.now().minusMinutes(10));
        fresh.setClubUnreadCount(3);

        ChatSessionRepository repository = mock(ChatSessionRepository.class);
        when(repository.findByClubIdAndUserIdIn(12, List.of(45, 46))).thenReturn(List.of(stale, fresh));
        when(repository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatSessionService service = new ChatSessionService(repository, handoffProperties());
        var sessions = service.getOrCreateSessions(12, List.of(45, 46));

        assertEquals(ChatMode.AI, sessions.get(45).getChatMode());
        assertEquals(ChatMode.HUMAN, sessions.get(46).getChatMode());
        verify(repository).save(stale);
    }

    private static ChatHandoffProperties handoffProperties() {
        ChatHandoffProperties properties = new ChatHandoffProperties();
        properties.setIdleResetMinutes(60);
        return properties;
    }
}
