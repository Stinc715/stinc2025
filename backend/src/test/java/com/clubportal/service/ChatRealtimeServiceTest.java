package com.clubportal.service;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatRealtimeServiceTest {

    @Test
    void afterCommitRunsAsynchronouslyWithoutTransaction() throws Exception {
        ChatRealtimeService service = new ChatRealtimeService();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> workerThread = new AtomicReference<>("");
        String callerThread = Thread.currentThread().getName();

        try {
            service.afterCommit(() -> {
                workerThread.set(Thread.currentThread().getName());
                latch.countDown();
            });

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertTrue(workerThread.get().startsWith("chat-realtime-"));
            assertNotEquals(callerThread, workerThread.get());
        } finally {
            service.shutdown();
        }
    }

    @Test
    void afterCommitWaitsForCommitAndThenRunsAsynchronously() throws Exception {
        ChatRealtimeService service = new ChatRealtimeService();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> workerThread = new AtomicReference<>("");

        try {
            TransactionSynchronizationManager.initSynchronization();

            service.afterCommit(() -> {
                workerThread.set(Thread.currentThread().getName());
                latch.countDown();
            });

            assertEquals(1, latch.getCount());

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            assertTrue(latch.await(2, TimeUnit.SECONDS));
            assertTrue(workerThread.get().startsWith("chat-realtime-"));
        } finally {
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.clearSynchronization();
            }
            service.shutdown();
        }
    }
}
