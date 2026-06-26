package com.repolens.backend.ai;

import com.repolens.backend.ai.dto.AiTestResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AiService {
    private final ChatClient chatClient;
    private final boolean enabled;
    private final String provider;
    private final String chatModel;
    private final String embeddingModel;

    public AiService(
            ChatClient.Builder chatClientBuilder,
            @Value("${app.ai.enabled:true}") boolean enabled,
            @Value("${app.ai.provider:ollama}") String provider,
            @Value("${app.ai.chat-model:qwen2.5-coder:7b}") String chatModel,
            @Value("${app.ai.embedding-model:nomic-embed-text}") String embeddingModel
    ) {
        this.chatClient = chatClientBuilder.build();
        this.enabled = enabled;
        this.provider = provider;
        this.chatModel = chatModel;
        this.embeddingModel = embeddingModel;
    }

    public AiTestResponse status() {
        return new AiTestResponse(enabled, provider, chatModel, embeddingModel, "Spring AI is configured. Use POST /api/ai/test to call the model.", Instant.now());
    }

    public AiTestResponse test(String prompt) {
        if (!enabled) {
            return new AiTestResponse(false, provider, chatModel, embeddingModel, "AI is disabled by configuration.", Instant.now());
        }

        try {
            String response = chatClient.prompt()
                    .system("You are RepoPilot AI, a concise Java Spring Boot mentor. Answer clearly in 3 to 5 lines.")
                    .user(prompt.trim())
                    .call()
                    .content();

            return new AiTestResponse(true, provider, chatModel, embeddingModel, response, Instant.now());
        } catch (RuntimeException ex) {
            return new AiTestResponse(
                    true,
                    provider,
                    chatModel,
                    embeddingModel,
                    "Spring AI is configured, but the model call failed. Check Ollama, model memory, or set AI_CHAT_MODEL to a smaller local model. Cause: " + ex.getClass().getSimpleName(),
                    Instant.now()
            );
        }
    }
}
