package com.repolens.backend.ai.rag;

import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.file.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RepositoryEmbeddingIndexer {
    private static final Logger log = LoggerFactory.getLogger(RepositoryEmbeddingIndexer.class);

    private final EmbeddingModel embeddingModel;
    private final RepositoryChunker chunker;
    private final RepositoryEmbeddingStore embeddingStore;
    private final boolean aiEnabled;
    private final boolean ragEnabled;
    private final int batchSize;

    public RepositoryEmbeddingIndexer(
            EmbeddingModel embeddingModel,
            RepositoryChunker chunker,
            RepositoryEmbeddingStore embeddingStore,
            @Value("${app.ai.enabled:true}") boolean aiEnabled,
            @Value("${app.ai.rag.enabled:true}") boolean ragEnabled,
            @Value("${app.ai.rag.embedding-batch-size:16}") int batchSize
    ) {
        this.embeddingModel = embeddingModel;
        this.chunker = chunker;
        this.embeddingStore = embeddingStore;
        this.aiEnabled = aiEnabled;
        this.ragEnabled = ragEnabled;
        this.batchSize = Math.max(1, batchSize);
    }

    public void indexRepository(RepositoryProject repository, List<RepositoryFile> files) {
        if (!aiEnabled || !ragEnabled) {
            return;
        }
        if (repository.getId() == null || files.isEmpty()) {
            return;
        }

        try {
            List<RepositoryChunk> chunks = chunker.chunk(repository, files);
            if (chunks.isEmpty()) {
                embeddingStore.replaceRepositoryChunks(repository.getId(), List.of(), List.of());
                return;
            }

            List<float[]> embeddings = new ArrayList<>(chunks.size());
            for (int start = 0; start < chunks.size(); start += batchSize) {
                int end = Math.min(chunks.size(), start + batchSize);
                List<String> batch = chunks.subList(start, end).stream()
                        .map(RepositoryChunk::embeddingText)
                        .toList();
                embeddings.addAll(embeddingModel.embed(batch));
            }

            embeddingStore.replaceRepositoryChunks(repository.getId(), chunks, embeddings);
            log.info("Indexed {} semantic chunks for repository {}.", chunks.size(), repository.getId());
        } catch (RuntimeException ex) {
            log.warn("Repository embedding index failed for repository {}. Import will continue without RAG. Cause: {}",
                    repository.getId(), ex.getMessage());
        }
    }
}