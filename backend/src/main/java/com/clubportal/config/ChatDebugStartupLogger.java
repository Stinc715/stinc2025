package com.clubportal.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ChatDebugStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(ChatDebugStartupLogger.class);

    @EventListener(ApplicationReadyEvent.class)
    public void logVersionMarker() {
        log.info("[CLUB_CHAT_DEBUG] app version marker={}", ChatDebugVersion.VERSION_MARKER);
    }
}
