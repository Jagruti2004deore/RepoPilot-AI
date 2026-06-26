package com.repolens.backend.ai.dto;

import java.time.Instant;

public record AiTestResponse(
        boolean enabled,
        String provider,
        String chatModel,
        String embeddingModel,
        String response,
        Instant createdAt
) {
}
