package com.clubportal.service;

import com.clubportal.model.ClubChatKbLanguage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ClubChatKbSupport {

    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public ClubChatKbSupport() {
        this.objectMapper = new ObjectMapper();
    }

    public List<String> normalizeKeywords(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            String[] pieces = rawValue.split("[\\r\\n,;锛岋紱]+");
            for (String piece : pieces) {
                String cleaned = safe(piece);
                if (!cleaned.isBlank()) {
                    normalized.add(cleaned);
                }
            }
        }
        return List.copyOf(normalized);
    }

    public List<String> normalizeExampleQuestions(List<String> rawValues) {
        if (rawValues == null || rawValues.isEmpty()) {
            return List.of();
        }

        Set<String> normalized = new LinkedHashSet<>();
        for (String rawValue : rawValues) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }
            String[] pieces = rawValue.split("[\\r\\n]+");
            for (String piece : pieces) {
                String cleaned = safe(piece);
                if (!cleaned.isBlank()) {
                    normalized.add(cleaned);
                }
            }
        }
        return List.copyOf(normalized);
    }

    public String encodeList(List<String> values) {
        List<String> normalized = values == null ? List.of() : values.stream()
                .map(this::safe)
                .filter(value -> !value.isBlank())
                .distinct()
                .toList();
        try {
            return objectMapper.writeValueAsString(normalized);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize chat KB values", ex);
        }
    }

    public List<String> decodeList(String raw) {
        String value = safe(raw);
        if (value.isBlank()) {
            return List.of();
        }

        try {
            List<String> parsed = objectMapper.readValue(value, STRING_LIST);
            return parsed == null ? List.of() : parsed.stream()
                    .map(this::safe)
                    .filter(item -> !item.isBlank())
                    .distinct()
                    .toList();
        } catch (Exception ignored) {
            List<String> fallback = new ArrayList<>();
            for (String piece : value.split("[\\r\\n,;锛岋紱]+")) {
                String cleaned = safe(piece);
                if (!cleaned.isBlank()) {
                    fallback.add(cleaned);
                }
            }
            return fallback.stream().distinct().toList();
        }
    }

    public ClubChatKbLanguage normalizeLanguage(String raw) {
        String normalized = safe(raw).toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "EN" -> ClubChatKbLanguage.EN;
            case "ZH" -> ClubChatKbLanguage.ZH;
            default -> ClubChatKbLanguage.ANY;
        };
    }

    public int normalizePriority(Integer raw) {
        if (raw == null) {
            return 0;
        }
        return Math.max(0, Math.min(999, raw));
    }

    public String safe(String raw) {
        if (raw == null) {
            return "";
        }
        String value = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
        return value.replaceAll("\\s+", " ");
    }
}
