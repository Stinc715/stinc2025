package com.clubportal.service;

public record ClubChatAiReplyDecision(
        String replyText,
        String answerSource,
        Integer matchedFaqId,
        String matchedQuestion,
        Double similarityScore,
        Double secondBestScore,
        boolean handoffSuggested
) {
    public static ClubChatAiReplyDecision clubFaq(String replyText,
                                                  Integer matchedFaqId,
                                                  String matchedQuestion,
                                                  Double similarityScore,
                                                  Double secondBestScore) {
        return new ClubChatAiReplyDecision(
                safe(replyText),
                "CLUB_FAQ",
                matchedFaqId,
                safe(matchedQuestion),
                similarityScore,
                secondBestScore,
                false
        );
    }

    public static ClubChatAiReplyDecision standard(String replyText, String answerSource) {
        return standard(replyText, answerSource, false);
    }

    public static ClubChatAiReplyDecision standard(String replyText, String answerSource, boolean handoffSuggested) {
        return new ClubChatAiReplyDecision(
                safe(replyText),
                safe(answerSource),
                null,
                "",
                null,
                null,
                handoffSuggested
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
