package com.clubportal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmbeddingVectorCodec {

    private static final TypeReference<List<Double>> DOUBLE_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public EmbeddingVectorCodec() {
        this.objectMapper = new ObjectMapper();
    }

    public String encode(List<Double> vector) {
        List<Double> normalized = vector == null ? List.of() : vector.stream()
                .filter(value -> value != null && Double.isFinite(value))
                .toList();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Embedding vector is empty");
        }
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize embedding vector", ex);
        }
    }

    public List<Double> decode(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isBlank()) {
            return List.of();
        }
        try {
            List<Double> parsed = objectMapper.readValue(value, DOUBLE_LIST);
            return parsed == null ? List.of() : parsed.stream()
                    .filter(item -> item != null && Double.isFinite(item))
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse embedding vector", ex);
        }
    }
}
