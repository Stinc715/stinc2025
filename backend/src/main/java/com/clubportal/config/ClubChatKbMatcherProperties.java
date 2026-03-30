package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.chat.kb.matcher")
public class ClubChatKbMatcherProperties {

    private double bestScoreThreshold = 0.84d;
    private double minScoreGap = 0.05d;

    public double getBestScoreThreshold() {
        return bestScoreThreshold;
    }

    public void setBestScoreThreshold(double bestScoreThreshold) {
        this.bestScoreThreshold = bestScoreThreshold;
    }

    public double getMinScoreGap() {
        return minScoreGap;
    }

    public void setMinScoreGap(double minScoreGap) {
        this.minScoreGap = minScoreGap;
    }
}
