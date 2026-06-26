package com.repolens.backend.ai;

import com.repolens.backend.ai.prompt.RepoPromptBuilder;
import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.file.RepositoryFile;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RepoAiChatService {
    private final ChatClient chatClient;
    private final RepoPromptBuilder promptBuilder;
    private final boolean enabled;

    public RepoAiChatService(
            ChatClient.Builder chatClientBuilder,
            RepoPromptBuilder promptBuilder,
            @Value("${app.ai.enabled:true}") boolean enabled
    ) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.enabled = enabled;
    }

    public Optional<String> answerQuestion(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question) {
        if (!enabled) {
            return Optional.empty();
        }

        try {
            String response = chatClient.prompt()
                    .system(promptBuilder.systemPrompt())
                    .user(promptBuilder.repositoryQuestionPrompt(repository, files, analysis, question))
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(response.trim());
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }
}
