package com.clubportal.service;

import com.clubportal.config.AppLlmProperties;
import com.clubportal.config.OpenAiConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAiEmbeddingService implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingService.class);

    private final OpenAiConfig.OpenAiClient openAiClient;
    private final AppLlmProperties appLlmProperties;
    private final ObjectMapper objectMapper;

    public OpenAiEmbeddingService(OpenAiConfig.OpenAiClient openAiClient,
                                  AppLlmProperties appLlmProperties) {
        this.openAiClient = openAiClient;
        this.appLlmProperties = appLlmProperties;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public EmbeddingResult generateQuestionEmbedding(String normalizedQuestion) {
        String input = safe(normalizedQuestion);
        if (input.isBlank()) {
            throw new EmbeddingGenerationException("Question is empty after normalization");
        }
        if (!appLlmProperties.isEnabled()) {
            throw new EmbeddingGenerationException("Embedding generation is disabled");
        }
        if (!openAiClient.isConfigured()) {
            throw new EmbeddingGenerationException("OPENAI_API_KEY is not configured");
        }

        try {
            log.info("[CLUB_CHAT_DEBUG] KB_EMBED_PROVIDER start: model={}, inputLength={}",
                    appLlmProperties.getEmbeddingModel(),
                    input.length());
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", appLlmProperties.getEmbeddingModel());
            payload.put("input", input);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(openAiClient.embeddingsUri())
                    .timeout(Duration.ofSeconds(12))
                    .header("Authorization", "Bearer " + openAiClient.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = openAiClient.httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                log.warn("OpenAI embedding request failed with status {}", response.statusCode());
                throw new EmbeddingGenerationException("Embedding request failed with status " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode embeddingNode = root.path("data").path(0).path("embedding");
            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new EmbeddingGenerationException("Embedding response did not contain a usable vector");
            }

            List<Double> vector = new ArrayList<>();
            for (JsonNode value : embeddingNode) {
                if (!value.isNumber()) {
                    continue;
                }
                vector.add(value.asDouble());
            }
            if (vector.isEmpty()) {
                throw new EmbeddingGenerationException("Embedding response did not contain numeric vector values");
            }

            String model = text(root, "model");
            if (model.isBlank()) {
                model = appLlmProperties.getEmbeddingModel();
            }
            log.info("[CLUB_CHAT_DEBUG] KB_EMBED_PROVIDER success: model={}, dimension={}", model, vector.size());
            return new EmbeddingResult(vector, model, vector.size());
        } catch (EmbeddingGenerationException ex) {
            log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_PROVIDER failed: exceptionType={}, message={}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.warn("[CLUB_CHAT_DEBUG] KB_EMBED_PROVIDER failed: exceptionType={}, message={}",
                    ex.getClass().getSimpleName(),
                    ex.getMessage());
            throw new EmbeddingGenerationException("Failed to generate question embedding", ex);
        }
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null ? "" : value.asText("").trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
