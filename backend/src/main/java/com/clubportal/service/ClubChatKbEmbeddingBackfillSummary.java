package com.clubportal.service;

import java.util.List;

public record ClubChatKbEmbeddingBackfillSummary(
        String scope,
        Integer clubId,
        boolean forceRebuild,
        boolean dryRun,
        int totalFound,
        int eligibleCount,
        int rebuiltCount,
        int skippedCount,
        int failedCount,
        List<Integer> failureEntryIds
) {
}
