package com.repolens.backend.ai.rag;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RepositoryEmbeddingStore {
    private static final Logger log = LoggerFactory.getLogger(RepositoryEmbeddingStore.class);

    private final JdbcTemplate jdbcTemplate;
    private final int embeddingDimensions;

    public RepositoryEmbeddingStore(
            JdbcTemplate jdbcTemplate,
            @Value("${app.ai.embedding-dimensions:768}") int embeddingDimensions
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingDimensions = embeddingDimensions;
    }

    @PostConstruct
    void initializeSchema() {
        try {
            ensureSchema();
        } catch (RuntimeException ex) {
            log.warn("PGVector schema initialization skipped. Repository import will retry it later. Cause: {}", ex.getMessage());
        }
    }

    public void ensureSchema() {
        jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS repository_file_embeddings (
                    id BIGSERIAL PRIMARY KEY,
                    repository_id BIGINT NOT NULL REFERENCES repositories(id) ON DELETE CASCADE,
                    repository_file_id BIGINT REFERENCES repository_files(id) ON DELETE CASCADE,
                    file_path VARCHAR(700) NOT NULL,
                    language VARCHAR(100),
                    chunk_index INTEGER NOT NULL,
                    content TEXT NOT NULL,
                    embedding vector(%d) NOT NULL,
                    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                    CONSTRAINT uq_repository_file_chunk UNIQUE(repository_id, repository_file_id, chunk_index)
                )
                """.formatted(embeddingDimensions));
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_repository_file_embeddings_repository ON repository_file_embeddings(repository_id)");
        try {
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_repository_file_embeddings_vector ON repository_file_embeddings USING hnsw (embedding vector_cosine_ops)");
        } catch (RuntimeException ex) {
            log.warn("PGVector HNSW index was not created. Semantic search still works with exact scan. Cause: {}", ex.getMessage());
        }
    }

    public void replaceRepositoryChunks(Long repositoryId, List<RepositoryChunk> chunks, List<float[]> embeddings) {
        if (chunks.size() != embeddings.size()) {
            throw new IllegalArgumentException("Chunks and embeddings must have the same size.");
        }

        ensureSchema();
        deleteByRepositoryId(repositoryId);
        for (int i = 0; i < chunks.size(); i++) {
            save(chunks.get(i), embeddings.get(i));
        }
    }

    public List<RepositorySemanticChunk> findRelevantChunks(Long repositoryId, float[] queryEmbedding, int limit) {
        ensureSchema();
        String vector = toPgVector(queryEmbedding);
        return jdbcTemplate.query("""
                SELECT repository_id,
                       repository_file_id,
                       file_path,
                       language,
                       chunk_index,
                       content,
                       1 - (embedding <=> ?::vector) AS similarity
                FROM repository_file_embeddings
                WHERE repository_id = ?
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """, this::mapSemanticChunk, vector, repositoryId, vector, limit);
    }

    public long countByRepositoryId(Long repositoryId) {
        ensureSchema();
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM repository_file_embeddings WHERE repository_id = ?",
                Long.class,
                repositoryId
        );
        return count == null ? 0 : count;
    }

    private void deleteByRepositoryId(Long repositoryId) {
        jdbcTemplate.update("DELETE FROM repository_file_embeddings WHERE repository_id = ?", repositoryId);
    }

    private void save(RepositoryChunk chunk, float[] embedding) {
        jdbcTemplate.update("""
                INSERT INTO repository_file_embeddings (
                    repository_id,
                    repository_file_id,
                    file_path,
                    language,
                    chunk_index,
                    content,
                    embedding
                ) VALUES (?, ?, ?, ?, ?, ?, ?::vector)
                ON CONFLICT (repository_id, repository_file_id, chunk_index)
                DO UPDATE SET
                    file_path = EXCLUDED.file_path,
                    language = EXCLUDED.language,
                    content = EXCLUDED.content,
                    embedding = EXCLUDED.embedding,
                    created_at = NOW()
                """,
                chunk.repositoryId(),
                chunk.fileId(),
                chunk.filePath(),
                chunk.language(),
                chunk.chunkIndex(),
                chunk.content(),
                toPgVector(embedding)
        );
    }

    private RepositorySemanticChunk mapSemanticChunk(ResultSet rs, int rowNum) throws SQLException {
        return new RepositorySemanticChunk(
                rs.getLong("repository_id"),
                rs.getLong("repository_file_id"),
                rs.getString("file_path"),
                rs.getString("language"),
                rs.getInt("chunk_index"),
                rs.getString("content"),
                rs.getDouble("similarity")
        );
    }

    private String toPgVector(float[] values) {
        StringBuilder vector = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                vector.append(',');
            }
            vector.append(values[i]);
        }
        vector.append(']');
        return vector.toString();
    }
}