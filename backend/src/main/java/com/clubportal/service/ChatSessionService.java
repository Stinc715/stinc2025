package com.clubportal.service;

import com.clubportal.config.ChatHandoffProperties;
import com.clubportal.model.ChatMode;
import com.clubportal.model.ChatSession;
import com.clubportal.model.HandoffReason;
import com.clubportal.repository.ChatSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ChatSessionService {

    private final ChatSessionRepository chatSessionRepository;
    private final ChatHandoffProperties chatHandoffProperties;

    public ChatSessionService(ChatSessionRepository chatSessionRepository,
                              ChatHandoffProperties chatHandoffProperties) {
        this.chatSessionRepository = chatSessionRepository;
        this.chatHandoffProperties = chatHandoffProperties;
    }

    @Transactional
    public ChatSession getOrCreateSession(Integer clubId, Integer userId) {
        validateIds(clubId, userId);
        return chatSessionRepository.findByClubIdAndUserId(clubId, userId)
                .map(this::resetExpiredHumanModeIfNeeded)
                .orElseGet(() -> {
                    ChatSession session = new ChatSession();
                    session.setClubId(clubId);
                    session.setUserId(userId);
                    session.setChatMode(ChatMode.AI);
                    session.setClubUnreadCount(0);
                    return chatSessionRepository.save(session);
                });
    }

    @Transactional
    public Map<Integer, ChatSession> getOrCreateSessions(Integer clubId, Collection<Integer> userIds) {
        Map<Integer, ChatSession> out = new HashMap<>();
        if (clubId == null || userIds == null || userIds.isEmpty()) {
            return out;
        }

        List<Integer> normalizedUserIds = userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedUserIds.isEmpty()) {
            return out;
        }

        Map<Integer, ChatSession> existingByUserId = new HashMap<>();
        for (ChatSession session : chatSessionRepository.findByClubIdAndUserIdIn(clubId, normalizedUserIds)) {
            if (session.getUserId() != null) {
                existingByUserId.put(session.getUserId(), resetExpiredHumanModeIfNeeded(session));
            }
        }

        for (Integer userId : normalizedUserIds) {
            ChatSession session = existingByUserId.get(userId);
            if (session == null) {
                session = getOrCreateSession(clubId, userId);
            }
            out.put(userId, session);
        }
        return out;
    }

    @Transactional
    public ChatSession requestHandoff(Integer sessionId, HandoffReason reason) {
        ChatSession session = requireSession(sessionId);
        session.setChatMode(ChatMode.HANDOFF_REQUESTED);
        session.setHandoffRequestedAt(LocalDateTime.now());
        session.setHandoffReason(reason == null ? HandoffReason.OTHER : reason);
        return chatSessionRepository.save(session);
    }

    @Transactional
    public ChatSession acceptHandoff(Integer sessionId) {
        ChatSession session = requireSession(sessionId);
        session.setChatMode(ChatMode.HUMAN);
        return chatSessionRepository.save(session);
    }

    @Transactional
    public ChatSession closeSession(Integer sessionId) {
        ChatSession session = requireSession(sessionId);
        session.setChatMode(ChatMode.CLOSED);
        return chatSessionRepository.save(session);
    }

    @Transactional
    public ChatSession resetClubUnreadCount(Integer sessionId) {
        ChatSession session = requireSession(sessionId);
        session.setClubUnreadCount(0);
        return chatSessionRepository.save(session);
    }

    @Transactional
    public ChatSession recordUserMessage(Integer clubId, Integer userId) {
        ChatSession session = getOrCreateSession(clubId, userId);
        ChatMode mode = session.getChatMode() == null ? ChatMode.AI : session.getChatMode();
        if (mode == ChatMode.HANDOFF_REQUESTED || mode == ChatMode.HUMAN) {
            session.setClubUnreadCount(normalizeUnreadCount(session.getClubUnreadCount()) + 1);
            return chatSessionRepository.save(session);
        }
        return session;
    }

    public ChatSession requireSession(Integer sessionId) {
        if (sessionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chat session id is required");
        }
        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat session not found"));
        return resetExpiredHumanModeIfNeeded(session);
    }

    private static void validateIds(Integer clubId, Integer userId) {
        if (clubId == null || userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Club id and user id are required");
        }
    }

    private static int normalizeUnreadCount(Integer unreadCount) {
        return unreadCount == null ? 0 : Math.max(0, unreadCount);
    }

    private ChatSession resetExpiredHumanModeIfNeeded(ChatSession session) {
        if (session == null || !isHumanManagedMode(session.getChatMode())) {
            return session;
        }
        LocalDateTime updatedAt = session.getUpdatedAt();
        if (updatedAt == null) {
            return resetSessionToAi(session);
        }
        Duration idleTimeout = Duration.ofMinutes(chatHandoffProperties.getIdleResetMinutes());
        if (!updatedAt.isAfter(LocalDateTime.now().minus(idleTimeout))) {
            return resetSessionToAi(session);
        }
        return session;
    }

    private ChatSession resetSessionToAi(ChatSession session) {
        session.setChatMode(ChatMode.AI);
        session.setHandoffRequestedAt(null);
        session.setHandoffReason(null);
        session.setClubUnreadCount(0);
        return chatSessionRepository.save(session);
    }

    private static boolean isHumanManagedMode(ChatMode mode) {
        return mode == ChatMode.HANDOFF_REQUESTED || mode == ChatMode.HUMAN;
    }
}
