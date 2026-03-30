package com.clubportal.service;

import java.util.List;

public interface EmbeddingService {

    EmbeddingResult generateQuestionEmbedding(String normalizedQuestion);

    record EmbeddingResult(
            List<Double> vector,
            String model,
            int dimension
    ) {
    }
}
