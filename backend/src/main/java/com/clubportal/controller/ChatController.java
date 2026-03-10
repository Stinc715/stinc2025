package com.clubportal.controller;

import com.clubportal.dto.ChatConversationSummaryResponse;
import com.clubportal.dto.ChatMarkReadResponse;
import com.clubportal.dto.ChatMessageCreateRequest;
import com.clubportal.dto.ChatMessageResponse;
import com.clubportal.dto.ChatThreadResponse;
import com.clubportal.model.ChatMessage;
import com.clubportal.model.Club;
import com.clubportal.model.User;
import com.clubportal.repository.ChatMessageRepository;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.repository.UserRepository;
import com.clubportal.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public ChatController(CurrentUserService currentUserService,
                          ClubRepository clubRepo,
                          UserRepository userRepo,
                          ClubAdminRepository clubAdminRepo,
                          ChatMessageRepository chatMessageRepo) {
        this.currentUserService = currentUserService;
        this.clubRepo = clubRepo;
        this.userRepo = userRepo;
        this.clubAdminRepo = clubAdminRepo;
        this.chatMessageRepo = chatMessageRepo;
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

        List<ChatMessage> rows = chatMessageRepo.findByClubIdAndUserIdOrderByCreatedAtAscMessageIdAsc(clubId, me.getUserId());
        List<ChatMessageResponse> messages = rows.stream()
                .map(msg -> toMessageResponse(msg, safe(club.getClubName()), safe(me.getUsername())))
                .toList();

        long unread = chatMessageRepo.countByClubIdAndUserIdAndSenderAndReadByUserFalse(clubId, me.getUserId(), SENDER_CLUB);
        return ResponseEntity.ok(new ChatThreadResponse(
                clubId,
                safe(club.getClubName()),
                me.getUserId(),
                safe(me.getUsername()),
                unread,
                messages
        ));
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

        ChatMessage row = new ChatMessage();
        row.setClubId(clubId);
        row.setUserId(me.getUserId());
        row.setSender(SENDER_USER);
        row.setMessageText(text);
        row.setReadByClub(false);
        row.setReadByUser(true);

        ChatMessage saved = chatMessageRepo.save(row);
        return ResponseEntity.ok(toMessageResponse(saved, safe(club.getClubName()), safe(me.getUsername())));
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
        Map<Integer, User> userById = userRepo.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, it -> it));

        List<ChatConversationSummaryResponse> out = new ArrayList<>();
        for (Map.Entry<Integer, ConversationAccumulator> entry : grouped.entrySet()) {
            Integer userId = entry.getKey();
            ConversationAccumulator acc = entry.getValue();
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
                    acc.totalMessages
            ));
        }

        return ResponseEntity.ok(out);
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
                messages
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

        ChatMessage row = new ChatMessage();
        row.setClubId(clubId);
        row.setUserId(userId);
        row.setSender(SENDER_CLUB);
        row.setMessageText(text);
        row.setReadByClub(true);
        row.setReadByUser(false);

        ChatMessage saved = chatMessageRepo.save(row);
        return ResponseEntity.ok(toMessageResponse(saved, safe(club.getClubName()), safe(targetUser.getUsername())));
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

        int updated = chatMessageRepo.markUserMessagesReadByClub(clubId, userId);
        return ResponseEntity.ok(new ChatMarkReadResponse(updated));
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
        boolean fromClub = SENDER_CLUB.equalsIgnoreCase(msg.getSender());
        String authorName = fromClub ? clubName : userName;
        return new ChatMessageResponse(
                msg.getMessageId(),
                msg.getClubId(),
                msg.getUserId(),
                normalizeSender(msg.getSender()),
                safe(msg.getMessageText()),
                authorName,
                msg.isReadByClub(),
                msg.isReadByUser(),
                msg.getCreatedAt()
        );
    }

    private static String normalizeSender(String sender) {
        return SENDER_CLUB.equalsIgnoreCase(safe(sender)) ? "club" : "user";
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
}

