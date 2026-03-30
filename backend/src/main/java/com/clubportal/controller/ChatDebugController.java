package com.clubportal.controller;

import com.clubportal.config.AppLlmProperties;
import com.clubportal.config.ChatDebugVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class ChatDebugController {

    private static final Logger log = LoggerFactory.getLogger(ChatDebugController.class);

    private final Environment environment;
    private final AppLlmProperties appLlmProperties;

    public ChatDebugController(Environment environment,
                               AppLlmProperties appLlmProperties) {
        this.environment = environment;
        this.appLlmProperties = appLlmProperties;
    }

    @GetMapping("/chat-version")
    public ResponseEntity<ChatVersionResponse> chatVersion() {
        log.info("[CLUB_CHAT_DEBUG] debug endpoint hit: path=/api/debug/chat-version, version={}, thread={}",
                ChatDebugVersion.VERSION_MARKER,
                Thread.currentThread().getName());
        return ResponseEntity.ok()
                .header(ChatDebugVersion.VERSION_HEADER, ChatDebugVersion.VERSION_MARKER)
                .body(new ChatVersionResponse(
                ChatDebugVersion.VERSION_MARKER,
                List.of(environment.getActiveProfiles()),
                appLlmProperties.isEnabled(),
                appLlmProperties.getModel(),
                LocalDateTime.now()
        ));
    }

    @GetMapping("/ping")
    public ResponseEntity<PingResponse> ping() {
        log.info("[CLUB_CHAT_DEBUG] debug endpoint hit: path=/api/debug/ping, version={}, thread={}",
                ChatDebugVersion.VERSION_MARKER,
                Thread.currentThread().getName());
        return ResponseEntity.ok()
                .header(ChatDebugVersion.VERSION_HEADER, ChatDebugVersion.VERSION_MARKER)
                .body(new PingResponse(true, "/api/debug/ping"));
    }

    public record ChatVersionResponse(
            String versionMarker,
            List<String> activeProfiles,
            boolean llmEnabled,
            String llmModel,
            LocalDateTime currentTime
    ) {
    }

    public record PingResponse(
            boolean ok,
            String path
    ) {
    }
}
