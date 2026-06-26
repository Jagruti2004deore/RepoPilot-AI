package com.repolens.backend.ai.rag;

public record RepositoryChunk(
        Long repositoryId,
        Long fileId,
        String filePath,
        String language,
        int chunkIndex,
        String content
) {
    public String embeddingText() {
        return "File: " + filePath + "\nLanguage: " + safe(language) + "\n\n" + content;
    }

    private String safe(String value) {
        return value == null ? "unknown" : value;
    }
}