package com.repolens.backend.ai;

import com.repolens.backend.ai.dto.RepoAiChatResponse;
import com.repolens.backend.ai.prompt.RepoPromptBuilder;
import com.repolens.backend.ai.rag.RepositorySemanticChunk;
import com.repolens.backend.ai.rag.RepositorySemanticSearchService;
import com.repolens.backend.ai.tool.RepositoryAnalysisTools;
import com.repolens.backend.ai.tool.RepositoryToolContextKeys;
import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.file.RepositoryFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RepoAiChatService {
    private static final Logger log = LoggerFactory.getLogger(RepoAiChatService.class);

    private final ChatClient chatClient;
    private final RepoPromptBuilder promptBuilder;
    private final RepoAiChatResponseFormatter responseFormatter;
    private final RepositorySemanticSearchService semanticSearchService;
    private final RepositoryAnalysisTools repositoryAnalysisTools;
    private final boolean enabled;

    public RepoAiChatService(
            ChatClient.Builder chatClientBuilder,
            RepoPromptBuilder promptBuilder,
            RepoAiChatResponseFormatter responseFormatter,
            RepositorySemanticSearchService semanticSearchService,
            RepositoryAnalysisTools repositoryAnalysisTools,
            @Value("${app.ai.enabled:true}") boolean enabled
    ) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.responseFormatter = responseFormatter;
        this.semanticSearchService = semanticSearchService;
        this.repositoryAnalysisTools = repositoryAnalysisTools;
        this.enabled = enabled;
    }

    public Optional<String> answerQuestion(
            RepositoryProject repository,
            Long userId,
            List<RepositoryFile> files,
            Analysis analysis,
            String question,
            String memoryContext
    ) {
        if (!enabled) {
            return Optional.empty();
        }

        try {
            List<RepositorySemanticChunk> semanticChunks = semanticSearchService.findRelevantChunks(repository.getId(), question);
            String userPrompt = semanticChunks.isEmpty()
                    ? promptBuilder.structuredRepositoryQuestionPrompt(repository, files, analysis, question, memoryContext)
                    : promptBuilder.structuredRagRepositoryQuestionPrompt(repository, analysis, question, semanticChunks, memoryContext);

            RepoAiChatResponse response = chatClient.prompt()
                    .system(promptBuilder.systemPrompt())
                    .user(userPrompt)
                    .tools(repositoryAnalysisTools)
                    .toolContext(Map.of(
                            RepositoryToolContextKeys.REPOSITORY_ID, repository.getId(),
                            RepositoryToolContextKeys.USER_ID, userId
                    ))
                    .call()
                    .entity(RepoAiChatResponse.class);

            return responseFormatter.format(response);
        } catch (RuntimeException ex) {
            log.debug("RepoPilot AI memory/tool RAG chat failed; using rule-based fallback.", ex);
            return Optional.empty();
        }
    }
}