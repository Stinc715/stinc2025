package com.clubportal.dto;

import java.util.List;

public record ClubCommunityQuestionBoardResponse(
        Integer clubId,
        boolean canAsk,
        boolean canAnswer,
        boolean canReplyAsClub,
        int questionCount,
        List<ClubCommunityQuestionResponse> questions
) {
}
