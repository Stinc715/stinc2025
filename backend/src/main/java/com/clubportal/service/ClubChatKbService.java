package com.clubportal.service;

import com.clubportal.dto.ClubChatKbEntryResponse;
import com.clubportal.dto.ClubChatKbEntryUpsertRequest;
import com.clubportal.model.ClubChatKbEntry;
import com.clubportal.model.ClubChatKbLanguage;
import com.clubportal.repository.ClubChatKbEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ClubChatKbService {

    private static final Logger log = LoggerFactory.getLogger(ClubChatKbService.class);

    private final ClubChatKbEntryRepository clubChatKbEntryRepository;
    private final ClubChatKbSupport clubChatKbSupport;
    private final EmbeddingService embeddingService;
    private final ClubChatKbEmbeddingTextNormalizer embeddingTextNormalizer;
    private final EmbeddingVectorCodec embeddingVectorCodec;

    public ClubChatKbService(ClubChatKbEntryRepository clubChatKbEntryRepository,
                             ClubChatKbSupport clubChatKbSupport,
                             EmbeddingService embeddingService,
                             ClubChatKbEmbeddingTextNormalizer embeddingTextNormalizer,
                             EmbeddingVectorCodec embeddingVectorCodec) {
        this.clubChatKbEntryRepository = clubChatKbEntryRepository;
        this.clubChatKbSupport = clubChatKbSupport;
        this.embeddingService = embeddingService;
        this.embeddingTextNormalizer = embeddingTextNormalizer;
        this.embeddingVectorCodec = embeddingVectorCodec;
    }

    @Transactional(readOnly = true)
    public List<ClubChatKbEntryResponse> listEntries(Integer clubId) {
        return clubChatKbEntryRepository.findByClubIdOrderByPriorityDescUpdatedAtDescIdDesc(clubId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClubChatKbEntryResponse createEntry(Integer clubId, ClubChatKbEntryUpsertRequest request) {
        ClubChatKbEntry entry = new ClubChatKbEntry();
        applyRequest(entry, clubId, request);
        ClubChatKbEntry saved = clubChatKbEntryRepository.save(entry);
        return toResponse(saved);
    }

    @Transactional
    public ClubChatKbEntryResponse updateEntry(Integer clubId, Integer entryId, ClubChatKbEntryUpsertRequest request) {
        ClubChatKbEntry entry = clubChatKbEntryRepository.findByIdAndClubId(entryId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat KB entry not found"));
        applyRequest(entry, clubId, request);
        ClubChatKbEntry saved = clubChatKbEntryRepository.save(entry);
        return toResponse(saved);
    }

    @Transactional
    public void deleteEntry(Integer clubId, Integer entryId) {
        ClubChatKbEntry entry = clubChatKbEntryRepository.findByIdAndClubId(entryId, clubId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chat KB entry not found"));
        clubChatKbEntryRepository.delete(entry);
    }

    private void applyRequest(ClubChatKbEntry entry, Integer clubId, ClubChatKbEntryUpsertRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing chat KB data");
        }

        String questionTitle = clubChatKbSupport.safe(request.questionTitle());
        if (questionTitle.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question title is required");
        }

        String answerText = clubChatKbSupport.safe(request.answerText());
        if (answerText.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Standard reply is required");
        }

        String existingQuestion = clubChatKbSupport.safe(entry.getQuestionTitle());
        String normalizedQuestionForEmbedding = normalizeQuestionForEmbedding(questionTitle);
        if (normalizedQuestionForEmbedding.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question is empty after normalization");
        }
        boolean questionChanged = !normalizedQuestionForEmbedding.equals(normalizeQuestionForEmbedding(existingQuestion));

        List<String> triggerKeywords = resolveTriggerKeywords(entry, request, questionChanged);
        List<String> exampleQuestions = resolveExampleQuestions(entry, request, questionChanged, questionTitle);
        ClubChatKbLanguage language = request.language() == null || request.language().isBlank()
                ? (entry.getLanguage() == null ? ClubChatKbLanguage.ANY : entry.getLanguage())
                : clubChatKbSupport.normalizeLanguage(request.language());
        int priority = request.priority() == null
                ? (entry.getPriority() == null ? 0 : clubChatKbSupport.normalizePriority(entry.getPriority()))
                : clubChatKbSupport.normalizePriority(request.priority());
        boolean enabled = request.enabled() == null
                ? (entry.getEnabled() == null || entry.getEnabled())
                : request.enabled();

        entry.setClubId(clubId);
        boolean refreshEmbedding = shouldRefreshEmbedding(entry, questionChanged);

        if (refreshEmbedding) {
            refreshEmbedding(entry, normalizedQuestionForEmbedding);
        } else {
        }

        entry.setQuestionTitle(questionTitle);
        entry.setAnswerText(answerText);
        entry.setTriggerKeywords(clubChatKbSupport.encodeList(triggerKeywords));
        entry.setExampleQuestions(clubChatKbSupport.encodeList(exampleQuestions));
        entry.setLanguage(language);
        entry.setPriority(priority);
        entry.setEnabled(enabled);
    }

    private List<String> resolveTriggerKeywords(ClubChatKbEntry entry,
                                                ClubChatKbEntryUpsertRequest request,
                                                boolean questionChanged) {
        if (request.triggerKeywords() != null) {
            return clubChatKbSupport.normalizeKeywords(request.triggerKeywords());
        }
        if (questionChanged) {
            return List.of();
        }
        return clubChatKbSupport.decodeList(entry.getTriggerKeywords());
    }

    private List<String> resolveExampleQuestions(ClubChatKbEntry entry,
                                                 ClubChatKbEntryUpsertRequest request,
                                                 boolean questionChanged,
                                                 String questionTitle) {
        List<String> exampleQuestions;
        if (request.exampleQuestions() != null) {
            exampleQuestions = clubChatKbSupport.normalizeExampleQuestions(request.exampleQuestions());
        } else if (questionChanged) {
            exampleQuestions = List.of(questionTitle);
        } else {
            exampleQuestions = clubChatKbSupport.decodeList(entry.getExampleQuestions());
        }

        if (exampleQuestions.isEmpty()) {
            return List.of(questionTitle);
        }
        return exampleQuestions;
    }

    private boolean shouldRefreshEmbedding(ClubChatKbEntry entry, boolean questionChanged) {
        if (questionChanged || entry.getId() == null) {
            return true;
        }
        if (clubChatKbSupport.safe(entry.getQuestionEmbedding()).isBlank()) {
            return true;
        }
        if (clubChatKbSupport.safe(entry.getEmbeddingModel()).isBlank()) {
            return true;
        }
        return entry.getEmbeddingDim() == null || entry.getEmbeddingDim() <= 0;
    }

    private void refreshEmbedding(ClubChatKbEntry entry, String normalizedQuestionForEmbedding) {
        Integer clubId = entry == null ? null : entry.getClubId();
        Integer entryId = entry == null ? null : entry.getId();
        try {
            EmbeddingSnapshot snapshot = generateEmbeddingSnapshot(normalizedQuestionForEmbedding);
            applyEmbeddingSnapshot(entry, snapshot);
        } catch (EmbeddingGenerationException | IllegalArgumentException ex) {
            throw ClubChatKbOperationException.embeddingFailed(clubId, entryId, ex.getMessage(), ex);
        }
    }

    String normalizeQuestionForEmbedding(String questionTitle) {
        return embeddingTextNormalizer.normalize(clubChatKbSupport.safe(questionTitle));
    }

    EmbeddingSnapshot generateEmbeddingSnapshot(String normalizedQuestionForEmbedding) {
        String normalizedQuestion = clubChatKbSupport.safe(normalizedQuestionForEmbedding);
        if (normalizedQuestion.isBlank()) {
            throw new IllegalArgumentException("Question is empty after normalization");
        }

        EmbeddingService.EmbeddingResult result = embeddingService.generateQuestionEmbedding(normalizedQuestion);
        return new EmbeddingSnapshot(
                embeddingVectorCodec.encode(result.vector()),
                clubChatKbSupport.safe(result.model()),
                result.dimension()
        );
    }

    void applyEmbeddingSnapshot(ClubChatKbEntry entry, EmbeddingSnapshot snapshot) {
        if (entry == null || snapshot == null) {
            return;
        }
        entry.setQuestionEmbedding(snapshot.questionEmbedding());
        entry.setEmbeddingModel(snapshot.embeddingModel());
        entry.setEmbeddingDim(snapshot.embeddingDim());
    }

    private ClubChatKbEntryResponse toResponse(ClubChatKbEntry entry) {
        return new ClubChatKbEntryResponse(
                entry.getId(),
                entry.getClubId(),
                clubChatKbSupport.safe(entry.getQuestionTitle()),
                clubChatKbSupport.safe(entry.getAnswerText()),
                clubChatKbSupport.decodeList(entry.getTriggerKeywords()),
                clubChatKbSupport.decodeList(entry.getExampleQuestions()),
                (entry.getLanguage() == null ? ClubChatKbLanguage.ANY : entry.getLanguage()).name(),
                entry.getPriority() == null ? 0 : entry.getPriority(),
                Boolean.TRUE.equals(entry.getEnabled()),
                entry.getCreatedAt(),
                entry.getUpdatedAt()
        );
    }

    record EmbeddingSnapshot(
            String questionEmbedding,
            String embeddingModel,
            Integer embeddingDim
    ) {
    }
}
