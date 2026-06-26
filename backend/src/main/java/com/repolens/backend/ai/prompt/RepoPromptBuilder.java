package com.repolens.backend.ai.prompt;

import com.repolens.backend.ai.rag.RepositorySemanticChunk;
import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.file.RepositoryFile;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RepoPromptBuilder {
    private static final int MAX_CONTEXT_FILES = 5;
    private static final int MAX_CONTENT_CHARS_PER_FILE = 1_600;
    private static final int MAX_RAG_CHARS_PER_CHUNK = 1_400;

    public String systemPrompt() {
        return """
                You are RepoPilot AI, a senior Java Spring Boot architect and code-review mentor.
                Answer using only the provided repository context, chat memory, tool results, and latest analysis signals.
                Be practical, interview-ready, and source-aware.
                If context is limited, say what should be verified in the code.
                Do not invent files, dependencies, endpoints, or security behavior.
                Use backend tools when the question asks about latest analysis, repository files, security findings, architecture score, or improvement plan.
                Format the answer with concise sections and relevant file evidence.
                """;
    }

    public String repositoryQuestionPrompt(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question) {
        return repositoryQuestionPrompt(repository, files, analysis, question, "No previous conversation for this repository.");
    }

    public String repositoryQuestionPrompt(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question, String memoryContext) {
        List<RepositoryFile> matches = topMatches(files, question);
        return basePrompt(repository, analysis, question, memoryContext, "Relevant repository files", fileContext(matches));
    }

    public String ragRepositoryQuestionPrompt(RepositoryProject repository, Analysis analysis, String question, List<RepositorySemanticChunk> chunks) {
        return ragRepositoryQuestionPrompt(repository, analysis, question, chunks, "No previous conversation for this repository.");
    }

    public String ragRepositoryQuestionPrompt(RepositoryProject repository, Analysis analysis, String question, List<RepositorySemanticChunk> chunks, String memoryContext) {
        if (chunks == null || chunks.isEmpty()) {
            return basePrompt(repository, analysis, question, memoryContext, "Semantic repository context", "No semantic chunks were available. Use repository-level analysis only.");
        }
        return basePrompt(repository, analysis, question, memoryContext, "Semantic repository context", semanticChunkContext(chunks));
    }

    public String structuredRepositoryQuestionPrompt(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question) {
        return structuredRepositoryQuestionPrompt(repository, files, analysis, question, "No previous conversation for this repository.");
    }

    public String structuredRepositoryQuestionPrompt(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question, String memoryContext) {
        return withStructuredResponseRequirements(repositoryQuestionPrompt(repository, files, analysis, question, memoryContext));
    }

    public String structuredRagRepositoryQuestionPrompt(RepositoryProject repository, Analysis analysis, String question, List<RepositorySemanticChunk> chunks) {
        return structuredRagRepositoryQuestionPrompt(repository, analysis, question, chunks, "No previous conversation for this repository.");
    }

    public String structuredRagRepositoryQuestionPrompt(RepositoryProject repository, Analysis analysis, String question, List<RepositorySemanticChunk> chunks, String memoryContext) {
        return withStructuredResponseRequirements(ragRepositoryQuestionPrompt(repository, analysis, question, chunks, memoryContext));
    }

    private String basePrompt(RepositoryProject repository, Analysis analysis, String question, String memoryContext, String contextHeading, String context) {
        return """
                Repository: %s/%s
                GitHub URL: %s
                Default branch: %s
                User question: %s

                Conversation memory:
                %s

                Latest analysis:
                %s

                %s:
                %s

                Available backend tools:
                - latestAnalysis: use for latest saved scores/status.
                - repositoryFiles: use for imported file inventory.
                - securityFindings: use for security issues and security report.
                - architectureScore: use for architecture score and explanation.
                - improvementPlan: use for recommendations and next steps.

                Answer requirements:
                - Start with a direct answer.
                - Reference specific files when useful.
                - Include architecture/security/readiness context if relevant.
                - Use conversation memory for follow-up questions.
                - End with a practical next step.
                """.formatted(
                repository.getOwnerName(),
                repository.getRepositoryName(),
                repository.getGithubUrl(),
                repository.getDefaultBranch(),
                sanitize(question),
                sanitize(memoryContext),
                analysisContext(analysis),
                contextHeading,
                context
        );
    }

    private String withStructuredResponseRequirements(String prompt) {
        return prompt + """

                Structured response requirements:
                Return only valid JSON. Do not wrap the JSON in markdown fences.
                The JSON must match this shape exactly:
                {
                  "directAnswer": "Clear answer in 2 to 4 sentences.",
                  "keyPoints": ["Important explanation point"],
                  "evidence": [
                    {
                      "filePath": "path/from/provided/context.java",
                      "reason": "Why this file matters for the answer.",
                      "excerpt": "Small source-backed clue or summary from the provided snippet."
                    }
                  ],
                  "recommendedNextSteps": ["Practical next action"],
                  "confidence": "high | medium | low",
                  "limitation": "What should be manually verified, or empty string if none."
                }
                Use evidence only from the provided repository context or backend tool results. If no file evidence is available, return an empty evidence array.
                """;
    }

    private String analysisContext(Analysis analysis) {
        if (analysis == null) {
            return "No saved analysis is available yet.";
        }

        return """
                Overall score: %s/100
                Project readiness: %s/100
                Architecture summary: %s
                Security summary: %s
                Top recommendations: %s
                """.formatted(
                Math.round(analysis.getOverallScore()),
                Math.round(analysis.getProjectReadinessScore()),
                firstLines(analysis.getArchitectureExplanation(), 5),
                firstLines(analysis.getSecurityReport(), 4),
                firstLines(analysis.getRecommendations(), 5)
        );
    }

    private String fileContext(List<RepositoryFile> files) {
        if (files.isEmpty()) {
            return "No matching files were found. Use repository-level analysis only.";
        }

        return files.stream()
                .map(file -> """
                        File: %s
                        Language: %s
                        Snippet:
                        %s
                        """.formatted(file.getPath(), file.getLanguage(), trimContent(file.getContent(), MAX_CONTENT_CHARS_PER_FILE)))
                .collect(Collectors.joining("\n---\n"));
    }

    private String semanticChunkContext(List<RepositorySemanticChunk> chunks) {
        return chunks.stream()
                .map(chunk -> """
                        File: %s
                        Language: %s
                        Chunk: %s
                        Similarity: %.4f
                        Snippet:
                        %s
                        """.formatted(
                        chunk.filePath(),
                        chunk.language(),
                        chunk.chunkIndex(),
                        chunk.similarity(),
                        trimContent(chunk.content(), MAX_RAG_CHARS_PER_CHUNK)))
                .collect(Collectors.joining("\n---\n"));
    }

    private List<RepositoryFile> topMatches(List<RepositoryFile> files, String question) {
        Set<String> terms = Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(term -> term.length() > 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return files.stream()
                .sorted(Comparator.comparingInt((RepositoryFile file) -> score(file, terms)).reversed()
                        .thenComparing(RepositoryFile::getPath))
                .filter(file -> score(file, terms) > 0)
                .limit(MAX_CONTEXT_FILES)
                .toList();
    }

    private int score(RepositoryFile file, Set<String> terms) {
        String haystack = (file.getPath() + "\n" + file.getLanguage() + "\n" + file.getContent()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            if (file.getPath().toLowerCase(Locale.ROOT).contains(term)) score += 6;
            if (haystack.contains(term)) score += 2;
        }
        String path = file.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("controller")) score += 2;
        if (path.contains("service")) score += 2;
        if (path.contains("security")) score += 2;
        return score;
    }

    private String trimContent(String content, int maxChars) {
        if (content == null || content.isBlank()) {
            return "No content stored for this file.";
        }
        String sanitized = sanitize(content);
        return sanitized.length() <= maxChars
                ? sanitized
                : sanitized.substring(0, maxChars) + "\n... [truncated for AI context]";
    }

    private String sanitize(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("(?i)(password|secret|token|api[_-]?key)\\s*[:=]\\s*[^\\s\\n]+", "$1=[REDACTED]")
                .trim();
    }

    private String firstLines(String text, int maxLines) {
        if (text == null || text.isBlank()) {
            return "Not available.";
        }
        return Arrays.stream(text.split("\\R"))
                .filter(line -> !line.isBlank())
                .limit(maxLines)
                .collect(Collectors.joining("\n"));
    }
}