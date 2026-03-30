package com.clubportal.service;

import com.clubportal.config.ClubChatKbMatcherProperties;
import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ClubChatKbMatcherService {

    private static final Logger log = LoggerFactory.getLogger(ClubChatKbMatcherService.class);

    private final ClubChatKbEntryRepository clubChatKbEntryRepository;
    private final EmbeddingService embeddingService;
    private final ClubChatKbEmbeddingTextNormalizer embeddingTextNormalizer;
    private final EmbeddingVectorCodec embeddingVectorCodec;
    private final ClubChatKbMatcherProperties matcherProperties;

    public ClubChatKbMatcherService(ClubChatKbEntryRepository clubChatKbEntryRepository,
                                    EmbeddingService embeddingService,
                                    ClubChatKbEmbeddingTextNormalizer embeddingTextNormalizer,
                                    EmbeddingVectorCodec embeddingVectorCodec,
                                    ClubChatKbMatcherProperties matcherProperties) {
        this.clubChatKbEntryRepository = clubChatKbEntryRepository;
        this.embeddingService = embeddingService;
        this.embeddingTextNormalizer = embeddingTextNormalizer;
        this.embeddingVectorCodec = embeddingVectorCodec;
        this.matcherProperties = matcherProperties;
    }

    public ClubChatKbMatchResult matchClubFaq(Integer clubId, String userQuestion) {
        String normalizedQuestion = embeddingTextNormalizer.normalize(userQuestion);
        if (clubId == null || normalizedQuestion.isBlank()) {
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.INVALID_USER_QUESTION,
                    null,
                    null,
                    normalizedQuestion
            );
            logResult(clubId, 0, 0, null, null, null, result.rejectReason());
            return result;
        }

        List<ClubChatKbEntry> entries = clubChatKbEntryRepository
                .findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(clubId);
        if (entries.isEmpty()) {
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.NO_FAQ,
                    null,
                    null,
                    normalizedQuestion
            );
            logResult(clubId, 0, 0, null, null, null, result.rejectReason());
            return result;
        }

        List<DecodedEntry> decodedEntries = new ArrayList<>();
        for (ClubChatKbEntry entry : entries) {
            List<Double> entryVector;
            try {
                entryVector = embeddingVectorCodec.decode(entry.getQuestionEmbedding());
            } catch (RuntimeException ex) {
                log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH invalid_embedding_json: clubId={}, entryId={}, reason={}",
                        clubId,
                        entry.getId(),
                        ex.getMessage());
                continue;
            }

            if (entryVector.isEmpty()) {
                log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH empty_embedding: clubId={}, entryId={}", clubId, entry.getId());
                continue;
            }
            if (entry.getEmbeddingDim() != null && entry.getEmbeddingDim() > 0 && entryVector.size() != entry.getEmbeddingDim()) {
                log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH invalid_embedding_dim: clubId={}, entryId={}, storedDim={}, actualDim={}",
                        clubId,
                        entry.getId(),
                        entry.getEmbeddingDim(),
                        entryVector.size());
                continue;
            }
            decodedEntries.add(new DecodedEntry(entry, entryVector));
        }

        if (decodedEntries.isEmpty()) {
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.NO_VALID_EMBEDDINGS,
                    null,
                    null,
                    normalizedQuestion
            );
            logResult(clubId, entries.size(), 0, null, null, null, result.rejectReason());
            return result;
        }

        final List<Double> userVector;
        try {
            userVector = embeddingService.generateQuestionEmbedding(normalizedQuestion).vector();
        } catch (EmbeddingGenerationException ex) {
            log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH embedding_failed: clubId={}, reason={}", clubId, ex.getMessage());
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.EMBEDDING_FAILED,
                    null,
                    null,
                    normalizedQuestion
            );
            logResult(clubId, entries.size(), 0, null, null, null, result.rejectReason());
            return result;
        }

        List<ScoredEntry> scoredEntries = new ArrayList<>();
        for (DecodedEntry decodedEntry : decodedEntries) {
            ClubChatKbEntry entry = decodedEntry.entry();
            List<Double> entryVector = decodedEntry.vector();
            if (entryVector.size() != userVector.size()) {
                log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH dimension_mismatch: clubId={}, entryId={}, userDim={}, faqDim={}",
                        clubId,
                        entry.getId(),
                        userVector.size(),
                        entryVector.size());
                continue;
            }

            double score = cosineSimilarity(userVector, entryVector);
            if (!Double.isFinite(score)) {
                log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH invalid_similarity: clubId={}, entryId={}, userDim={}, faqDim={}",
                        clubId,
                        entry.getId(),
                        userVector.size(),
                        entryVector.size());
                continue;
            }
            scoredEntries.add(new ScoredEntry(entry, score));
        }

        if (scoredEntries.isEmpty()) {
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.NO_VALID_EMBEDDINGS,
                    null,
                    null,
                    normalizedQuestion
            );
            logResult(clubId, entries.size(), 0, null, null, null, result.rejectReason());
            return result;
        }

        scoredEntries.sort(Comparator.comparingDouble(ScoredEntry::score).reversed()
                .thenComparing((ScoredEntry row) -> row.entry().getPriority() == null ? 0 : row.entry().getPriority(), Comparator.reverseOrder())
                .thenComparing(row -> row.entry().getId(), Comparator.nullsLast(Comparator.reverseOrder())));

        ScoredEntry top1 = scoredEntries.get(0);
        Double bestScore = top1.score();
        Double secondBestScore = scoredEntries.size() > 1 ? scoredEntries.get(1).score() : null;

        if (bestScore < matcherProperties.getBestScoreThreshold()) {
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.LOW_SIMILARITY,
                    bestScore,
                    secondBestScore,
                    normalizedQuestion
            );
            logResult(clubId, entries.size(), scoredEntries.size(), top1.entry().getId(), bestScore, secondBestScore, result.rejectReason());
            return result;
        }

        if (secondBestScore != null && (bestScore - secondBestScore) < matcherProperties.getMinScoreGap()) {
            ClubChatKbMatchResult result = ClubChatKbMatchResult.miss(
                    ClubChatKbMatchRejectReason.AMBIGUOUS_MATCH,
                    bestScore,
                    secondBestScore,
                    normalizedQuestion
            );
            logResult(clubId, entries.size(), scoredEntries.size(), top1.entry().getId(), bestScore, secondBestScore, result.rejectReason());
            return result;
        }

        ClubChatKbMatchResult result = ClubChatKbMatchResult.hit(
                top1.entry().getId(),
                safe(top1.entry().getQuestionTitle()),
                safe(top1.entry().getAnswerText()),
                bestScore,
                secondBestScore,
                normalizedQuestion
        );
        logResult(clubId, entries.size(), scoredEntries.size(), top1.entry().getId(), bestScore, secondBestScore, ClubChatKbMatchRejectReason.NONE);
        return result;
    }

    static double cosineSimilarity(List<Double> left, List<Double> right) {
        if (left == null || right == null || left.isEmpty() || right.isEmpty() || left.size() != right.size()) {
            return Double.NaN;
        }

        double dot = 0d;
        double leftNorm = 0d;
        double rightNorm = 0d;
        for (int i = 0; i < left.size(); i++) {
            double l = left.get(i);
            double r = right.get(i);
            dot += l * r;
            leftNorm += l * l;
            rightNorm += r * r;
        }
        if (leftNorm <= 0d || rightNorm <= 0d) {
            return Double.NaN;
        }
        return dot / (Math.sqrt(leftNorm) * Math.sqrt(rightNorm));
    }

    private void logResult(Integer clubId,
                           int faqCount,
                           int validEmbeddingCount,
                           Integer top1EntryId,
                           Double bestScore,
                           Double secondBestScore,
                           ClubChatKbMatchRejectReason rejectReason) {
        log.info("[CLUB_CHAT_DEBUG] KB_EMBED_MATCH result: clubId={}, faqCount={}, validEmbeddingCount={}, top1EntryId={}, bestScore={}, secondBestScore={}, rejectReason={}",
                clubId,
                faqCount,
                validEmbeddingCount,
                top1EntryId,
                scoreText(bestScore),
                scoreText(secondBestScore),
                rejectReason == null ? ClubChatKbMatchRejectReason.NONE : rejectReason);
    }

    private String scoreText(Double value) {
        return value == null ? "null" : String.format(java.util.Locale.ROOT, "%.4f", value);
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record ScoredEntry(ClubChatKbEntry entry, double score) {
    }

    private record DecodedEntry(ClubChatKbEntry entry, List<Double> vector) {
    }
}
