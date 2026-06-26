package com.repolens.backend.ai.rag;

public record RepositorySemanticChunk(
        Long repositoryId,
        Long fileId,
        String filePath,
        String language,
        int chunkIndex,
        String content,
        double similarity
) {
}