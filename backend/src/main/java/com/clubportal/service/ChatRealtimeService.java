package com.clubportal.service;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ChatRealtimeService {

    private static final long SSE_TIMEOUT_MILLIS = 0L;

    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final AtomicInteger workerCounter = new AtomicInteger(1);
    private final ExecutorService notificationExecutor = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "chat-realtime-" + workerCounter.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    });

    public SseEmitter subscribeUserThread(Integer clubId, Integer userId) {
        return subscribe(userKey(clubId, userId), "connected", payload("connected", clubId, userId, null));
    }

    public SseEmitter subscribeClubConversations(Integer clubId) {
        return subscribe(clubKey(clubId), "connected", payload("connected", clubId, null, null));
    }

    public void notifyThreadUpdated(Integer clubId, Integer userId, Integer messageId) {
        send(userKey(clubId, userId), "thread-updated", payload("thread-updated", clubId, userId, messageId));
    }

    public void notifyConversationUpdated(Integer clubId, Integer userId, Integer messageId) {
        send(clubKey(clubId), "conversation-updated", payload("conversation-updated", clubId, userId, messageId));
    }

    public void afterCommit(Runnable action) {
        Runnable asyncAction = () -> dispatchAsync(action);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            asyncAction.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                asyncAction.run();
            }
        });
    }

    @PreDestroy
    void shutdown() {
        notificationExecutor.shutdownNow();
    }

    private SseEmitter subscribe(String key, String eventName, Map<String, Object> data) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);
        emitters.computeIfAbsent(key, unused -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(key, emitter));
        emitter.onTimeout(() -> removeEmitter(key, emitter));
        emitter.onError(ex -> removeEmitter(key, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name(eventName)
                    .data(data));
        } catch (IOException ex) {
            removeEmitter(key, emitter);
            emitter.completeWithError(ex);
        }
        return emitter;
    }

    private void send(String key, String eventName, Map<String, Object> data) {
        CopyOnWriteArrayList<SseEmitter> listeners = emitters.get(key);
        if (listeners == null || listeners.isEmpty()) {
            return;
        }

        for (SseEmitter emitter : listeners) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException ex) {
                removeEmitter(key, emitter);
                emitter.completeWithError(ex);
            }
        }
    }

    private void dispatchAsync(Runnable action) {
        if (action == null) {
            return;
        }
        notificationExecutor.execute(() -> {
            try {
                action.run();
            } catch (RuntimeException ex) {
                // Swallow async SSE errors so chat responses are never blocked by a dead emitter.
            }
        });
    }

    private void removeEmitter(String key, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> listeners = emitters.get(key);
        if (listeners == null) {
            return;
        }
        listeners.remove(emitter);
        if (listeners.isEmpty()) {
            emitters.remove(key, listeners);
        }
    }

    private static Map<String, Object> payload(String type, Integer clubId, Integer userId, Integer messageId) {
        return Map.of(
                "type", type,
                "clubId", clubId,
                "userId", userId == null ? "" : userId,
                "messageId", messageId == null ? "" : messageId,
                "timestamp", Instant.now().toString()
        );
    }

    private static String userKey(Integer clubId, Integer userId) {
        return "user:" + clubId + ":" + userId;
    }

    private static String clubKey(Integer clubId) {
        return "club:" + clubId;
    }
}
