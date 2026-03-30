package com.clubportal.service;

import com.clubportal.config.ClubChatKbGuardProperties;
import com.clubportal.config.ClubChatKbMatcherProperties;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Service
public class ClubChatKbResponseGuard {

    private final ClubChatKbGuardProperties guardProperties;
    private final ClubChatKbMatcherProperties matcherProperties;
    private final ClubChatKbEmbeddingTextNormalizer embeddingTextNormalizer;

    public ClubChatKbResponseGuard(ClubChatKbGuardProperties guardProperties,
                                   ClubChatKbMatcherProperties matcherProperties,
                                   ClubChatKbEmbeddingTextNormalizer embeddingTextNormalizer) {
        this.guardProperties = guardProperties;
        this.matcherProperties = matcherProperties;
        this.embeddingTextNormalizer = embeddingTextNormalizer;
    }

    public Decision evaluate(String userMessage, ClubChatKbMatchResult matchResult) {
        String normalizedQuestion = embeddingTextNormalizer.normalize(userMessage);

        String highRiskKeyword = findMatchedKeyword(normalizedQuestion, guardProperties.getHighRiskKeywords());
        if (highRiskKeyword != null) {
            return Decision.reject(
                    ClubChatKbGuardRejectReason.HIGH_RISK_KEYWORD,
                    "matched high-risk keyword: " + highRiskKeyword
            );
        }

        String realtimeKeyword = findMatchedKeyword(normalizedQuestion, guardProperties.getRealtimeKeywords());
        if (realtimeKeyword != null) {
            return Decision.reject(
                    ClubChatKbGuardRejectReason.REALTIME_KEYWORD,
                    "matched realtime keyword: " + realtimeKeyword
            );
        }

        Double bestScore = matchResult == null ? null : matchResult.bestScore();
        Double secondBestScore = matchResult == null ? null : matchResult.secondBestScore();
        if (bestScore != null
                && secondBestScore != null
                && (bestScore - secondBestScore) < matcherProperties.getMinScoreGap()) {
            return Decision.reject(
                    ClubChatKbGuardRejectReason.AMBIGUOUS_MATCH,
                    "score gap below threshold: gap=" + String.format(java.util.Locale.ROOT, "%.4f", bestScore - secondBestScore)
            );
        }

        return Decision.permit();
    }

    private String findMatchedKeyword(String normalizedQuestion, List<String> candidates) {
        if (normalizedQuestion == null || normalizedQuestion.isBlank() || candidates == null || candidates.isEmpty()) {
            return null;
        }

        for (String rawCandidate : candidates) {
            String candidate = embeddingTextNormalizer.normalize(rawCandidate);
            if (candidate.isBlank()) {
                continue;
            }
            if (matchesKeyword(normalizedQuestion, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private boolean matchesKeyword(String normalizedQuestion, String keyword) {
        if (containsHan(keyword)) {
            return normalizedQuestion.contains(keyword);
        }
        if (keyword.contains(" ")) {
            return normalizedQuestion.contains(keyword);
        }
        Pattern boundaryPattern = Pattern.compile("(^|\\s)" + Pattern.quote(keyword) + "(\\s|$)");
        return boundaryPattern.matcher(normalizedQuestion).find();
    }

    private boolean containsHan(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.codePoints()
                .mapToObj(Character.UnicodeScript::of)
                .filter(Objects::nonNull)
                .anyMatch(script -> script == Character.UnicodeScript.HAN);
    }

    public record Decision(
            boolean allow,
            ClubChatKbGuardRejectReason rejectReason,
            String detail
    ) {
        public static Decision permit() {
            return new Decision(true, ClubChatKbGuardRejectReason.NONE, "");
        }

        public static Decision reject(ClubChatKbGuardRejectReason rejectReason, String detail) {
            return new Decision(false, rejectReason == null ? ClubChatKbGuardRejectReason.NONE : rejectReason, detail == null ? "" : detail);
        }
    }
}
