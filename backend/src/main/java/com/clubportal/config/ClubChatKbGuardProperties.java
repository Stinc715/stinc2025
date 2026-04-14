package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "app.chat.kb.guard")
public class ClubChatKbGuardProperties {

    private List<String> highRiskKeywords = new ArrayList<>(List.of(
            "refund",
            "complaint",
            "dispute",
            "legal",
            "lawyer",
            "human agent",
            "special case"
    ));

    private List<String> realtimeKeywords = new ArrayList<>(List.of(
            "today",
            "now",
            "currently",
            "available now",
            "remaining",
            "left",
            "available today"
    ));

    public List<String> getHighRiskKeywords() {
        return highRiskKeywords;
    }

    public void setHighRiskKeywords(List<String> highRiskKeywords) {
        this.highRiskKeywords = highRiskKeywords == null ? new ArrayList<>() : new ArrayList<>(highRiskKeywords);
    }

    public List<String> getRealtimeKeywords() {
        return realtimeKeywords;
    }

    public void setRealtimeKeywords(List<String> realtimeKeywords) {
        this.realtimeKeywords = realtimeKeywords == null ? new ArrayList<>() : new ArrayList<>(realtimeKeywords);
    }
}
