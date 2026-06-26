package com.repolens.backend.ai.dto;

import java.util.List;

public record RepoAiChatResponse(
        String directAnswer,
        List<String> keyPoints,
        List<RepoAiChatEvidence> evidence,
        List<String> recommendedNextSteps,
        String confidence,
        String limitation
) {
}