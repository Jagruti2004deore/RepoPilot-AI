package com.repolens.backend.ai.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
public class AiTimeoutExecutor {
    private final Duration timeout;

    public AiTimeoutExecutor(@Value("${app.ai.security.timeout-seconds:45}") long timeoutSeconds) {
        this.timeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
    }

    public <T> T execute(Supplier<T> supplier) {
        try {
            return CompletableFuture.supplyAsync(supplier)
                    .get(timeout.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("AI request timed out or failed.", ex);
        }
    }

    public Duration timeout() {
        return timeout;
    }
}