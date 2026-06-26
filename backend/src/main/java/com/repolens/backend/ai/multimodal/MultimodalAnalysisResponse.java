package com.repolens.backend.ai.multimodal;

import java.time.Instant;

public record MultimodalAnalysisResponse(
        boolean enabled,
        String model,
        String response,
        Instant createdAt
) {
}