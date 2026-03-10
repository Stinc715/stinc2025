package com.clubportal.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ClubTagCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<List<String>> STRING_LIST = new TypeReference<>() {};

    private ClubTagCodec() {
    }

    public static List<String> normalize(List<String> rawTags) {
        if (rawTags == null || rawTags.isEmpty()) {
            return List.of();
        }
        Set<String> seen = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        for (String rawTag : rawTags) {
            String tag = safe(rawTag);
            if (tag.isBlank()) continue;
            String key = tag.toLowerCase();
            if (!seen.add(key)) continue;
            out.add(tag);
        }
        return out;
    }

    public static List<String> decode(String storedTags, String fallbackCategory) {
        List<String> parsed = normalize(parseStored(storedTags));
        if (!parsed.isEmpty()) {
            return parsed;
        }
        String fallback = safe(fallbackCategory);
        if (fallback.isBlank()) {
            return List.of();
        }
        return List.of(fallback);
    }

    public static String encode(List<String> rawTags, String fallbackCategory) {
        List<String> tags = normalize(rawTags);
        if (tags.isEmpty()) {
            String fallback = safe(fallbackCategory);
            if (!fallback.isBlank()) {
                tags = List.of(fallback);
            }
        }
        if (tags.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(tags);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String primary(List<String> rawTags, String fallbackCategory) {
        List<String> tags = normalize(rawTags);
        if (!tags.isEmpty()) {
            return tags.get(0);
        }
        return safe(fallbackCategory);
    }

    private static List<String> parseStored(String storedTags) {
        String text = safe(storedTags);
        if (text.isBlank()) {
            return List.of();
        }
        try {
            List<String> parsed = OBJECT_MAPPER.readValue(text, STRING_LIST);
            return parsed == null ? List.of() : parsed;
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
