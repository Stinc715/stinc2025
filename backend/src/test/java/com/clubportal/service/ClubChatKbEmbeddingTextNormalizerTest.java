package com.clubportal.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClubChatKbEmbeddingTextNormalizerTest {

    private final ClubChatKbEmbeddingTextNormalizer normalizer = new ClubChatKbEmbeddingTextNormalizer();

    @Test
    void normalizeAppliesLightweightEmbeddingCleanup() {
        String normalized = normalizer.normalize("  Can\u3000I  Bring，My Own Racket？  ");
        assertEquals("can i bring, my own racket?", normalized);
    }

    @Test
    void normalizeKeepsMeaningButRemovesRepeatedTrailingPunctuation() {
        String normalized = normalizer.normalize("What time do you open!!!!");
        assertEquals("what time do you open!", normalized);
    }
}
