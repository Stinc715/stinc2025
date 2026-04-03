package com.clubportal.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ClubCommunityQuestionResponse(
        Integer questionId,
        Integer authorUserId,
        String authorName,
        String questionText,
        boolean clubAnswered,
        int answerCount,
        boolean canEdit,
        boolean canDelete,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ClubCommunityAnswerResponse> answers
) {
}
