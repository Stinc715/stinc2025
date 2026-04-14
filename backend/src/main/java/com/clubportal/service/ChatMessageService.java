package com.clubportal.service;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.MessageSenderType;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ChatSessionRepository;
import com.clubportal.util.KeyedLockService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChatMessageService {

    private static final Duration AI_DUPLICATE_RETRY_WINDOW = Duration.ofSeconds(20);
    private static final Duration HUMAN_DUPLICATE_RETRY_WINDOW = Duration.ofSeconds(8);

    private final ChatSessionService chatSessionService;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ClubChatAiService clubChatAiService;
    private final KeyedLockService keyedLockService;

    public ChatMessageService(ChatSessionService chatSessionService,
                              ChatSessionRepository chatSessionRepository,
                              ChatMessageRepository chatMessageRepository,
                              ClubChatAiService clubChatAiService,
                              KeyedLockService keyedLockService) {
        this.chatSessionService = chatSessionService;
        this.chatSessionRepository = chatSessionRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.clubChatAiService = clubChatAiService;
        this.keyedLockService = keyedLockService;
    }

    @Transactional
    public ChatSendResult sendUserMessage(Integer clubId, Integer userId, String content) {
        return keyedLockService.withLock(chatLockKey(clubId, userId), () -> {
            ChatSession session = chatSessionService.getOrCreateSession(clubId, userId);
            return switch (normalizeMode(session)) {
                case AI -> handleAiModeMessage(session, content);
                case HANDOFF_REQUESTED, HUMAN -> handleHumanModeMessage(session, MessageSenderType.USER, content);
                case CLOSED -> throw new ResponseStatusException(HttpStatus.CONFLICT, "This chat session is closed");
            };
        });
    }

    @Transactional
    public ChatSendResult sendClubMessage(Integer clubId, Integer userId, String content) {
        return keyedLockService.withLock(chatLockKey(clubId, userId), () -> {
            ChatSession session = chatSessionService.getOrCreateSession(clubId, userId);
            return switch (normalizeMode(session)) {
                case AI -> throw new ResponseStatusException(HttpStatus.CONFLICT, "Club replies are only allowed after human handoff");
                case HANDOFF_REQUESTED, HUMAN -> handleHumanModeMessage(session, MessageSenderType.CLUB, content);
                case CLOSED -> throw new ResponseStatusException(HttpStatus.CONFLICT, "This chat session is closed");
            };
        });
    }

    private ChatSendResult handleAiModeMessage(ChatSession session, String content) {
        ChatSendResult duplicateRetry = trySuppressDuplicateRetry(session, MessageSenderType.USER, content);
        if (duplicateRetry != null) {
            return duplicateRetry;
        }


        ChatMessage userMessage = saveMessage(
                session.getClubId(),
                session.getUserId(),
                MessageSenderType.USER,
                content,
                null,
                null,
                false,
                true,
                true
        );

        ClubChatAiReplyDecision replyDecision = clubChatAiService.buildReplyDecision(
                session.getClubId(),
                session.getUserId(),
                content
        );

        ChatMessage assistantMessage = saveMessage(
                session.getClubId(),
                session.getUserId(),
                MessageSenderType.ASSISTANT,
                replyDecision.replyText(),
                replyDecision.answerSource(),
                replyDecision.matchedFaqId(),
                replyDecision.handoffSuggested(),
                true,
                true
        );


        touchSession(session);
        return new ChatSendResult(
                userMessage,
                List.of(userMessage, assistantMessage),
                assistantMessage.getMessageId(),
                true,
                false
        );
    }

    private ChatSendResult handleHumanModeMessage(ChatSession session,
                                                  MessageSenderType senderType,
                                                  String content) {
        if (senderType != MessageSenderType.USER && senderType != MessageSenderType.CLUB) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported message sender");
        }

        ChatSendResult duplicateRetry = trySuppressDuplicateRetry(session, senderType, content);
        if (duplicateRetry != null) {
            return duplicateRetry;
        }

        boolean fromUser = senderType == MessageSenderType.USER;
        ChatMode currentMode = normalizeMode(session);
        if (!fromUser && currentMode == ChatMode.HANDOFF_REQUESTED) {
            session.setChatMode(ChatMode.HUMAN);
            session.setHandoffRequestedAt(null);
            session.setHandoffReason(null);
        }

        ChatMessage savedMessage = saveMessage(
                session.getClubId(),
                session.getUserId(),
                senderType,
                content,
                null,
                null,
                false,
                !fromUser,
                fromUser
        );

        if (fromUser) {
            int unread = session.getClubUnreadCount() == null ? 0 : Math.max(0, session.getClubUnreadCount());
            session.setClubUnreadCount(unread + 1);
        }
        touchSession(session);
        return new ChatSendResult(
                savedMessage,
                List.of(savedMessage),
                savedMessage.getMessageId(),
                true,
                true
        );
    }

    private ChatMessage saveMessage(Integer clubId,
                                    Integer userId,
                                    MessageSenderType senderType,
                                    String content,
                                    String answerSource,
                                    Integer matchedFaqId,
                                    boolean handoffSuggested,
                                    boolean readByClub,
                                    boolean readByUser) {
        ChatMessage row = new ChatMessage();
        row.setClubId(clubId);
        row.setUserId(userId);
        row.setSender(senderType.name());
        row.setMessageText(content);
        row.setAnswerSource(answerSource);
        row.setMatchedFaqId(matchedFaqId);
        row.setHandoffSuggested(handoffSuggested);
        row.setReadByClub(readByClub);
        row.setReadByUser(readByUser);
        return chatMessageRepository.save(row);
    }

    @Transactional
    public ChatMessage saveSystemMessage(Integer clubId,
                                         Integer userId,
                                         String content,
                                         boolean readByClub,
                                         boolean readByUser) {
        return saveMessage(
                clubId,
                userId,
                MessageSenderType.SYSTEM,
                content,
                "SYSTEM_HANDOFF",
                null,
                false,
                readByClub,
                readByUser
        );
    }

    private void touchSession(ChatSession session) {
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);
    }

    private static ChatMode normalizeMode(ChatSession session) {
        return session == null || session.getChatMode() == null ? ChatMode.AI : session.getChatMode();
    }

    private ChatSendResult trySuppressDuplicateRetry(ChatSession session,
                                                     MessageSenderType senderType,
                                                     String content) {
        if (session == null || senderType == null) {
            return null;
        }
        ChatMessage latestSameSender = chatMessageRepository.findTopByClubIdAndUserIdAndSenderOrderByCreatedAtDescMessageIdDesc(
                session.getClubId(),
                session.getUserId(),
                senderType.name()
        );
        if (!isLikelyDuplicateRetry(session, senderType, latestSameSender, content)) {
            return null;
        }
        List<ChatMessage> retryMessages = buildDuplicateRetryMessages(session, senderType, latestSameSender);
        ChatMessage latestVisibleMessage = retryMessages.isEmpty()
                ? latestSameSender
                : retryMessages.get(retryMessages.size() - 1);
        return new ChatSendResult(
                latestSameSender,
                retryMessages,
                latestVisibleMessage.getMessageId(),
                false,
                false
        );
    }

    private boolean isLikelyDuplicateRetry(ChatSession session,
                                           MessageSenderType senderType,
                                           ChatMessage latestSameSender,
                                           String content) {
        if (latestSameSender == null || latestSameSender.getCreatedAt() == null) {
            return false;
        }
        if (!Objects.equals(normalizeDuplicateContent(latestSameSender.getMessageText()), normalizeDuplicateContent(content))) {
            return false;
        }
        LocalDateTime cutoff = LocalDateTime.now().minus(resolveDuplicateRetryWindow(session, senderType));
        return !latestSameSender.getCreatedAt().isBefore(cutoff);
    }

    private Duration resolveDuplicateRetryWindow(ChatSession session, MessageSenderType senderType) {
        if (senderType == MessageSenderType.USER && normalizeMode(session) == ChatMode.AI) {
            return AI_DUPLICATE_RETRY_WINDOW;
        }
        return HUMAN_DUPLICATE_RETRY_WINDOW;
    }

    private List<ChatMessage> buildDuplicateRetryMessages(ChatSession session,
                                                          MessageSenderType senderType,
                                                          ChatMessage latestSameSender) {
        if (session == null || latestSameSender == null) {
            return List.of();
        }
        if (!(senderType == MessageSenderType.USER && normalizeMode(session) == ChatMode.AI)) {
            return List.of(latestSameSender);
        }

        List<ChatMessage> trailingMessages = chatMessageRepository.findByClubIdAndUserIdAndMessageIdGreaterThanEqualOrderByMessageIdAsc(
                session.getClubId(),
                session.getUserId(),
                latestSameSender.getMessageId()
        );
        if (trailingMessages.isEmpty()) {
            return List.of(latestSameSender);
        }

        List<ChatMessage> retryMessages = new ArrayList<>();
        boolean collecting = false;
        for (ChatMessage message : trailingMessages) {
            if (!collecting) {
                if (!Objects.equals(message.getMessageId(), latestSameSender.getMessageId())) {
                    continue;
                }
                collecting = true;
                retryMessages.add(message);
                continue;
            }
            if (MessageSenderType.USER.name().equalsIgnoreCase(message.getSender())) {
                break;
            }
            retryMessages.add(message);
        }
        return retryMessages.isEmpty() ? List.of(latestSameSender) : List.copyOf(retryMessages);
    }

    private static String normalizeDuplicateContent(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ");
    }

    private static String chatLockKey(Integer clubId, Integer userId) {
        return "chat-send:" + clubId + ":" + userId;
    }

    public record ChatSendResult(
            ChatMessage responseMessage,
            List<ChatMessage> savedMessages,
            Integer latestMessageId,
            boolean notifyUserThread,
            boolean notifyClubConversation
    ) {
    }
}
