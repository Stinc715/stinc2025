package com.clubportal.controller;

import com.clubportal.dto.ChatConversationSummaryResponse;
import com.clubportal.dto.ChatMarkReadResponse;
import com.clubportal.dto.ChatMessageCreateRequest;
import com.clubportal.dto.ChatMessageResponse;
import com.clubportal.dto.ChatThreadResponse;
import com.clubportal.model.ChatSession;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.Club;
import com.clubportal.model.HandoffReason;
import com.clubportal.model.MessageSenderType;
import com.clubportal.model.User;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.ChatMessageService;
import com.clubportal.service.ChatRealtimeService;
import com.clubportal.service.ChatSessionService;
import com.clubportal.service.CurrentUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final String SENDER_USER = "USER";
    private static final String SENDER_CLUB = "CLUB";
    private static final int MAX_MESSAGE_LENGTH = 500;

    private final CurrentUserService currentUserService;
    private final ClubRepository clubRepo;
    private final UserRepository userRepo;
    private final ClubAdminRepository clubAdminRepo;
    private final ChatMessageRepository chatMessageRepo;
    private final ChatMessageService chatMessageService;
    private final ChatRealtimeService chatRealtimeService;
    private final ChatSessionService chatSessionService;

    public ChatController(CurrentUserService currentUserService,
                          ClubRepository clubRepo,
                          UserRepository userRepo,
                          ClubAdminRepository clubAdminRepo,
                          ChatMessageRepository chatMessageRepo,
                          ChatMessageService chatMessageService,
                          ChatRealtimeService chatRealtimeService,
                          ChatSessionService chatSessionService) {
        this.currentUserService = currentUserService;
        this.clubRepo = clubRepo;
        this.userRepo = userRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.chatMessageRepo = chatMessageRepo;
        this.chatMessageService = chatMessageService;
        this.chatRealtimeService = chatRealtimeService;
        this.chatSessionService = chatSessionService;
    }

    // User side: fetch own conversation with club.
    @GetMapping("/clubs/{clubId}/chat/messages")
    public ResponseEntity<?> listUserMessages(@PathVariable Integer clubId) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        if (me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can access this chat");
        }
        ChatSession session = chatSessionService.getOrCreateSession(clubId, me.getUserId());
        return ResponseEntity.ok(buildUserThreadResponse(club, me, session));
    }

    @GetMapping(value = "/clubs/{clubId}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamUserMessages(@PathVariable Integer clubId, HttpServletResponse response) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            throw new ChatStreamException(HttpStatus.NOT_FOUND, "Club not found");
        }

        User me = currentUserService.requireUser();
        if (me.getRole() != User.Role.USER) {
            throw new ChatStreamException(HttpStatus.FORBIDDEN, "Only user accounts can access this chat");
        }

        prepareSseResponse(response);
        return chatRealtimeService.subscribeUserThread(clubId, me.getUserId());
    }

    @ExceptionHandler(ChatStreamException.class)
    public ResponseEntity<String> handleChatStreamError(ChatStreamException ex) {
        return ResponseEntity.status(ex.status).body(ex.getMessage());
    }

    // User side: send message to club.
    @PostMapping("/clubs/{clubId}/chat/messages")
    @Transactional
    public ResponseEntity<?> sendUserMessage(@PathVariable Integer clubId,
                                             @RequestBody(required = false) ChatMessageCreateRequest req) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        if (me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can send chat messages");
        }

        String text = normalizeMessageText(req == null ? null : req.text());
        if (text == null) {
            return ResponseEntity.badRequest().body("Message text is required (1-500 chars)");
        }

        try {
            ChatMessageService.ChatSendResult result = chatMessageService.sendUserMessage(clubId, me.getUserId(), text);
            ChatSession session = chatSessionService.getOrCreateSession(clubId, me.getUserId());
            notifyChatSendAfterCommit(clubId, me.getUserId(), result);
            return ResponseEntity.ok(buildUserThreadResponse(club, me, session));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    // User side: mark club messages as read.
    @PostMapping("/clubs/{clubId}/chat/read")
    @Transactional
    public ResponseEntity<?> markUserRead(@PathVariable Integer clubId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        if (me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can update chat read status");
        }

        int updated = chatMessageRepo.markClubMessagesReadByUser(clubId, me.getUserId());
        if (updated > 0) {
            notifyChatChangeAfterCommit(clubId, me.getUserId(), null);
        }
        return ResponseEntity.ok(new ChatMarkReadResponse(updated));
    }

    // Club side: list conversations (latest first).
    @GetMapping("/my/clubs/{clubId}/chat/conversations")
    public ResponseEntity<?> listClubConversations(@PathVariable Integer clubId) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        List<ChatMessage> rows = chatMessageRepo.findByClubIdOrderByCreatedAtDescMessageIdDesc(clubId);
        if (rows.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        LinkedHashMap<Integer, ConversationAccumulator> grouped = new LinkedHashMap<>();
        for (ChatMessage row : rows) {
            Integer userId = row.getUserId();
            if (userId == null) continue;

            ConversationAccumulator acc = grouped.get(userId);
            if (acc == null) {
                acc = new ConversationAccumulator();
                acc.lastMessageId = row.getMessageId();
                acc.lastSender = normalizeSender(row.getSender());
                acc.lastMessageText = safe(row.getMessageText());
                acc.lastMessageAt = row.getCreatedAt();
                grouped.put(userId, acc);
            }

            acc.totalMessages++;
            if (SENDER_USER.equalsIgnoreCase(row.getSender()) && !row.isReadByClub()) {
                acc.unreadCount++;
            }
        }

        Set<Integer> userIds = grouped.keySet();
        Map<Integer, ChatSession> sessionByUserId = chatSessionService.getOrCreateSessions(clubId, userIds);
        Map<Integer, User> userById = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, it -> it));

        List<ChatConversationSummaryResponse> out = new ArrayList<>();
        for (Map.Entry<Integer, ConversationAccumulator> entry : grouped.entrySet()) {
            Integer userId = entry.getKey();
            ConversationAccumulator acc = entry.getValue();
            ChatSession session = sessionByUserId.get(userId);
            User user = userById.get(userId);

            String userName = user == null ? ("User #" + userId) : safe(user.getUsername());
            String userEmail = user == null ? "" : safe(user.getEmail());

            out.add(new ChatConversationSummaryResponse(
                    clubId,
                    userId,
                    userName,
                    userEmail,
                    acc.lastMessageId,
                    acc.lastSender,
                    acc.lastMessageText,
                    acc.lastMessageAt,
                    acc.unreadCount,
                    acc.totalMessages,
                    session == null ? null : session.getSessionId(),
                    normalizeChatMode(session),
                    session == null ? null : session.getHandoffRequestedAt(),
                    normalizeHandoffReason(session),
                    normalizeClubUnreadCount(session)
            ));
        }

        return ResponseEntity.ok(out);
    }

    @GetMapping(value = "/my/clubs/{clubId}/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamClubConversations(@PathVariable Integer clubId, HttpServletResponse response) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            throw new ChatStreamException(HttpStatus.NOT_FOUND, "Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) {
            throw new ChatStreamException(HttpStatus.valueOf(denied.getStatusCode().value()), String.valueOf(denied.getBody()));
        }

        prepareSseResponse(response);
        return chatRealtimeService.subscribeClubConversations(clubId);
    }

    // Club side: fetch one conversation.
    @GetMapping("/my/clubs/{clubId}/chat/conversations/{userId}/messages")
    public ResponseEntity<?> listClubConversationMessages(@PathVariable Integer clubId,
                                                          @PathVariable Integer userId) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        User targetUser = userRepo.findById(userId).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        ChatSession session = chatSessionService.getOrCreateSession(clubId, userId);

        List<ChatMessage> rows = chatMessageRepo.findByClubIdAndUserIdOrderByCreatedAtAscMessageIdAsc(clubId, userId);
        List<ChatMessageResponse> messages = rows.stream()
                .map(msg -> toMessageResponse(msg, safe(club.getClubName()), safe(targetUser.getUsername())))
                .toList();

        long unread = chatMessageRepo.countByClubIdAndUserIdAndSenderAndReadByClubFalse(clubId, userId, SENDER_USER);
        return ResponseEntity.ok(new ChatThreadResponse(
                clubId,
                safe(club.getClubName()),
                userId,
                safe(targetUser.getUsername()),
                unread,
                messages,
                session.getSessionId(),
                normalizeChatMode(session),
                session.getHandoffRequestedAt(),
                normalizeHandoffReason(session),
                normalizeClubUnreadCount(session)
        ));
    }

    // Club side: send reply to a user.
    @PostMapping("/my/clubs/{clubId}/chat/conversations/{userId}/messages")
    @Transactional
    public ResponseEntity<?> sendClubMessage(@PathVariable Integer clubId,
                                             @PathVariable Integer userId,
                                             @RequestBody(required = false) ChatMessageCreateRequest req) {
        Club club = clubRepo.findById(clubId).orElse(null);
        if (club == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        User targetUser = userRepo.findById(userId).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        String text = normalizeMessageText(req == null ? null : req.text());
        if (text == null) {
            return ResponseEntity.badRequest().body("Message text is required (1-500 chars)");
        }

        try {
            ChatMessageService.ChatSendResult result = chatMessageService.sendClubMessage(clubId, userId, text);
            notifyChatSendAfterCommit(clubId, userId, result);
            return ResponseEntity.ok(toMessageResponse(result.responseMessage(), safe(club.getClubName()), safe(targetUser.getUsername())));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    // Club side: mark user messages as read.
    @PostMapping("/my/clubs/{clubId}/chat/conversations/{userId}/read")
    @Transactional
    public ResponseEntity<?> markClubRead(@PathVariable Integer clubId,
                                          @PathVariable Integer userId) {
        if (!clubRepo.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        ResponseEntity<?> denied = requireClubAdmin(me, clubId);
        if (denied != null) return denied;

        if (!userRepo.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        ChatSession session = chatSessionService.getOrCreateSession(clubId, userId);
        int updated = chatMessageRepo.markUserMessagesReadByClub(clubId, userId);
        if (updated > 0 || normalizeClubUnreadCount(session) > 0) {
            chatSessionService.resetClubUnreadCount(session.getSessionId());
            notifyChatChangeAfterCommit(clubId, userId, null);
        }
        return ResponseEntity.ok(new ChatMarkReadResponse(updated));
    }

    private void notifyChatChangeAfterCommit(Integer clubId, Integer userId, Integer messageId) {
        chatRealtimeService.afterCommit(() -> {
            chatRealtimeService.notifyThreadUpdated(clubId, userId, messageId);
            chatRealtimeService.notifyConversationUpdated(clubId, userId, messageId);
        });
    }

    private void notifyUserThreadAfterCommit(Integer clubId, Integer userId, Integer messageId) {
        chatRealtimeService.afterCommit(() -> chatRealtimeService.notifyThreadUpdated(clubId, userId, messageId));
    }

    private void notifyChatSendAfterCommit(Integer clubId, Integer userId, ChatMessageService.ChatSendResult result) {
        if (result == null) {
            return;
        }
        Integer messageId = result.latestMessageId();
        if (result.notifyUserThread() && result.notifyClubConversation()) {
            notifyChatChangeAfterCommit(clubId, userId, messageId);
            return;
        }
        if (result.notifyUserThread()) {
            notifyUserThreadAfterCommit(clubId, userId, messageId);
        }
    }

    private static void prepareSseResponse(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");
    }

    private ResponseEntity<?> requireClubAdmin(User me, Integer clubId) {
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can access club chat");
        }
        if (me.getRole() == User.Role.ADMIN) {
            return null;
        }

        boolean allowed = clubAdminRepo.existsByUserIdAndClubId(me.getUserId(), clubId);
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only access your own club chat");
        }
        return null;
    }

    private ChatMessageResponse toMessageResponse(ChatMessage msg, String clubName, String userName) {
        MessageSenderType senderType = MessageSenderType.fromRaw(msg.getSender());
        String authorName = switch (senderType) {
            case CLUB -> clubName;
            case ASSISTANT -> "Assistant";
            case SYSTEM -> "System";
            case USER -> userName;
        };
        return new ChatMessageResponse(
                msg.getMessageId(),
                msg.getClubId(),
                msg.getUserId(),
                normalizeSender(msg.getSender()),
                safe(msg.getMessageText()),
                authorName,
                safe(msg.getAnswerSource()),
                msg.getMatchedFaqId(),
                msg.isHandoffSuggested(),
                msg.isReadByClub(),
                msg.isReadByUser(),
                msg.getCreatedAt()
        );
    }

    private ChatThreadResponse buildUserThreadResponse(Club club, User me, ChatSession session) {
        List<ChatMessage> rows = chatMessageRepo.findByClubIdAndUserIdOrderByCreatedAtAscMessageIdAsc(club.getClubId(), me.getUserId());
        List<ChatMessageResponse> messages = rows.stream()
                .map(msg -> toMessageResponse(msg, safe(club.getClubName()), safe(me.getUsername())))
                .toList();

        long unread = chatMessageRepo.countByClubIdAndUserIdAndSenderAndReadByUserFalse(club.getClubId(), me.getUserId(), SENDER_CLUB);
        return new ChatThreadResponse(
                club.getClubId(),
                safe(club.getClubName()),
                me.getUserId(),
                safe(me.getUsername()),
                unread,
                messages,
                session.getSessionId(),
                normalizeChatMode(session),
                session.getHandoffRequestedAt(),
                normalizeHandoffReason(session),
                normalizeClubUnreadCount(session)
        );
    }

    private static String normalizeSender(String sender) {
        return switch (MessageSenderType.fromRaw(sender)) {
            case CLUB -> "club";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            case USER -> "user";
        };
    }

    private static String normalizeChatMode(ChatSession session) {
        if (session == null || session.getChatMode() == null) {
            return "AI";
        }
        return session.getChatMode().name();
    }

    private static String normalizeHandoffReason(ChatSession session) {
        HandoffReason reason = session == null ? null : session.getHandoffReason();
        return reason == null ? null : reason.name();
    }

    private static int normalizeClubUnreadCount(ChatSession session) {
        Integer unreadCount = session == null ? null : session.getClubUnreadCount();
        return unreadCount == null ? 0 : Math.max(0, unreadCount);
    }

    private static String normalizeMessageText(String raw) {
        String text = String.valueOf(raw == null ? "" : raw)
                .replace("\r\n", "\n")
                .replace("\u0000", "")
                .trim();
        if (text.isEmpty()) return null;
        if (text.length() > MAX_MESSAGE_LENGTH) return null;
        return text;
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private static class ConversationAccumulator {
        Integer lastMessageId;
        String lastSender;
        String lastMessageText;
        LocalDateTime lastMessageAt;
        long unreadCount;
        long totalMessages;
    }

    private static class ChatStreamException extends RuntimeException {
        private final HttpStatus status;

        private ChatStreamException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }
    }
}
