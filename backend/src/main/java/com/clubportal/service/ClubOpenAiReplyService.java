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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClubOpenAiReplyService {

    private static final Logger log = LoggerFactory.getLogger(ClubOpenAiReplyService.class);
    private static final String REWRITE_INSTRUCTIONS =
            "You are rewriting a club assistant reply. Only use the facts provided. Do not add any new facts or policies. Keep the reply concise and practical.";

    private final OpenAiConfig.OpenAiClient openAiClient;
    private final AppLlmProperties appLlmProperties;
    private final ObjectMapper objectMapper;

    public ClubOpenAiReplyService(OpenAiConfig.OpenAiClient openAiClient,
                                  AppLlmProperties appLlmProperties) {
        this.openAiClient = openAiClient;
        this.appLlmProperties = appLlmProperties;
        this.objectMapper = new ObjectMapper();
    }

    public String rewriteReply(ChatIntentType intentType, String answerSkeleton, String userMessage) {
        String skeleton = safe(answerSkeleton);
        if (skeleton.isBlank() || intentType == null || !intentType.isLowRisk()) {
            return skeleton;
        }
        if (!appLlmProperties.isEnabled() || !openAiClient.isConfigured()) {
            return skeleton;
        }

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", appLlmProperties.getModel());
            payload.put("instructions", REWRITE_INSTRUCTIONS);
            payload.put("input", List.of(Map.of(
                    "role", "user",
                    "content", """
                            Original user message: %s
                            Facts to express: %s
                            Write the final club reply only.
                            """.formatted(safe(userMessage), skeleton).trim()
            )));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(openAiClient.responsesUri())
                    .timeout(Duration.ofSeconds(12))
                    .header("Authorization", "Bearer " + openAiClient.apiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = openAiClient.httpClient().send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                log.warn("OpenAI rewrite request failed with status {}", response.statusCode());
                return skeleton;
            }

            String rewritten = extractOutputText(objectMapper.readTree(response.body()));
            return rewritten.isBlank() ? skeleton : rewritten;
        } catch (Exception ex) {
            log.warn("OpenAI rewrite fallback: {} - {}", ex.getClass().getSimpleName(), ex.getMessage());
            return skeleton;
        }
    }

    private String extractOutputText(JsonNode root) {
        String topLevel = text(root, "output_text");
        if (!topLevel.isBlank()) {
            return topLevel;
        }

        StringBuilder out = new StringBuilder();
        JsonNode output = root == null ? null : root.path("output");
        if (output == null || !output.isArray()) {
            return "";
        }

        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) continue;
            for (JsonNode part : content) {
                if (!"output_text".equalsIgnoreCase(text(part, "type"))) continue;
                String value = text(part, "text");
                if (value.isBlank()) continue;
                if (out.length() > 0) {
                    out.append(' ');
                }
                out.append(value);
            }
        }
        return out.toString().trim();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null ? "" : value.asText("").trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
