package com.repolens.backend.ai;

import com.repolens.backend.ai.dto.RepoAiChatResponse;
import com.repolens.backend.ai.mcp.McpContextService;
import com.repolens.backend.ai.prompt.RepoPromptBuilder;
import com.repolens.backend.ai.rag.RepositorySemanticChunk;
import com.repolens.backend.ai.rag.RepositorySemanticSearchService;
import com.repolens.backend.ai.security.AiAuditService;
import com.repolens.backend.ai.security.AiRateLimiter;
import com.repolens.backend.ai.security.AiSecretRedactor;
import com.repolens.backend.ai.security.AiTimeoutExecutor;
import com.repolens.backend.ai.security.PromptInjectionGuard;
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
import reactor.core.publisher.Flux;

import java.time.Duration;
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
    private final PromptInjectionGuard injectionGuard;
    private final AiSecretRedactor redactor;
    private final AiRateLimiter rateLimiter;
    private final AiAuditService auditService;
    private final AiTimeoutExecutor timeoutExecutor;
    private final McpContextService mcpContextService;
    private final boolean enabled;
    private final String chatModel;

    public RepoAiChatService(
            ChatClient.Builder chatClientBuilder,
            RepoPromptBuilder promptBuilder,
            RepoAiChatResponseFormatter responseFormatter,
            RepositorySemanticSearchService semanticSearchService,
            RepositoryAnalysisTools repositoryAnalysisTools,
            PromptInjectionGuard injectionGuard,
            AiSecretRedactor redactor,
            AiRateLimiter rateLimiter,
            AiAuditService auditService,
            AiTimeoutExecutor timeoutExecutor,
            McpContextService mcpContextService,
            @Value("${app.ai.enabled:true}") boolean enabled,
            @Value("${app.ai.chat-model:qwen2.5-coder:7b}") String chatModel
    ) {
        this.chatClient = chatClientBuilder.build();
        this.promptBuilder = promptBuilder;
        this.responseFormatter = responseFormatter;
        this.semanticSearchService = semanticSearchService;
        this.repositoryAnalysisTools = repositoryAnalysisTools;
        this.injectionGuard = injectionGuard;
        this.redactor = redactor;
        this.rateLimiter = rateLimiter;
        this.auditService = auditService;
        this.timeoutExecutor = timeoutExecutor;
        this.mcpContextService = mcpContextService;
        this.enabled = enabled;
        this.chatModel = chatModel;
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
        if (!injectionGuard.isAllowed(question)) {
            auditService.record(userId, repository.getId(), "CHAT_GUARD", "BLOCKED", chatModel, question);
            return Optional.of(injectionGuard.rejectionMessage());
        }
        if (!rateLimiter.tryAcquire("chat:" + userId)) {
            auditService.record(userId, repository.getId(), "CHAT_RATE_LIMIT", "BLOCKED", chatModel, question);
            return Optional.empty();
        }

        try {
            String safeQuestion = redactor.redact(question);
            String userPrompt = buildPrompt(repository, files, analysis, safeQuestion, memoryContext, true);
            RepoAiChatResponse response = timeoutExecutor.execute(() -> chatClient.prompt()
                    .system(promptBuilder.systemPrompt())
                    .user(userPrompt)
                    .tools(repositoryAnalysisTools)
                    .toolContext(toolContext(repository.getId(), userId))
                    .call()
                    .entity(RepoAiChatResponse.class));

            Optional<String> answer = responseFormatter.format(response);
            auditService.record(userId, repository.getId(), "CHAT_COMPLETION", answer.isPresent() ? "SUCCESS" : "EMPTY", chatModel, safeQuestion);
            return answer;
        } catch (RuntimeException ex) {
            auditService.record(userId, repository.getId(), "CHAT_COMPLETION", "FAILED", chatModel, ex.getMessage());
            log.debug("RepoPilot AI secure RAG chat failed; using rule-based fallback.", ex);
            return Optional.empty();
        }
    }

    public Flux<String> streamQuestion(
            RepositoryProject repository,
            Long userId,
            List<RepositoryFile> files,
            Analysis analysis,
            String question,
            String memoryContext
    ) {
        if (!enabled) {
            return Flux.just("AI streaming is disabled.");
        }
        if (!injectionGuard.isAllowed(question)) {
            auditService.record(userId, repository.getId(), "CHAT_STREAM_GUARD", "BLOCKED", chatModel, question);
            return Flux.just(injectionGuard.rejectionMessage());
        }
        if (!rateLimiter.tryAcquire("stream:" + userId)) {
            auditService.record(userId, repository.getId(), "CHAT_STREAM_RATE_LIMIT", "BLOCKED", chatModel, question);
            return Flux.just("AI rate limit reached. Please wait a minute and try again.");
        }

        String safeQuestion = redactor.redact(question);
        String userPrompt = buildPrompt(repository, files, analysis, safeQuestion, memoryContext, false);
        return chatClient.prompt()
                .system(promptBuilder.systemPrompt())
                .user(userPrompt)
                .tools(repositoryAnalysisTools)
                .toolContext(toolContext(repository.getId(), userId))
                .stream()
                .content()
                .timeout(timeoutExecutor.timeout())
                .doOnComplete(() -> auditService.record(userId, repository.getId(), "CHAT_STREAM", "SUCCESS", chatModel, safeQuestion))
                .onErrorResume(ex -> {
                    auditService.record(userId, repository.getId(), "CHAT_STREAM", "FAILED", chatModel, ex.getMessage());
                    return Flux.just("AI streaming failed. Please retry or use the normal chat endpoint.");
                });
    }

    private String buildPrompt(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question, String memoryContext, boolean structured) {
        List<RepositorySemanticChunk> semanticChunks = semanticSearchService.findRelevantChunks(repository.getId(), question);
        String mcpContext = mcpContextService.promptContext();
        if (structured) {
            return semanticChunks.isEmpty()
                    ? promptBuilder.structuredRepositoryQuestionPrompt(repository, files, analysis, question, memoryContext, mcpContext)
                    : promptBuilder.structuredRagRepositoryQuestionPrompt(repository, analysis, question, semanticChunks, memoryContext, mcpContext);
        }
        return semanticChunks.isEmpty()
                ? promptBuilder.repositoryQuestionPrompt(repository, files, analysis, question, memoryContext)
                : promptBuilder.ragRepositoryQuestionPrompt(repository, analysis, question, semanticChunks, memoryContext);
    }

    private Map<String, Object> toolContext(Long repositoryId, Long userId) {
        return Map.of(
                RepositoryToolContextKeys.REPOSITORY_ID, repositoryId,
                RepositoryToolContextKeys.USER_ID, userId
        );
    }
}