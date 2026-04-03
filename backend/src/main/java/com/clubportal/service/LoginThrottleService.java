package com.clubportal.service;

import com.clubportal.config.AuthLoginThrottleProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginThrottleService {

    private static final String BLOCK_MESSAGE = "Too many login attempts. Please try again later.";

    private final AuthLoginThrottleProperties properties;
    private final Clock clock;
    private final ConcurrentHashMap<String, AttemptState> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginThrottleService(AuthLoginThrottleProperties properties) {
        this(properties, Clock.systemUTC());
    }

    public LoginThrottleService(AuthLoginThrottleProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public Optional<String> getBlockMessage(String email, String clientIp) {
        Instant now = Instant.now(clock);
        for (String key : keysFor(email, clientIp)) {
            AttemptState state = attempts.computeIfPresent(key, (ignored, current) ->
                    current.isExpired(now, window()) ? null : current
            );
            if (state != null && state.isBlocked(now)) {
                return Optional.of(BLOCK_MESSAGE);
            }
        }
        return Optional.empty();
    }

    public void recordFailure(String email, String clientIp) {
        Instant now = Instant.now(clock);
        for (String key : keysFor(email, clientIp)) {
            attempts.compute(key, (ignored, current) -> {
                AttemptState next = current == null ? new AttemptState() : current;
                next.registerFailure(now, maxFailures(), window(), blockDuration());
                return next;
            });
        }
    }

    public void recordSuccess(String email, String clientIp) {
        for (String key : keysFor(email, clientIp)) {
            attempts.remove(key);
        }
    }

    private Set<String> keysFor(String email, String clientIp) {
        Set<String> keys = new LinkedHashSet<>();
        String normalizedEmail = normalize(email);
        String normalizedIp = normalize(clientIp);
        if (normalizedEmail != null) {
            keys.add("email:" + normalizedEmail);
        }
        if (normalizedIp != null) {
            keys.add("ip:" + normalizedIp);
        }
        return keys;
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.isBlank() ? null : normalized;
    }

    private int maxFailures() {
        return Math.max(1, properties.getMaxFailures());
    }

    private Duration window() {
        return Duration.ofSeconds(Math.max(1, properties.getWindowSeconds()));
    }

    private Duration blockDuration() {
        return Duration.ofSeconds(Math.max(1, properties.getBlockSeconds()));
    }

    private static final class AttemptState {
        private int failures;
        private Instant firstFailureAt;
        private Instant blockedUntil;

        private void registerFailure(Instant now,
                                     int maxFailures,
                                     Duration window,
                                     Duration blockDuration) {
            if (firstFailureAt == null || now.isAfter(firstFailureAt.plus(window))) {
                failures = 0;
                blockedUntil = null;
                firstFailureAt = now;
            }
            if (blockedUntil != null && !now.isBefore(blockedUntil)) {
                failures = 0;
                blockedUntil = null;
                firstFailureAt = now;
            }
            failures += 1;
            if (failures >= maxFailures) {
                blockedUntil = now.plus(blockDuration);
            }
        }

        private boolean isBlocked(Instant now) {
            return blockedUntil != null && now.isBefore(blockedUntil);
        }

        private boolean isExpired(Instant now, Duration window) {
            if (blockedUntil != null && now.isBefore(blockedUntil)) {
                return false;
            }
            return firstFailureAt == null || now.isAfter(firstFailureAt.plus(window));
        }
    }
}
