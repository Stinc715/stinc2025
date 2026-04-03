package com.clubportal.service;

import com.clubportal.config.ChatDebugVersion;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.MessageSenderType;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ChatSessionRepository;
import com.clubportal.util.KeyedLockService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ChatMessageService {

    private static final Logger log = LoggerFactory.getLogger(ChatMessageService.class);
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

        log.info("[CLUB_CHAT_DEBUG] AI request start: version={}, thread={}, requestUri={}, sessionId={}, clubId={}, userId={}, chatMode={}, message=\"{}\"",
                ChatDebugVersion.VERSION_MARKER,
                Thread.currentThread().getName(),
                currentRequestUri(),
                session == null ? null : session.getSessionId(),
                session == null ? null : session.getClubId(),
                session == null ? null : session.getUserId(),
                normalizeMode(session),
                safe(content));

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

        log.info("[CLUB_CHAT_DEBUG] AI request resolved: sessionId={}, clubId={}, userId={}, answerSource={}, matchedFaqId={}, similarityScore={}, secondBestScore={}, assistantMessageId={}",
                session.getSessionId(),
                session.getClubId(),
                session.getUserId(),
                replyDecision.answerSource(),
                replyDecision.matchedFaqId(),
                scoreText(replyDecision.similarityScore()),
                scoreText(replyDecision.secondBestScore()),
                assistantMessage.getMessageId());

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
            log.info("[CLUB_CHAT_DEBUG] human handoff accepted by club reply: sessionId={}, clubId={}, userId={}",
                    session.getSessionId(),
                    session.getClubId(),
                    session.getUserId());
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

    private static String safe(String value) {
        return value == null ? "" : value.replace("\"", "\\\"").trim();
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
        log.info("[CLUB_CHAT_DEBUG] duplicate chat retry suppressed: sessionId={}, clubId={}, userId={}, sender={}, messageId={}, createdAt={}",
                session.getSessionId(),
                session.getClubId(),
                session.getUserId(),
                senderType.name(),
                latestSameSender.getMessageId(),
                latestSameSender.getCreatedAt());
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

    private static String currentRequestUri() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes == null ? null : attributes.getRequest();
        return request == null ? null : request.getRequestURI();
    }

    private static String scoreText(Double value) {
        return value == null ? "null" : String.format(java.util.Locale.ROOT, "%.4f", value);
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
