package com.repolens.backend.ai.dto;

public record RepoAiChatEvidence(
        String filePath,
        String reason,
        String excerpt
) {
}