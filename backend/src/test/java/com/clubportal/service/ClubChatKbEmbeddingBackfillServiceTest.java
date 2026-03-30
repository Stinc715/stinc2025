package com.clubportal.service;

import com.clubportal.config.ClubChatKbMatcherProperties;
import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.model.ClubChatKbLanguage;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClubChatKbEmbeddingBackfillServiceTest {

    private final ClubChatKbSupport support = new ClubChatKbSupport();
    private final ClubChatKbEmbeddingTextNormalizer normalizer = new ClubChatKbEmbeddingTextNormalizer();
    private final EmbeddingVectorCodec embeddingVectorCodec = new EmbeddingVectorCodec();

    @Test
    void backfillClubRebuildsMissingEmbeddings() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry entry = entry(7, 2, "When are you open?", "We are open weekdays.", null, null, null);

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(entry));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("when are you open?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.4, 0.5, 0.6), "text-embedding-3-small", 3));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillClub(2, false, false);

        assertEquals(1, summary.totalFound());
        assertEquals(1, summary.eligibleCount());
        assertEquals(1, summary.rebuiltCount());
        assertEquals(0, summary.skippedCount());
        assertEquals(0, summary.failedCount());
        assertFalse(entry.getQuestionEmbedding() == null || entry.getQuestionEmbedding().isBlank());
        assertEquals("text-embedding-3-small", entry.getEmbeddingModel());
        assertEquals(3, entry.getEmbeddingDim());
    }

    @Test
    void backfillAllProcessesMultipleClubs() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry first = entry(7, 2, "When are you open?", "Weekdays.", null, null, null);
        ClubChatKbEntry second = entry(8, 9, "How do I contact you?", "Email us.", null, null, null);

        when(repository.findAllByOrderByClubIdAscPriorityDescUpdatedAtDescIdDesc()).thenReturn(List.of(first, second));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("when are you open?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0), "text-embedding-3-small", 2));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.0, 1.0), "text-embedding-3-small", 2));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillAll(false, false);

        assertEquals("ALL", summary.scope());
        assertEquals(2, summary.totalFound());
        assertEquals(2, summary.eligibleCount());
        assertEquals(2, summary.rebuiltCount());
        assertEquals(0, summary.failedCount());
        verify(repository, times(2)).save(any(ClubChatKbEntry.class));
    }

    @Test
    void forceFalseOnlyRebuildsMissingOrInvalidEmbeddings() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry valid = entry(
                7,
                2,
                "When are you open?",
                "Weekdays.",
                embeddingVectorCodec.encode(List.of(0.1, 0.2, 0.3)),
                "text-embedding-3-small",
                3
        );
        ClubChatKbEntry invalid = entry(
                8,
                2,
                "How do I contact you?",
                "Email us.",
                "[broken-json",
                "text-embedding-3-small",
                3
        );

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(valid, invalid));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.9, 0.1, 0.0), "text-embedding-3-small", 3));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillClub(2, false, false);

        assertEquals(2, summary.totalFound());
        assertEquals(1, summary.eligibleCount());
        assertEquals(1, summary.rebuiltCount());
        assertEquals(1, summary.skippedCount());
        verify(repository, times(1)).save(any(ClubChatKbEntry.class));
    }

    @Test
    void forceTrueRebuildsEvenExistingEmbeddings() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry existing = entry(
                7,
                2,
                "When are you open?",
                "Weekdays.",
                embeddingVectorCodec.encode(List.of(0.1, 0.2, 0.3)),
                "text-embedding-3-small",
                3
        );

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(existing));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("when are you open?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.6, 0.6, 0.1), "text-embedding-3-large", 3));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillClub(2, true, false);

        assertEquals(1, summary.eligibleCount());
        assertEquals(1, summary.rebuiltCount());
        assertEquals("text-embedding-3-large", existing.getEmbeddingModel());
        assertEquals(List.of(0.6, 0.6, 0.1), embeddingVectorCodec.decode(existing.getQuestionEmbedding()));
    }

    @Test
    void blankQuestionIsSkipped() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry blank = entry(7, 2, "   ", "Weekdays.", null, null, null);

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(blank));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillClub(2, false, false);

        assertEquals(0, summary.eligibleCount());
        assertEquals(0, summary.rebuiltCount());
        assertEquals(1, summary.skippedCount());
        verifyNoInteractions(embeddingService);
        verify(repository, times(0)).save(any(ClubChatKbEntry.class));
    }

    @Test
    void singleEmbeddingFailureDoesNotAbortWholeBatch() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry first = entry(7, 2, "When are you open?", "Weekdays.", null, null, null);
        ClubChatKbEntry second = entry(8, 2, "How do I contact you?", "Email us.", null, null, null);

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(first, second));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("when are you open?"))
                .thenThrow(new EmbeddingGenerationException("OPENAI_API_KEY is not configured"));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.0, 1.0, 0.0), "text-embedding-3-small", 3));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillClub(2, false, false);

        assertEquals(2, summary.eligibleCount());
        assertEquals(1, summary.rebuiltCount());
        assertEquals(1, summary.failedCount());
        assertEquals(List.of(7), summary.failureEntryIds());
        assertFalse(second.getQuestionEmbedding() == null || second.getQuestionEmbedding().isBlank());
    }

    @Test
    void backfilledFaqCanBeMatchedByExistingMatcher() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry entry = entry(7, 2, "When are you open?", "Weekdays.", null, null, null);

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(entry));
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(2))
                .thenReturn(List.of(entry));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("when are you open?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0, 0.0), "text-embedding-3-small", 3));
        when(embeddingService.generateQuestionEmbedding("what are your opening hours?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.99, 0.01, 0.0), "text-embedding-3-small", 3));

        ClubChatKbEmbeddingBackfillService backfillService = service(repository, embeddingService);
        ClubChatKbEmbeddingBackfillSummary summary = backfillService.backfillClub(2, false, false);
        assertEquals(1, summary.rebuiltCount());

        ClubChatKbMatcherProperties matcherProperties = new ClubChatKbMatcherProperties();
        matcherProperties.setBestScoreThreshold(0.84d);
        matcherProperties.setMinScoreGap(0.05d);
        ClubChatKbMatcherService matcherService = new ClubChatKbMatcherService(
                repository,
                embeddingService,
                normalizer,
                embeddingVectorCodec,
                matcherProperties
        );

        ClubChatKbMatchResult result = matcherService.matchClubFaq(2, "What are your opening hours?");

        assertTrue(result.hit());
        assertEquals(Integer.valueOf(7), result.matchedEntryId());
    }

    @Test
    void dryRunCountsEligibleWithoutSaving() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry entry = entry(7, 2, "When are you open?", "Weekdays.", null, null, null);

        when(repository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(2)).thenReturn(List.of(entry));

        ClubChatKbEmbeddingBackfillService service = service(repository, embeddingService);

        ClubChatKbEmbeddingBackfillSummary summary = service.backfillClub(2, false, true);

        assertTrue(summary.dryRun());
        assertEquals(1, summary.eligibleCount());
        assertEquals(0, summary.rebuiltCount());
        verifyNoInteractions(embeddingService);
        verify(repository, times(0)).save(any(ClubChatKbEntry.class));
    }

    private ClubChatKbEmbeddingBackfillService service(ClubChatKbEntryRepository repository,
                                                       EmbeddingService embeddingService) {
        ClubChatKbService kbService = new ClubChatKbService(
                repository,
                support,
                embeddingService,
                normalizer,
                embeddingVectorCodec
        );
        return new ClubChatKbEmbeddingBackfillService(repository, kbService, embeddingVectorCodec);
    }

    private ClubChatKbEntry entry(int id,
                                  int clubId,
                                  String question,
                                  String reply,
                                  String questionEmbedding,
                                  String embeddingModel,
                                  Integer embeddingDim) {
        ClubChatKbEntry entry = new ClubChatKbEntry();
        entry.setId(id);
        entry.setClubId(clubId);
        entry.setQuestionTitle(question);
        entry.setAnswerText(reply);
        entry.setQuestionEmbedding(questionEmbedding);
        entry.setEmbeddingModel(embeddingModel);
        entry.setEmbeddingDim(embeddingDim);
        entry.setLanguage(ClubChatKbLanguage.ANY);
        entry.setPriority(0);
        entry.setEnabled(true);
        entry.setTriggerKeywords(support.encodeList(List.of()));
        entry.setExampleQuestions(support.encodeList(List.of(question)));
        return entry;
    }
}
