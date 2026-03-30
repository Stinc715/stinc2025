package com.clubportal.service;

import com.clubportal.model.ChatMessage;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.HandoffReason;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ChatSessionRepository;
import com.clubportal.util.KeyedLockService;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ChatMessageServiceTest {

    @Test
    void aiModeSavesUserAndAssistantWithoutClubNotification() {
        ChatSession session = session(5, 12, 45, ChatMode.AI, 0);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clubChatAiService.buildReplyDecision(12, 45, "Do you have any evening slots?"))
                .thenReturn(ClubChatAiReplyDecision.standard(
                        "From the currently published schedule, I can see one visible evening slot.",
                        "CLUB_CHAT_OPENAI"
                ));

        AtomicInteger ids = new AtomicInteger(100);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setMessageId(ids.getAndIncrement());
            return message;
        });

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendUserMessage(12, 45, "Do you have any evening slots?");

        assertEquals("USER", result.responseMessage().getSender());
        assertEquals(2, result.savedMessages().size());
        assertEquals("USER", result.savedMessages().get(0).getSender());
        assertEquals("ASSISTANT", result.savedMessages().get(1).getSender());
        assertEquals("CLUB_CHAT_OPENAI", result.savedMessages().get(1).getAnswerSource());
        assertEquals(false, result.savedMessages().get(1).isHandoffSuggested());
        assertEquals("From the currently published schedule, I can see one visible evening slot.",
                result.savedMessages().get(1).getMessageText());
        assertEquals(0, session.getClubUnreadCount());
        assertEquals(true, result.notifyUserThread());
        assertEquals(false, result.notifyClubConversation());
    }

    @Test
    void aiModePersistsFaqDirectReplyThroughSameMessageFlow() {
        ChatSession session = session(6, 12, 45, ChatMode.AI, 0);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(clubChatAiService.buildReplyDecision(12, 45, "What are your opening hours?"))
                .thenReturn(ClubChatAiReplyDecision.clubFaq(
                        "We are open from 8am to 10pm on weekdays.",
                        7,
                        "When are you open?",
                        0.94d,
                        0.70d
                ));

        AtomicInteger ids = new AtomicInteger(200);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setMessageId(ids.getAndIncrement());
            return message;
        });

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendUserMessage(12, 45, "What are your opening hours?");

        assertEquals(2, result.savedMessages().size());
        assertEquals("ASSISTANT", result.savedMessages().get(1).getSender());
        assertEquals("CLUB_FAQ", result.savedMessages().get(1).getAnswerSource());
        assertEquals(Integer.valueOf(7), result.savedMessages().get(1).getMatchedFaqId());
        assertEquals("We are open from 8am to 10pm on weekdays.", result.savedMessages().get(1).getMessageText());
        verify(clubChatAiService).buildReplyDecision(12, 45, "What are your opening hours?");
    }

    @Test
    void humanModeKeepsManualBranchAndIncrementsClubUnread() {
        ChatSession session = session(7, 12, 45, ChatMode.HUMAN, 2);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setMessageId(501);
            return message;
        });

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendUserMessage(12, 45, "I need help with my booking.");

        assertEquals(1, result.savedMessages().size());
        assertEquals("USER", result.savedMessages().get(0).getSender());
        assertEquals(3, session.getClubUnreadCount());
        assertEquals(true, result.notifyUserThread());
        assertEquals(true, result.notifyClubConversation());
        verifyNoInteractions(clubChatAiService);
    }

    @Test
    void handoffRequestedModeKeepsManualBranchAndNeverCallsAi() {
        ChatSession session = session(8, 12, 45, ChatMode.HANDOFF_REQUESTED, 1);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setMessageId(601);
            return message;
        });

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendUserMessage(12, 45, "I still need help.");

        assertEquals(1, result.savedMessages().size());
        assertEquals("USER", result.savedMessages().get(0).getSender());
        assertEquals(2, session.getClubUnreadCount());
        verifyNoInteractions(clubChatAiService);
    }

    @Test
    void clubReplyAcceptsPendingHandoffAndClearsPendingBadgeState() {
        ChatSession session = session(10, 12, 45, ChatMode.HANDOFF_REQUESTED, 1);
        session.setHandoffReason(HandoffReason.USER_REQUEST);
        session.setHandoffRequestedAt(java.time.LocalDateTime.of(2026, 3, 29, 10, 15));

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatSessionRepository.save(any(ChatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setMessageId(701);
            return message;
        });

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendClubMessage(12, 45, "Hi, a staff member is here now.");

        assertEquals(1, result.savedMessages().size());
        assertEquals("CLUB", result.savedMessages().get(0).getSender());
        assertEquals(ChatMode.HUMAN, session.getChatMode());
        assertEquals(null, session.getHandoffRequestedAt());
        assertEquals(null, session.getHandoffReason());
        verifyNoInteractions(clubChatAiService);
    }

    @Test
    void closedModeRejectsNewMessages() {
        ChatSession session = session(9, 12, 45, ChatMode.CLOSED, 0);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.sendUserMessage(12, 45, "Any message"));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    void aiModeSuppressesRapidDuplicateRetryWithoutCallingAiAgain() {
        ChatSession session = session(11, 12, 45, ChatMode.AI, 0);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        ChatMessage existing = new ChatMessage();
        existing.setMessageId(888);
        existing.setClubId(12);
        existing.setUserId(45);
        existing.setSender("USER");
        existing.setMessageText("Need help");
        existing.setCreatedAt(LocalDateTime.now().minusSeconds(2));

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatMessageRepository.findTopByClubIdAndUserIdAndSenderOrderByCreatedAtDescMessageIdDesc(12, 45, "USER"))
                .thenReturn(existing);

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendUserMessage(12, 45, "Need help");

        assertEquals(Integer.valueOf(888), result.responseMessage().getMessageId());
        assertEquals(1, result.savedMessages().size());
        assertEquals(false, result.notifyUserThread());
        assertEquals(false, result.notifyClubConversation());
        verifyNoInteractions(clubChatAiService);
    }

    @Test
    void humanModeSuppressesRapidDuplicateClubReplyRetry() {
        ChatSession session = session(12, 12, 45, ChatMode.HUMAN, 0);

        ChatSessionService chatSessionService = mock(ChatSessionService.class);
        ChatSessionRepository chatSessionRepository = mock(ChatSessionRepository.class);
        ChatMessageRepository chatMessageRepository = mock(ChatMessageRepository.class);
        ClubChatAiService clubChatAiService = mock(ClubChatAiService.class);

        ChatMessage existing = new ChatMessage();
        existing.setMessageId(999);
        existing.setClubId(12);
        existing.setUserId(45);
        existing.setSender("CLUB");
        existing.setMessageText("Staff is here.");
        existing.setCreatedAt(LocalDateTime.now().minusSeconds(3));

        when(chatSessionService.getOrCreateSession(12, 45)).thenReturn(session);
        when(chatMessageRepository.findTopByClubIdAndUserIdAndSenderOrderByCreatedAtDescMessageIdDesc(12, 45, "CLUB"))
                .thenReturn(existing);

        ChatMessageService service = new ChatMessageService(chatSessionService, chatSessionRepository, chatMessageRepository, clubChatAiService, new KeyedLockService());
        ChatMessageService.ChatSendResult result = service.sendClubMessage(12, 45, "Staff is here.");

        assertEquals(Integer.valueOf(999), result.responseMessage().getMessageId());
        assertEquals(1, result.savedMessages().size());
        assertEquals(false, result.notifyUserThread());
        assertEquals(false, result.notifyClubConversation());
        verifyNoInteractions(clubChatAiService);
    }

    private static ChatSession session(int sessionId, int clubId, int userId, ChatMode mode, int unreadCount) {
        ChatSession session = new ChatSession();
        session.setSessionId(sessionId);
        session.setClubId(clubId);
        session.setUserId(userId);
        session.setChatMode(mode);
        session.setClubUnreadCount(unreadCount);
        return session;
    }
}
