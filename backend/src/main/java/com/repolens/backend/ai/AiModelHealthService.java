package com.repolens.backend.ai;

import com.repolens.backend.ai.security.AiTimeoutExecutor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AiModelHealthService {
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final AiTimeoutExecutor timeoutExecutor;
    private final String chatModelName;
    private final String embeddingModelName;

    public AiModelHealthService(
            ChatModel chatModel,
            EmbeddingModel embeddingModel,
            AiTimeoutExecutor timeoutExecutor,
            @Value("${app.ai.chat-model:qwen2.5-coder:7b}") String chatModelName,
            @Value("${app.ai.embedding-model:nomic-embed-text}") String embeddingModelName
    ) {
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
        this.timeoutExecutor = timeoutExecutor;
        this.chatModelName = chatModelName;
        this.embeddingModelName = embeddingModelName;
    }

    public AiModelHealthResponse check() {
        boolean chatHealthy = false;
        boolean embeddingHealthy = false;
        String detail = "OK";
        try {
            String response = timeoutExecutor.execute(() -> chatModel.call("Reply with OK."));
            chatHealthy = response != null && !response.isBlank();
        } catch (RuntimeException ex) {
            detail = "Chat model failed: " + ex.getMessage();
        }
        try {
            float[] embedding = timeoutExecutor.execute(() -> embeddingModel.embed("health check"));
            embeddingHealthy = embedding != null && embedding.length > 0;
        } catch (RuntimeException ex) {
            detail = detail + " Embedding model failed: " + ex.getMessage();
        }
        return new AiModelHealthResponse(chatHealthy, embeddingHealthy, chatModelName, embeddingModelName, detail, Instant.now());
    }

    public record AiModelHealthResponse(
            boolean chatHealthy,
            boolean embeddingHealthy,
            String chatModel,
            String embeddingModel,
            String detail,
            Instant checkedAt
    ) {
    }
}