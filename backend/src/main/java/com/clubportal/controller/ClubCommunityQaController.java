package com.clubportal.controller;

import com.clubportal.dto.ClubCommunityAnswerCreateRequest;
import com.clubportal.dto.ClubCommunityQuestionBoardResponse;
import com.clubportal.dto.ClubCommunityQuestionCreateRequest;
import com.clubportal.service.ClubCommunityQaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/clubs/{clubId}/community-questions")
public class ClubCommunityQaController {

    private final ClubCommunityQaService clubCommunityQaService;

    public ClubCommunityQaController(ClubCommunityQaService clubCommunityQaService) {
        this.clubCommunityQaService = clubCommunityQaService;
    }

    @GetMapping
    public ResponseEntity<?> getBoard(@PathVariable Integer clubId) {
        try {
            ClubCommunityQuestionBoardResponse board = clubCommunityQaService.getBoard(clubId);
            return ResponseEntity.ok()
                    .header("Cache-Control", "private, no-store, max-age=0")
                    .header("Pragma", "no-cache")
                    .body(board);
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .header("Cache-Control", "private, no-store, max-age=0")
                    .header("Pragma", "no-cache")
                    .body(ex.getReason());
        }
    }

    @PostMapping
    public ResponseEntity<?> askQuestion(@PathVariable Integer clubId,
                                         @RequestBody(required = false) ClubCommunityQuestionCreateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(clubCommunityQaService.askQuestion(clubId, request));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<?> updateQuestion(@PathVariable Integer clubId,
                                            @PathVariable Integer questionId,
                                            @RequestBody(required = false) ClubCommunityQuestionCreateRequest request) {
        try {
            return ResponseEntity.ok(clubCommunityQaService.updateQuestion(clubId, questionId, request));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer clubId,
                                            @PathVariable Integer questionId) {
        try {
            clubCommunityQaService.deleteQuestion(clubId, questionId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<?> answerQuestion(@PathVariable Integer clubId,
                                            @PathVariable Integer questionId,
                                            @RequestBody(required = false) ClubCommunityAnswerCreateRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(clubCommunityQaService.answerQuestion(clubId, questionId, request));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @PutMapping("/{questionId}/answers/{answerId}")
    public ResponseEntity<?> updateAnswer(@PathVariable Integer clubId,
                                          @PathVariable Integer questionId,
                                          @PathVariable Integer answerId,
                                          @RequestBody(required = false) ClubCommunityAnswerCreateRequest request) {
        try {
            return ResponseEntity.ok(clubCommunityQaService.updateAnswer(clubId, questionId, answerId, request));
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }

    @DeleteMapping("/{questionId}/answers/{answerId}")
    public ResponseEntity<?> deleteAnswer(@PathVariable Integer clubId,
                                          @PathVariable Integer questionId,
                                          @PathVariable Integer answerId) {
        try {
            clubCommunityQaService.deleteAnswer(clubId, questionId, answerId);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException ex) {
            return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
        }
    }
}
