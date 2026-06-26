package com.repolens.backend.ai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AiRateLimiter {
    private final Map<String, Deque<Instant>> calls = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int maxRequestsPerMinute;

    public AiRateLimiter(@Value("${app.ai.security.rate-limit-per-minute:12}") int maxRequestsPerMinute) {
        this.clock = Clock.systemUTC();
        this.maxRequestsPerMinute = Math.max(1, maxRequestsPerMinute);
    }

    public boolean tryAcquire(String key) {
        String safeKey = key == null || key.isBlank() ? "anonymous" : key;
        Instant now = Instant.now(clock);
        Instant cutoff = now.minusSeconds(60);
        Deque<Instant> timestamps = calls.computeIfAbsent(safeKey, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= maxRequestsPerMinute) {
                return false;
            }
            timestamps.addLast(now);
            return true;
        }
    }
}