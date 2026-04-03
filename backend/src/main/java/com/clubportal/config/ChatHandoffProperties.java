package com.clubportal.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.chat.handoff")
public class ChatHandoffProperties {

    private int idleResetMinutes = 60;

    public int getIdleResetMinutes() {
        return idleResetMinutes;
    }

    public void setIdleResetMinutes(int idleResetMinutes) {
        this.idleResetMinutes = Math.max(1, idleResetMinutes);
    }
}
