package com.clubportal.service;

import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ClubChatKbEmbeddingBackfillService {

    private static final Logger log = LoggerFactory.getLogger(ClubChatKbEmbeddingBackfillService.class);

    private final ClubChatKbEntryRepository clubChatKbEntryRepository;
    private final ClubChatKbService clubChatKbService;
    private final EmbeddingVectorCodec embeddingVectorCodec;

    public ClubChatKbEmbeddingBackfillService(ClubChatKbEntryRepository clubChatKbEntryRepository,
                                              ClubChatKbService clubChatKbService,
                                              EmbeddingVectorCodec embeddingVectorCodec) {
        this.clubChatKbEntryRepository = clubChatKbEntryRepository;
        this.clubChatKbService = clubChatKbService;
        this.embeddingVectorCodec = embeddingVectorCodec;
    }

    public ClubChatKbEmbeddingBackfillSummary backfillClub(Integer clubId, boolean forceRebuild) {
        return backfillClub(clubId, forceRebuild, false);
    }

    public ClubChatKbEmbeddingBackfillSummary backfillClub(Integer clubId, boolean forceRebuild, boolean dryRun) {
        List<ClubChatKbEntry> entries = clubId == null
                ? List.of()
                : clubChatKbEntryRepository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(clubId);
        return processEntries("CLUB", clubId, entries, forceRebuild, dryRun);
    }

    public ClubChatKbEmbeddingBackfillSummary backfillAll(boolean forceRebuild) {
        return backfillAll(forceRebuild, false);
    }

    public ClubChatKbEmbeddingBackfillSummary backfillAll(boolean forceRebuild, boolean dryRun) {
        List<ClubChatKbEntry> entries = clubChatKbEntryRepository.findAllByOrderByClubIdAscPriorityDescUpdatedAtDescIdDesc();
        return processEntries("ALL", null, entries, forceRebuild, dryRun);
    }

    private ClubChatKbEmbeddingBackfillSummary processEntries(String scope,
                                                              Integer clubId,
                                                              List<ClubChatKbEntry> entries,
                                                              boolean forceRebuild,
                                                              boolean dryRun) {
        List<Integer> failureEntryIds = new ArrayList<>();
        int eligibleCount = 0;
        int rebuiltCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        log.info("[CLUB_CHAT_DEBUG] KB_EMBED_BACKFILL start: scope={}, clubId={}, totalFound={}, forceRebuild={}, dryRun={}",
                scope,
                clubId,
                entries == null ? 0 : entries.size(),
                forceRebuild,
                dryRun);

        for (ClubChatKbEntry entry : entries == null ? List.<ClubChatKbEntry>of() : entries) {
            Eligibility eligibility = assessEligibility(entry, forceRebuild);
            if (!eligibility.eligible()) {
                skippedCount++;
                log.info("[CLUB_CHAT_DEBUG] KB_EMBED_BACKFILL skip: scope={}, clubId={}, entryId={}, reason={}",
                        scope,
                        clubIdForLog(entry, clubId),
                        entryId(entry),
                        eligibility.reason());
                continue;
            }

            eligibleCount++;
            if (dryRun) {
                log.info("[CLUB_CHAT_DEBUG] KB_EMBED_BACKFILL dry-run eligible: scope={}, clubId={}, entryId={}, reason={}",
                        scope,
                        clubIdForLog(entry, clubId),
                        entryId(entry),
                        eligibility.reason());
                continue;
            }

            try {
                String normalizedQuestion = clubChatKbService.normalizeQuestionForEmbedding(entry.getQuestionTitle());
                ClubChatKbService.EmbeddingSnapshot snapshot = clubChatKbService.generateEmbeddingSnapshot(normalizedQuestion);
                clubChatKbService.applyEmbeddingSnapshot(entry, snapshot);
                clubChatKbEntryRepository.save(entry);
                rebuiltCount++;
                log.info("[CLUB_CHAT_DEBUG] KB_EMBED_BACKFILL rebuilt: scope={}, clubId={}, entryId={}, embeddingModel={}, embeddingDim={}",
                        scope,
                        clubIdForLog(entry, clubId),
                        entryId(entry),
                        safe(entry.getEmbeddingModel()),
                        entry.getEmbeddingDim());
            } catch (Exception ex) {
                failedCount++;
                failureEntryIds.add(entryId(entry));
                log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_BACKFILL failed: scope={}, clubId={}, entryId={}, reason={}",
                        scope,
                        clubIdForLog(entry, clubId),
                        entryId(entry),
                        ex.getMessage());
            }
        }

        ClubChatKbEmbeddingBackfillSummary summary = new ClubChatKbEmbeddingBackfillSummary(
                scope,
                clubId,
                forceRebuild,
                dryRun,
                entries == null ? 0 : entries.size(),
                eligibleCount,
                rebuiltCount,
                skippedCount,
                failedCount,
                List.copyOf(failureEntryIds)
        );

        log.info("[CLUB_CHAT_DEBUG] KB_EMBED_BACKFILL result: scope={}, clubId={}, totalFound={}, eligibleCount={}, rebuiltCount={}, skippedCount={}, failedCount={}, dryRun={}, forceRebuild={}, failureEntryIds={}",
                summary.scope(),
                summary.clubId(),
                summary.totalFound(),
                summary.eligibleCount(),
                summary.rebuiltCount(),
                summary.skippedCount(),
                summary.failedCount(),
                summary.dryRun(),
                summary.forceRebuild(),
                summary.failureEntryIds());
        return summary;
    }

    private Eligibility assessEligibility(ClubChatKbEntry entry, boolean forceRebuild) {
        if (entry == null) {
            return Eligibility.skip("missing_entry");
        }

        String normalizedQuestion = clubChatKbService.normalizeQuestionForEmbedding(entry.getQuestionTitle());
        if (normalizedQuestion.isBlank()) {
            return Eligibility.skip("blank_question");
        }

        if (forceRebuild) {
            return Eligibility.rebuild("force_rebuild");
        }

        String rawEmbedding = safe(entry.getQuestionEmbedding());
        if (rawEmbedding.isBlank()) {
            return Eligibility.rebuild("missing_embedding");
        }
        if (safe(entry.getEmbeddingModel()).isBlank()) {
            return Eligibility.rebuild("missing_embedding_model");
        }
        if (entry.getEmbeddingDim() == null || entry.getEmbeddingDim() <= 0) {
            return Eligibility.rebuild("missing_embedding_dim");
        }

        List<Double> decodedVector;
        try {
            decodedVector = embeddingVectorCodec.decode(rawEmbedding);
        } catch (RuntimeException ex) {
            return Eligibility.rebuild("invalid_embedding_json");
        }

        if (decodedVector.isEmpty()) {
            return Eligibility.rebuild("empty_embedding_vector");
        }
        if (decodedVector.size() != entry.getEmbeddingDim()) {
            return Eligibility.rebuild("embedding_dim_mismatch");
        }

        return Eligibility.skip("valid_embedding_present");
    }

    private static Integer clubIdForLog(ClubChatKbEntry entry, Integer requestedClubId) {
        if (entry != null && entry.getClubId() != null) {
            return entry.getClubId();
        }
        return requestedClubId;
    }

    private static int entryId(ClubChatKbEntry entry) {
        return entry == null || entry.getId() == null ? -1 : entry.getId();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private record Eligibility(boolean eligible, String reason) {
        private static Eligibility rebuild(String reason) {
            return new Eligibility(true, reason);
        }

        private static Eligibility skip(String reason) {
            return new Eligibility(false, reason);
        }
    }
}
