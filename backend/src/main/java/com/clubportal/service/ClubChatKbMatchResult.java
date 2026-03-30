package com.clubportal.service;

public record ClubChatKbMatchResult(
        boolean hit,
        Integer matchedEntryId,
        String matchedQuestion,
        String matchedReply,
        Double bestScore,
        Double secondBestScore,
        ClubChatKbMatchRejectReason rejectReason,
        String source,
        String normalizedQuestion
) {
    public static ClubChatKbMatchResult hit(Integer matchedEntryId,
                                            String matchedQuestion,
                                            String matchedReply,
                                            Double bestScore,
                                            Double secondBestScore,
                                            String normalizedQuestion) {
        return new ClubChatKbMatchResult(
                true,
                matchedEntryId,
                matchedQuestion,
                matchedReply,
                bestScore,
                secondBestScore,
                ClubChatKbMatchRejectReason.NONE,
                "CLUB_FAQ",
                normalizedQuestion
        );
    }

    public static ClubChatKbMatchResult miss(ClubChatKbMatchRejectReason rejectReason,
                                             Double bestScore,
                                             Double secondBestScore,
                                             String normalizedQuestion) {
        return new ClubChatKbMatchResult(
                false,
                null,
                "",
                "",
                bestScore,
                secondBestScore,
                rejectReason == null ? ClubChatKbMatchRejectReason.NO_VALID_EMBEDDINGS : rejectReason,
                "CLUB_FAQ",
                normalizedQuestion == null ? "" : normalizedQuestion
        );
    }
}
