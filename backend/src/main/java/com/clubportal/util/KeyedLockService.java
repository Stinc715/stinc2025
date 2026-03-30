package com.clubportal.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class KeyedLockService {

    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    public <T> T withLock(String key, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        String normalizedKey = normalizeKey(key);
        ReentrantLock lock = locks.computeIfAbsent(normalizedKey, ignored -> new ReentrantLock());
        lock.lock();
        try {
            return action.get();
        } finally {
            unlockAndCleanup(normalizedKey, lock);
        }
    }

    public <T> T withLocks(Collection<String> keys, Supplier<T> action) {
        Objects.requireNonNull(action, "action");
        List<String> normalizedKeys = normalizeKeys(keys);
        List<ReentrantLock> acquired = new ArrayList<>(normalizedKeys.size());
        for (String key : normalizedKeys) {
            ReentrantLock lock = locks.computeIfAbsent(key, ignored -> new ReentrantLock());
            lock.lock();
            acquired.add(lock);
        }
        try {
            return action.get();
        } finally {
            for (int i = normalizedKeys.size() - 1; i >= 0; i--) {
                unlockAndCleanup(normalizedKeys.get(i), acquired.get(i));
            }
        }
    }

    private static String normalizeKey(String key) {
        return (key == null || key.isBlank()) ? "__blank__" : key.trim();
    }

    private static List<String> normalizeKeys(Collection<String> keys) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        if (keys != null) {
            for (String key : keys) {
                unique.add(normalizeKey(key));
            }
        }
        List<String> out = new ArrayList<>(unique);
        out.sort(Comparator.naturalOrder());
        return out;
    }

    private void unlockAndCleanup(String key, ReentrantLock lock) {
        try {
            lock.unlock();
        } finally {
            if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        }
    }
}
