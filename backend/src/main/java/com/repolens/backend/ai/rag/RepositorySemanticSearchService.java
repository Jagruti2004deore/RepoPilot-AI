package com.repolens.backend.ai.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositorySemanticSearchService {
    private static final Logger log = LoggerFactory.getLogger(RepositorySemanticSearchService.class);

    private final EmbeddingModel embeddingModel;
    private final RepositoryEmbeddingStore embeddingStore;
    private final boolean aiEnabled;
    private final boolean ragEnabled;
    private final int defaultLimit;

    public RepositorySemanticSearchService(
            EmbeddingModel embeddingModel,
            RepositoryEmbeddingStore embeddingStore,
            @Value("${app.ai.enabled:true}") boolean aiEnabled,
            @Value("${app.ai.rag.enabled:true}") boolean ragEnabled,
            @Value("${app.ai.rag.search-limit:5}") int defaultLimit
    ) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.aiEnabled = aiEnabled;
        this.ragEnabled = ragEnabled;
        this.defaultLimit = Math.max(1, defaultLimit);
    }

    public List<RepositorySemanticChunk> findRelevantChunks(Long repositoryId, String question) {
        if (!aiEnabled || !ragEnabled || repositoryId == null || question == null || question.isBlank()) {
            return List.of();
        }

        try {
            if (embeddingStore.countByRepositoryId(repositoryId) == 0) {
                return List.of();
            }
            float[] queryEmbedding = embeddingModel.embed(question.trim());
            return embeddingStore.findRelevantChunks(repositoryId, queryEmbedding, defaultLimit);
        } catch (RuntimeException ex) {
            log.debug("Semantic search failed for repository {}; continuing without RAG context.", repositoryId, ex);
            return List.of();
        }
    }
}