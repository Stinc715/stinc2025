package com.clubportal.controller;

import com.clubportal.dto.ClubChatKbEntryUpsertRequest;
import com.clubportal.model.User;
import com.clubportal.repository.ClubAdminRepository;
import com.clubportal.repository.ClubRepository;
import com.clubportal.service.ClubChatKbEmbeddingBackfillService;
import com.clubportal.service.ClubChatKbService;
import com.clubportal.service.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/my/clubs/{clubId}/chat-kb")
public class ClubChatKbController {

    private static final Logger log = LoggerFactory.getLogger(ClubChatKbController.class);

    private final ClubRepository clubRepository;
    private final ClubAdminRepository clubAdminRepository;
    private final CurrentUserService currentUserService;
    private final ClubChatKbService clubChatKbService;
    private final ClubChatKbEmbeddingBackfillService clubChatKbEmbeddingBackfillService;

    public ClubChatKbController(ClubRepository clubRepository,
                                ClubAdminRepository clubAdminRepository,
                                CurrentUserService currentUserService,
                                ClubChatKbService clubChatKbService,
                                ClubChatKbEmbeddingBackfillService clubChatKbEmbeddingBackfillService) {
        this.clubRepository = clubRepository;
        this.clubAdminRepository = clubAdminRepository;
        this.currentUserService = currentUserService;
        this.clubChatKbService = clubChatKbService;
        this.clubChatKbEmbeddingBackfillService = clubChatKbEmbeddingBackfillService;
    }

    @GetMapping
    public ResponseEntity<?> listEntries(@PathVariable Integer clubId) {
        ResponseEntity<?> denied = requireClubAdmin(clubId);
        if (denied != null) {
            return denied;
        }
        return ResponseEntity.ok(clubChatKbService.listEntries(clubId));
    }

    @PostMapping
    public ResponseEntity<?> createEntry(@PathVariable Integer clubId,
                                         @RequestBody ClubChatKbEntryUpsertRequest request) {
        ResponseEntity<?> denied = requireClubAdmin(clubId);
        if (denied != null) {
            return denied;
        }
        var response = clubChatKbService.createEntry(clubId, request);
        log.info("[CLUB_CHAT_DEBUG] KB_SAVE http_complete: action=create, clubId={}, entryId={}, status={}",
                clubId,
                response.id(),
                HttpStatus.CREATED.value());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<?> updateEntry(@PathVariable Integer clubId,
                                         @PathVariable Integer entryId,
                                         @RequestBody ClubChatKbEntryUpsertRequest request) {
        ResponseEntity<?> denied = requireClubAdmin(clubId);
        if (denied != null) {
            return denied;
        }
        var response = clubChatKbService.updateEntry(clubId, entryId, request);
        log.info("[CLUB_CHAT_DEBUG] KB_SAVE http_complete: action=update, clubId={}, entryId={}, status={}",
                clubId,
                response.id(),
                HttpStatus.OK.value());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<?> deleteEntry(@PathVariable Integer clubId,
                                         @PathVariable Integer entryId) {
        ResponseEntity<?> denied = requireClubAdmin(clubId);
        if (denied != null) {
            return denied;
        }
        clubChatKbService.deleteEntry(clubId, entryId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/backfill")
    public ResponseEntity<?> backfillClub(@PathVariable Integer clubId,
                                          @RequestParam(defaultValue = "false") boolean forceRebuild,
                                          @RequestParam(defaultValue = "false") boolean dryRun) {
        ResponseEntity<?> denied = requireClubAdmin(clubId);
        if (denied != null) {
            return denied;
        }
        return ResponseEntity.ok(clubChatKbEmbeddingBackfillService.backfillClub(clubId, forceRebuild, dryRun));
    }

    private ResponseEntity<?> requireClubAdmin(Integer clubId) {
        if (!clubRepository.existsById(clubId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Club not found");
        }

        User me = currentUserService.requireUser();
        if (me.getRole() == null || me.getRole() == User.Role.USER) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only club accounts can access this resource");
        }

        boolean isAdmin = me.getRole() == User.Role.ADMIN;
        boolean isClubAdmin = clubAdminRepository.existsByUserIdAndClubId(me.getUserId(), clubId);
        if (!isAdmin && !isClubAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only access your own club");
        }
        return null;
    }
}
