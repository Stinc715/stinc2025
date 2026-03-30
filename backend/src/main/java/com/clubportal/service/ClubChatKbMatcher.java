package com.clubportal.service;

import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.model.ClubChatKbLanguage;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class ClubChatKbMatcher {

    private static final Logger log = LoggerFactory.getLogger(ClubChatKbMatcher.class);
    private static final int MIN_CONFIDENT_SCORE = 32;
    private static final int MIN_SCORE_LEAD = 8;
    private static final Set<String> EN_STOP_WORDS = Set.of(
            "a", "an", "and", "any", "are", "at", "be", "can", "club", "do", "for", "how", "i", "if",
            "in", "is", "it", "me", "my", "of", "on", "or", "please", "the", "this", "to", "what",
            "when", "where", "with", "you", "your"
    );

    private final ClubChatKbEntryRepository clubChatKbEntryRepository;
    private final ClubChatKbSupport clubChatKbSupport;
    private final ClubChatKbSemanticScorer clubChatKbSemanticScorer;
    private final ChatLanguageDetector chatLanguageDetector = new ChatLanguageDetector();

    public ClubChatKbMatcher(ClubChatKbEntryRepository clubChatKbEntryRepository,
                             ClubChatKbSupport clubChatKbSupport,
                             ClubChatKbSemanticScorer clubChatKbSemanticScorer) {
        this.clubChatKbEntryRepository = clubChatKbEntryRepository;
        this.clubChatKbSupport = clubChatKbSupport;
        this.clubChatKbSemanticScorer = clubChatKbSemanticScorer;
    }

    public Optional<KbMatch> findBestMatch(Integer clubId, String userMessage) {
        ChatLanguage language = chatLanguageDetector.detect(userMessage);
        log.info("[CLUB_CHAT_DEBUG] KB_MATCH start: clubId={}, language={}, message=\"{}\"",
                clubId,
                language,
                safe(userMessage));

        if (clubId == null) {
            log.info("[CLUB_CHAT_DEBUG] KB_MATCH miss: clubId={}, reason=missing club id", clubId);
            return Optional.empty();
        }

        String normalizedMessage = normalizeText(userMessage);
        if (normalizedMessage.isBlank()) {
            log.info("[CLUB_CHAT_DEBUG] KB_MATCH miss: clubId={}, reason=blank normalized message", clubId);
            return Optional.empty();
        }

        List<ClubChatKbEntry> enabledEntries = clubChatKbEntryRepository
                .findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(clubId)
                .stream()
                .filter(entry -> Boolean.TRUE.equals(entry.getEnabled()))
                .toList();
        log.info("[CLUB_CHAT_DEBUG] KB_MATCH entries: clubId={}, enabledCount={}", clubId, enabledEntries.size());

        if (enabledEntries.isEmpty()) {
            log.info("[CLUB_CHAT_DEBUG] KB_MATCH miss: clubId={}, reason=no enabled entries", clubId);
            return Optional.empty();
        }

        Set<String> messageTerms = extractTerms(normalizedMessage);
        MatchCandidate best = null;
        MatchCandidate runnerUp = null;

        for (ClubChatKbEntry entry : enabledEntries) {
            if (!languageMatches(entry.getLanguage(), language)) {
                continue;
            }

            MatchCandidate candidate = scoreEntry(entry, normalizedMessage, messageTerms, language);
            if (candidate.score() <= 0) {
                continue;
            }

            log.info("[CLUB_CHAT_DEBUG] KB_MATCH candidate: entryId={}, score={}, title=\"{}\"",
                    entry.getId(),
                    candidate.score(),
                    safe(entry.getQuestionTitle()));

            if (best == null || candidate.score() > best.score()) {
                runnerUp = best;
                best = candidate;
            } else if (runnerUp == null || candidate.score() > runnerUp.score()) {
                runnerUp = candidate;
            }
        }

        if (best == null) {
            log.info("[CLUB_CHAT_DEBUG] KB_MATCH miss: clubId={}, reason=no confident candidate", clubId);
            return Optional.empty();
        }
        if (best.score() < MIN_CONFIDENT_SCORE) {
            log.info("[CLUB_CHAT_DEBUG] KB_MATCH miss: clubId={}, reason=top score below threshold, topScore={}",
                    clubId,
                    best.score());
            return Optional.empty();
        }
        if (runnerUp != null && (best.score() - runnerUp.score()) < MIN_SCORE_LEAD) {
            log.info("[CLUB_CHAT_DEBUG] KB_MATCH miss: clubId={}, reason=ambiguous top candidates, topScore={}, runnerUpScore={}",
                    clubId,
                    best.score(),
                    runnerUp.score());
            return Optional.empty();
        }

        log.info("[CLUB_CHAT_DEBUG] KB_MATCH hit: entryId={}, clubId={}, score={}",
                best.entry().getId(),
                clubId,
                best.score());
        return Optional.of(new KbMatch(
                best.entry().getId(),
                best.entry().getClubId(),
                safe(best.entry().getQuestionTitle()),
                safe(best.entry().getAnswerText()),
                best.score()
        ));
    }

    private MatchCandidate scoreEntry(ClubChatKbEntry entry,
                                      String normalizedMessage,
                                      Set<String> messageTerms,
                                      ChatLanguage language) {
        int titleScore = scorePhrase(entry.getQuestionTitle(), normalizedMessage, messageTerms, 44, 28, 24);
        int keywordScore = scoreKeywords(entry.getTriggerKeywords(), normalizedMessage, messageTerms);
        int exampleScore = scoreExamples(entry.getExampleQuestions(), normalizedMessage, messageTerms);
        int languageScore = languageScore(entry.getLanguage(), language);
        int priorityBonus = Math.min(Math.max(entry.getPriority() == null ? 0 : entry.getPriority(), 0), 10);
        int semanticScore = Math.max(0, clubChatKbSemanticScorer.score(normalizedMessage, entry, language));
        int totalScore = titleScore + keywordScore + exampleScore + languageScore + priorityBonus + semanticScore;
        return new MatchCandidate(entry, totalScore);
    }

    private int scoreKeywords(String rawKeywords, String normalizedMessage, Set<String> messageTerms) {
        int total = 0;
        for (String keyword : clubChatKbSupport.decodeList(rawKeywords)) {
            total += scorePhrase(keyword, normalizedMessage, messageTerms, 26, 18, 14);
        }
        return Math.min(total, 36);
    }

    private int scoreExamples(String rawExamples, String normalizedMessage, Set<String> messageTerms) {
        int best = 0;
        for (String example : clubChatKbSupport.decodeList(rawExamples)) {
            best = Math.max(best, scorePhrase(example, normalizedMessage, messageTerms, 70, 48, 30));
        }
        return best;
    }

    private int scorePhrase(String rawCandidate,
                            String normalizedMessage,
                            Set<String> messageTerms,
                            int exactScore,
                            int containsScore,
                            int overlapCap) {
        String candidate = normalizeText(rawCandidate);
        if (candidate.isBlank()) {
            return 0;
        }
        if (candidate.equals(normalizedMessage)) {
            return exactScore;
        }
        if (candidate.length() >= 5 && (normalizedMessage.contains(candidate) || candidate.contains(normalizedMessage))) {
            return containsScore;
        }

        Set<String> candidateTerms = extractTerms(candidate);
        if (!candidateTerms.isEmpty() && !messageTerms.isEmpty()) {
            long sharedTerms = candidateTerms.stream().filter(messageTerms::contains).count();
            if (sharedTerms > 0) {
                double coverage = (double) sharedTerms / (double) candidateTerms.size();
                int overlapScore = (int) Math.round(sharedTerms * 5 + coverage * overlapCap);
                return Math.min(overlapScore, overlapCap);
            }
        }

        if (containsHan(candidate) && containsHan(normalizedMessage)) {
            int commonLength = longestCommonSubstring(candidate, normalizedMessage);
            if (commonLength >= 4) {
                return Math.max(12, containsScore - 6);
            }
        }
        return 0;
    }

    private boolean languageMatches(ClubChatKbLanguage entryLanguage, ChatLanguage userLanguage) {
        ClubChatKbLanguage normalizedLanguage = entryLanguage == null ? ClubChatKbLanguage.ANY : entryLanguage;
        return normalizedLanguage == ClubChatKbLanguage.ANY
                || (normalizedLanguage == ClubChatKbLanguage.EN && userLanguage == ChatLanguage.EN)
                || (normalizedLanguage == ClubChatKbLanguage.ZH && userLanguage == ChatLanguage.ZH);
    }

    private int languageScore(ClubChatKbLanguage entryLanguage, ChatLanguage userLanguage) {
        ClubChatKbLanguage normalizedLanguage = entryLanguage == null ? ClubChatKbLanguage.ANY : entryLanguage;
        if (normalizedLanguage == ClubChatKbLanguage.ANY) {
            return 2;
        }
        return languageMatches(normalizedLanguage, userLanguage) ? 10 : 0;
    }

    private Set<String> extractTerms(String normalizedText) {
        if (normalizedText == null || normalizedText.isBlank()) {
            return Set.of();
        }

        Set<String> terms = new LinkedHashSet<>();
        for (String token : normalizedText.split(" ")) {
            String trimmed = token.trim();
            if (trimmed.isBlank()) {
                continue;
            }
            if (containsHan(trimmed)) {
                terms.add(trimmed);
                if (trimmed.length() >= 2) {
                    for (int i = 0; i < trimmed.length() - 1; i++) {
                        terms.add(trimmed.substring(i, i + 2));
                    }
                }
                continue;
            }
            if (trimmed.length() < 2 || EN_STOP_WORDS.contains(trimmed)) {
                continue;
            }
            terms.add(trimmed);
        }
        return terms;
    }

    private static String normalizeText(String raw) {
        if (raw == null) {
            return "";
        }
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[\\p{P}\\p{S}]+", " ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        return normalized;
    }

    private static boolean containsHan(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeScript.of(text.charAt(i)) == Character.UnicodeScript.HAN) {
                return true;
            }
        }
        return false;
    }

    private static int longestCommonSubstring(String left, String right) {
        if (left.isBlank() || right.isBlank()) {
            return 0;
        }
        int[][] dp = new int[left.length() + 1][right.length() + 1];
        int max = 0;
        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                if (left.charAt(i - 1) == right.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    max = Math.max(max, dp[i][j]);
                }
            }
        }
        return max;
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\"", "\\\"").trim();
    }

    private record MatchCandidate(ClubChatKbEntry entry, int score) {
    }

    public record KbMatch(
            Integer entryId,
            Integer clubId,
            String questionTitle,
            String answerText,
            int score
    ) {
    }
}
