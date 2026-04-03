package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth.login-throttle")
public class AuthLoginThrottleProperties {

    private int maxFailures = 5;
    private int windowSeconds = 900;
    private int blockSeconds = 900;

    public int getMaxFailures() {
        return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
        this.maxFailures = maxFailures;
    }

    public int getWindowSeconds() {
        return windowSeconds;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    public int getBlockSeconds() {
        return blockSeconds;
    }

    public void setBlockSeconds(int blockSeconds) {
        this.blockSeconds = blockSeconds;
    }
}
