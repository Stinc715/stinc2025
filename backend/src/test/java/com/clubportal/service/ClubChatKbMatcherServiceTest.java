package com.clubportal.service;

import com.clubportal.config.ClubChatKbMatcherProperties;
import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ClubChatKbMatcherServiceTest {

    private final EmbeddingVectorCodec embeddingVectorCodec = new EmbeddingVectorCodec();
    private final ClubChatKbEmbeddingTextNormalizer normalizer = new ClubChatKbEmbeddingTextNormalizer();

    @Test
    void obviousMatchReturnsHit() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(2))
                .thenReturn(List.of(
                        entry(7, 2, "Can I bring my own racket?", "Yes. You may bring your own racket.", List.of(1.0, 0.0, 0.0), 3),
                        entry(8, 2, "Do you have parking?", "Parking is free after 6pm.", List.of(0.0, 1.0, 0.0), 3)
                ));
        when(embeddingService.generateQuestionEmbedding("can i bring my own racket?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.99, 0.01, 0.0), "text-embedding-3-small", 3));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(2, "Can I bring my own racket?");

        assertTrue(result.hit());
        assertEquals(Integer.valueOf(7), result.matchedEntryId());
        assertEquals("Can I bring my own racket?", result.matchedQuestion());
        assertEquals("Yes. You may bring your own racket.", result.matchedReply());
        assertEquals("CLUB_FAQ", result.source());
        assertTrue(result.bestScore() >= 0.84d);
    }

    @Test
    void similarExpressionCanHitSameFaq() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(2))
                .thenReturn(List.of(entry(
                        7,
                        2,
                        "When are you open?",
                        "We are open from 8am to 10pm on weekdays.",
                        List.of(1.0, 0.0),
                        2
                )));
        when(embeddingService.generateQuestionEmbedding("what are your opening hours?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.96, 0.04), "text-embedding-3-small", 2));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(2, "What are your opening hours?");

        assertTrue(result.hit());
        assertEquals(Integer.valueOf(7), result.matchedEntryId());
        assertEquals("We are open from 8am to 10pm on weekdays.", result.matchedReply());
        assertEquals("what are your opening hours?", result.normalizedQuestion());
        verify(embeddingService).generateQuestionEmbedding("what are your opening hours?");
    }

    @Test
    void lowSimilarityReturnsMiss() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(3))
                .thenReturn(List.of(entry(
                        11,
                        3,
                        "How do I contact you?",
                        "Email us at hello@example.com.",
                        List.of(0.0, 1.0, 0.0),
                        3
                )));
        when(embeddingService.generateQuestionEmbedding("can i bring my own racket?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0, 0.0), "text-embedding-3-small", 3));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(3, "Can I bring my own racket?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.LOW_SIMILARITY, result.rejectReason());
        assertNotNull(result.bestScore());
    }

    @Test
    void closeTopScoresReturnAmbiguousMatch() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(4))
                .thenReturn(List.of(
                        entry(21, 4, "How do I contact you?", "Email us.", List.of(0.95, 0.05, 0.0), 3),
                        entry(22, 4, "What is your phone number?", "Call reception.", List.of(0.92, 0.08, 0.0), 2)
                ));
        when(embeddingService.generateQuestionEmbedding("how can i contact the club?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0, 0.0), "text-embedding-3-small", 3));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(4, "How can I contact the club?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.AMBIGUOUS_MATCH, result.rejectReason());
        assertNotNull(result.bestScore());
        assertNotNull(result.secondBestScore());
        assertTrue(result.bestScore() - result.secondBestScore() < 0.05d);
    }

    @Test
    void noFaqReturnsNoFaqReason() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(9))
                .thenReturn(List.of());

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(9, "What are your opening hours?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.NO_FAQ, result.rejectReason());
        verifyNoInteractions(embeddingService);
    }

    @Test
    void missingEmbeddingsReturnNoValidEmbeddingsInsteadOfNoFaq() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry entry = new ClubChatKbEntry();
        entry.setId(30);
        entry.setClubId(6);
        entry.setQuestionTitle("How do I contact you?");
        entry.setAnswerText("Email us.");
        entry.setQuestionEmbedding(null);
        entry.setEmbeddingDim(null);
        entry.setEnabled(true);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(6))
                .thenReturn(List.of(entry));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0, 0.0), "text-embedding-3-small", 3));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(6, "How do I contact you?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.NO_VALID_EMBEDDINGS, result.rejectReason());
    }

    @Test
    void brokenEmbeddingJsonIsSkippedWithoutCrashing() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry badEntry = new ClubChatKbEntry();
        badEntry.setId(30);
        badEntry.setClubId(6);
        badEntry.setQuestionTitle("How do I contact you?");
        badEntry.setAnswerText("Email us.");
        badEntry.setQuestionEmbedding("[broken-json");
        badEntry.setEmbeddingDim(3);
        badEntry.setEnabled(true);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(6))
                .thenReturn(List.of(badEntry));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0, 0.0), "text-embedding-3-small", 3));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(6, "How do I contact you?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.NO_VALID_EMBEDDINGS, result.rejectReason());
    }

    @Test
    void dimensionMismatchIsSkippedWithoutCrashing() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(7))
                .thenReturn(List.of(entry(
                        31,
                        7,
                        "How do I contact you?",
                        "Email us.",
                        List.of(1.0, 0.0),
                        2
                )));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(1.0, 0.0, 0.0), "text-embedding-3-small", 3));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(7, "How do I contact you?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.NO_VALID_EMBEDDINGS, result.rejectReason());
    }

    @Test
    void blankUserQuestionReturnsInvalidUserQuestion() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(2, "   ");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.INVALID_USER_QUESTION, result.rejectReason());
        verifyNoInteractions(repository, embeddingService);
    }

    @Test
    void embeddingFailureReturnsEmbeddingFailed() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.findByClubIdAndEnabledTrueOrderByPriorityDescUpdatedAtDescIdDesc(8))
                .thenReturn(List.of(entry(
                        40,
                        8,
                        "How do I contact you?",
                        "Email us.",
                        List.of(1.0, 0.0, 0.0),
                        3
                )));
        when(embeddingService.generateQuestionEmbedding("how do i contact you?"))
                .thenThrow(new EmbeddingGenerationException("OPENAI_API_KEY is not configured"));

        ClubChatKbMatcherService service = service(repository, embeddingService, 0.84, 0.05);

        ClubChatKbMatchResult result = service.matchClubFaq(8, "How do I contact you?");

        assertFalse(result.hit());
        assertEquals(ClubChatKbMatchRejectReason.EMBEDDING_FAILED, result.rejectReason());
    }

    private ClubChatKbMatcherService service(ClubChatKbEntryRepository repository,
                                             EmbeddingService embeddingService,
                                             double bestScoreThreshold,
                                             double minScoreGap) {
        ClubChatKbMatcherProperties properties = new ClubChatKbMatcherProperties();
        properties.setBestScoreThreshold(bestScoreThreshold);
        properties.setMinScoreGap(minScoreGap);
        return new ClubChatKbMatcherService(
                repository,
                embeddingService,
                normalizer,
                embeddingVectorCodec,
                properties
        );
    }

    private ClubChatKbEntry entry(int id,
                                  int clubId,
                                  String question,
                                  String reply,
                                  List<Double> embedding,
                                  int priority) {
        ClubChatKbEntry entry = new ClubChatKbEntry();
        entry.setId(id);
        entry.setClubId(clubId);
        entry.setQuestionTitle(question);
        entry.setAnswerText(reply);
        entry.setQuestionEmbedding(embeddingVectorCodec.encode(embedding));
        entry.setEmbeddingModel("text-embedding-3-small");
        entry.setEmbeddingDim(embedding.size());
        entry.setPriority(priority);
        entry.setEnabled(true);
        return entry;
    }
}
