package com.clubportal.controller;

import com.clubportal.dto.ChatSessionHandoffRequest;
import com.clubportal.dto.ChatSessionResponse;
import com.clubportal.model.ChatSession;
import com.clubportal.model.HandoffReason;
import com.clubportal.model.User;
import com.clubportal.service.ChatMessageService;
import com.clubportal.service.ChatRealtimeService;
import com.clubportal.service.ChatSessionService;
import com.clubportal.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat-sessions")
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final CurrentUserService currentUserService;
    private final ChatMessageService chatMessageService;
    private final ChatRealtimeService chatRealtimeService;

    public ChatSessionController(ChatSessionService chatSessionService,
                                 CurrentUserService currentUserService,
                                 ChatMessageService chatMessageService,
                                 ChatRealtimeService chatRealtimeService) {
        this.chatSessionService = chatSessionService;
        this.currentUserService = currentUserService;
        this.chatMessageService = chatMessageService;
        this.chatRealtimeService = chatRealtimeService;
    }

    @PostMapping("/{sessionId}/handoff")
    @Transactional
    public ResponseEntity<?> requestHandoff(@PathVariable Integer sessionId,
                                            @RequestBody(required = false) ChatSessionHandoffRequest request) {
        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() != User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only user accounts can request human handoff");
        }

        ChatSession existing = chatSessionService.requireSession(sessionId);
        if (!me.getUserId().equals(existing.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only request handoff for your own chat session");
        }

        ChatSession session = chatSessionService.requestHandoff(sessionId, HandoffReason.fromRaw(request == null ? null : request.reason()));
        chatMessageService.saveSystemMessage(
                session.getClubId(),
                session.getUserId(),
                systemMessageFor(session.getHandoffReason()),
                false,
                true
        );
        chatRealtimeService.afterCommit(() -> {
            chatRealtimeService.notifyThreadUpdated(session.getClubId(), session.getUserId(), null);
            chatRealtimeService.notifyConversationUpdated(session.getClubId(), session.getUserId(), null);
        });
        return ResponseEntity.ok(toResponse(session));
    }

    private static ChatSessionResponse toResponse(ChatSession session) {
        return new ChatSessionResponse(
                session.getSessionId(),
                session.getChatMode() == null ? "AI" : session.getChatMode().name(),
                session.getHandoffRequestedAt(),
                session.getHandoffReason() == null ? null : session.getHandoffReason().name(),
                session.getClubUnreadCount() == null ? 0 : Math.max(0, session.getClubUnreadCount())
        );
    }

    private static String systemMessageFor(HandoffReason reason) {
        return switch (reason == null ? HandoffReason.OTHER : reason) {
            case USER_REQUEST -> "Member requested human support.";
            case REFUND -> "Member requested human support for a refund issue.";
            case PAYMENT_ISSUE -> "Member requested human support for a payment issue.";
            case HARASSMENT -> "Member requested urgent human support for a safety issue.";
            case POLICY_EXCEPTION -> "Member requested human support for a special case.";
            case OTHER -> "Member requested human support.";
        };
    }
}
