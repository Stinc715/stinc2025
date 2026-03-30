package com.clubportal.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AppLlmProperties.class)
public class OpenAiConfig {

    @Bean
    public OpenAiClient openAiClient(AppLlmProperties appLlmProperties) {
        return new OpenAiClient(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(8))
                        .build(),
                URI.create(appLlmProperties.getApiBaseUrl()),
                safe(appLlmProperties.getApiKey())
        );
    }

    public record OpenAiClient(
            HttpClient httpClient,
            URI baseUri,
            String apiKey
    ) {
        public boolean isConfigured() {
            return !safe(apiKey).isBlank();
        }

        public URI responsesUri() {
            return baseUri.resolve("/v1/responses");
        }

        public URI embeddingsUri() {
            return baseUri.resolve("/v1/embeddings");
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
