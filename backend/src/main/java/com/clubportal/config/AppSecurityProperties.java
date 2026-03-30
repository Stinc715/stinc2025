package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private final Cors cors = new Cors();

    public Cors getCors() {
        return cors;
    }

    public static class Cors {
        private List<String> allowedOriginPatterns = new ArrayList<>();

        public List<String> getAllowedOriginPatterns() {
            return allowedOriginPatterns;
        }

        public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
            this.allowedOriginPatterns = allowedOriginPatterns == null ? new ArrayList<>() : new ArrayList<>(allowedOriginPatterns);
        }
    }
}
