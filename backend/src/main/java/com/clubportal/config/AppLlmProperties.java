package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.llm")
public class AppLlmProperties {

    private boolean enabled = true;
    private String model = "gpt-5-mini";
    private String embeddingModel = "text-embedding-3-small";
    private String apiBaseUrl = "https://api.openai.com";
    private String apiKey = "";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModel() {
        String value = model == null ? "" : model.trim();
        return value.isBlank() ? "gpt-5-mini" : value;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getEmbeddingModel() {
        String value = embeddingModel == null ? "" : embeddingModel.trim();
        return value.isBlank() ? "text-embedding-3-small" : value;
    }

    public void setEmbeddingModel(String embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String getApiBaseUrl() {
        String value = apiBaseUrl == null ? "" : apiBaseUrl.trim();
        return value.isBlank() ? "https://api.openai.com" : value;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getApiKey() {
        return apiKey == null ? "" : apiKey.trim();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
