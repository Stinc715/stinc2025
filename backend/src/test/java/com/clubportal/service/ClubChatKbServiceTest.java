package com.clubportal.service;

import com.clubportal.dto.ClubChatKbEntryResponse;
import com.clubportal.dto.ClubChatKbEntryUpsertRequest;
import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.model.ClubChatKbLanguage;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ClubChatKbServiceTest {

    private final ClubChatKbSupport support = new ClubChatKbSupport();
    private final ClubChatKbEmbeddingTextNormalizer normalizer = new ClubChatKbEmbeddingTextNormalizer();
    private final EmbeddingVectorCodec embeddingVectorCodec = new EmbeddingVectorCodec();

    @Test
    void createEntryGeneratesEmbeddingAndPersistsIt() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> {
            ClubChatKbEntry saved = invocation.getArgument(0);
            saved.setId(14);
            return saved;
        });
        when(embeddingService.generateQuestionEmbedding("can i bring my own racket?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.12, -0.34, 0.56), "text-embedding-3-small", 3));

        ClubChatKbService service = new ClubChatKbService(
                repository,
                support,
                embeddingService,
                normalizer,
                embeddingVectorCodec
        );

        ClubChatKbEntryResponse response = service.createEntry(5, new ClubChatKbEntryUpsertRequest(
                "Can I bring my own racket?",
                "Yes. You may bring your own racket.",
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals(14, response.id());
        verify(embeddingService).generateQuestionEmbedding("can i bring my own racket?");
        verify(repository).save(argThat(entry ->
                entry.getClubId().equals(5)
                        && "Can I bring my own racket?".equals(entry.getQuestionTitle())
                        && "Yes. You may bring your own racket.".equals(entry.getAnswerText())
                        && "text-embedding-3-small".equals(entry.getEmbeddingModel())
                        && Integer.valueOf(3).equals(entry.getEmbeddingDim())
                        && embeddingVectorCodec.decode(entry.getQuestionEmbedding()).equals(List.of(0.12, -0.34, 0.56))
                        && Boolean.TRUE.equals(entry.getEnabled())
                        && support.decodeList(entry.getExampleQuestions()).equals(List.of("Can I bring my own racket?"))
        ));
    }

    @Test
    void updatingReplyOnlyDoesNotRecalculateEmbedding() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry existing = existingEntry();
        existing.setQuestionTitle("Can I bring my own racket?");
        existing.setAnswerText("Old reply");
        existing.setQuestionEmbedding(embeddingVectorCodec.encode(List.of(0.1, 0.2, 0.3)));
        existing.setEmbeddingModel("text-embedding-3-small");
        existing.setEmbeddingDim(3);

        when(repository.findByIdAndClubId(8, 5)).thenReturn(Optional.of(existing));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClubChatKbService service = new ClubChatKbService(
                repository,
                support,
                embeddingService,
                normalizer,
                embeddingVectorCodec
        );

        ClubChatKbEntryResponse response = service.updateEntry(5, 8, new ClubChatKbEntryUpsertRequest(
                "Can I bring my own racket?",
                "New reply",
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals("New reply", response.answerText());
        verifyNoInteractions(embeddingService);
        verify(repository).save(argThat(entry ->
                "New reply".equals(entry.getAnswerText())
                        && "text-embedding-3-small".equals(entry.getEmbeddingModel())
                        && Integer.valueOf(3).equals(entry.getEmbeddingDim())
                        && embeddingVectorCodec.decode(entry.getQuestionEmbedding()).equals(List.of(0.1, 0.2, 0.3))
        ));
    }

    @Test
    void updatingQuestionUnchangedButEmbeddingMissingRecalculates() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry existing = existingEntry();
        existing.setQuestionTitle("Can I bring my own racket?");
        existing.setAnswerText("Old reply");
        existing.setQuestionEmbedding(null);
        existing.setEmbeddingModel(null);
        existing.setEmbeddingDim(null);

        when(repository.findByIdAndClubId(8, 5)).thenReturn(Optional.of(existing));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("can i bring my own racket?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.4, 0.5, 0.6), "text-embedding-3-small", 3));

        ClubChatKbService service = new ClubChatKbService(
                repository,
                support,
                embeddingService,
                normalizer,
                embeddingVectorCodec
        );

        ClubChatKbEntryResponse response = service.updateEntry(5, 8, new ClubChatKbEntryUpsertRequest(
                "Can I bring my own racket?",
                "New reply",
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals("New reply", response.answerText());
        verify(embeddingService).generateQuestionEmbedding("can i bring my own racket?");
        verify(repository).save(argThat(entry ->
                "text-embedding-3-small".equals(entry.getEmbeddingModel())
                        && Integer.valueOf(3).equals(entry.getEmbeddingDim())
                        && embeddingVectorCodec.decode(entry.getQuestionEmbedding()).equals(List.of(0.4, 0.5, 0.6))
        ));
    }

    @Test
    void updatingQuestionRecalculatesEmbedding() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        ClubChatKbEntry existing = existingEntry();
        existing.setQuestionTitle("Can I bring my own racket?");
        existing.setAnswerText("Old reply");
        existing.setQuestionEmbedding(embeddingVectorCodec.encode(List.of(0.1, 0.2, 0.3)));
        existing.setEmbeddingModel("text-embedding-3-small");
        existing.setEmbeddingDim(3);

        when(repository.findByIdAndClubId(8, 5)).thenReturn(Optional.of(existing));
        when(repository.save(any(ClubChatKbEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(embeddingService.generateQuestionEmbedding("do you rent rackets?"))
                .thenReturn(new EmbeddingService.EmbeddingResult(List.of(0.9, 0.8), "text-embedding-3-large", 2));

        ClubChatKbService service = new ClubChatKbService(
                repository,
                support,
                embeddingService,
                normalizer,
                embeddingVectorCodec
        );

        ClubChatKbEntryResponse response = service.updateEntry(5, 8, new ClubChatKbEntryUpsertRequest(
                "Do you rent rackets?",
                "Yes. Rackets can be rented at reception.",
                null,
                null,
                null,
                null,
                null
        ));

        assertEquals("Do you rent rackets?", response.questionTitle());
        verify(embeddingService).generateQuestionEmbedding("do you rent rackets?");
        verify(repository).save(argThat(entry ->
                "Do you rent rackets?".equals(entry.getQuestionTitle())
                        && "text-embedding-3-large".equals(entry.getEmbeddingModel())
                        && Integer.valueOf(2).equals(entry.getEmbeddingDim())
                        && embeddingVectorCodec.decode(entry.getQuestionEmbedding()).equals(List.of(0.9, 0.8))
                        && support.decodeList(entry.getExampleQuestions()).equals(List.of("Do you rent rackets?"))
        ));
    }

    @Test
    void embeddingFailureReturnsClearError() {
        ClubChatKbEntryRepository repository = mock(ClubChatKbEntryRepository.class);
        EmbeddingService embeddingService = mock(EmbeddingService.class);
        when(embeddingService.generateQuestionEmbedding("can i bring my own racket?"))
                .thenThrow(new EmbeddingGenerationException("OPENAI_API_KEY is not configured"));

        ClubChatKbService service = new ClubChatKbService(
                repository,
                support,
                embeddingService,
                normalizer,
                embeddingVectorCodec
        );

        ClubChatKbOperationException ex = assertThrows(ClubChatKbOperationException.class, () -> service.createEntry(5, new ClubChatKbEntryUpsertRequest(
                "Can I bring my own racket?",
                "Yes. You may bring your own racket.",
                null,
                null,
                null,
                null,
                null
        )));

        assertEquals(503, ex.getStatus().value());
        assertEquals("CHAT_KB_EMBEDDING_FAILED", ex.getCode());
        assertEquals("OPENAI_API_KEY is not configured", ex.getMessage());
        assertEquals(Integer.valueOf(5), ex.getClubId());
        assertNull(ex.getEntryId());
    }

    private ClubChatKbEntry existingEntry() {
        ClubChatKbEntry entry = new ClubChatKbEntry();
        entry.setId(8);
        entry.setClubId(5);
        entry.setLanguage(ClubChatKbLanguage.ANY);
        entry.setPriority(0);
        entry.setEnabled(true);
        entry.setTriggerKeywords(support.encodeList(List.of()));
        entry.setExampleQuestions(support.encodeList(List.of("Can I bring my own racket?")));
        return entry;
    }
}
