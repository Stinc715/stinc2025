package com.clubportal.controller;

import com.clubportal.model.User;
import com.clubportal.service.ClubChatKbEmbeddingBackfillService;
import com.clubportal.service.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/chat-kb")
public class ClubChatKbAdminController {

    private final CurrentUserService currentUserService;
    private final ClubChatKbEmbeddingBackfillService clubChatKbEmbeddingBackfillService;

    public ClubChatKbAdminController(CurrentUserService currentUserService,
                                     ClubChatKbEmbeddingBackfillService clubChatKbEmbeddingBackfillService) {
        this.currentUserService = currentUserService;
        this.clubChatKbEmbeddingBackfillService = clubChatKbEmbeddingBackfillService;
    }

    @PostMapping("/backfill")
    public ResponseEntity<?> backfillAll(@RequestParam(defaultValue = "false") boolean forceRebuild,
                                         @RequestParam(defaultValue = "false") boolean dryRun) {
        ResponseEntity<?> denied = requireSystemAdmin();
        if (denied != null) {
            return denied;
        }
        return ResponseEntity.ok(clubChatKbEmbeddingBackfillService.backfillAll(forceRebuild, dryRun));
    }

    @PostMapping("/clubs/{clubId}/backfill")
    public ResponseEntity<?> backfillClub(@PathVariable Integer clubId,
                                          @RequestParam(defaultValue = "false") boolean forceRebuild,
                                          @RequestParam(defaultValue = "false") boolean dryRun) {
        ResponseEntity<?> denied = requireSystemAdmin();
        if (denied != null) {
            return denied;
        }
        return ResponseEntity.ok(clubChatKbEmbeddingBackfillService.backfillClub(clubId, forceRebuild, dryRun));
    }

    private ResponseEntity<?> requireSystemAdmin() {
        User me = currentUserService.requireUser();
        if (me.getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only system admins can access this resource");
        }
        return null;
    }
}
