package com.clubportal.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class ClubChatKbEmbeddingTextNormalizer {

    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }

        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFKC)
                .replace('\u3000', ' ')
                .replace('\u00A0', ' ')
                .replace('，', ',')
                .replace('。', '.')
                .replace('！', '!')
                .replace('？', '?')
                .replace('：', ':')
                .replace('；', ';');

        normalized = normalized.trim().toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("\\s+", " ");
        normalized = normalized.replaceAll("\\s*([,.:;!?])\\s*", "$1 ");
        normalized = normalized.replaceAll("\\s+", " ").trim();
        normalized = normalized.replaceAll("([?.!])(\\s*\\1)+$", "$1");
        return normalized.trim();
    }
}
