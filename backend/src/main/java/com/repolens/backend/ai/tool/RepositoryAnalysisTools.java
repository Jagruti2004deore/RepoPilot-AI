package com.repolens.backend.ai.tool;

import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.AnalysisRepository;
import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.RepositoryProjectRepository;
import com.repolens.backend.repository.file.RepositoryFile;
import com.repolens.backend.repository.file.RepositoryFileRepository;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RepositoryAnalysisTools {
    private static final int MAX_FILES = 40;
    private static final int MAX_LINES = 10;

    private final RepositoryProjectRepository repositoryProjectRepository;
    private final AnalysisRepository analysisRepository;
    private final RepositoryFileRepository repositoryFileRepository;

    public RepositoryAnalysisTools(
            RepositoryProjectRepository repositoryProjectRepository,
            AnalysisRepository analysisRepository,
            RepositoryFileRepository repositoryFileRepository
    ) {
        this.repositoryProjectRepository = repositoryProjectRepository;
        this.analysisRepository = analysisRepository;
        this.repositoryFileRepository = repositoryFileRepository;
    }

    @Tool(name = "latestAnalysis", description = "Get the latest saved analysis summary for the current repository.")
    public String latestAnalysis(ToolContext toolContext) {
        ScopedRepository scoped = scopedRepository(toolContext);
        Analysis analysis = latestAnalysis(scoped.repositoryId());
        return """
                Repository: %s/%s
                Status: %s
                Overall score: %s/100
                Project readiness: %s/100
                Architecture score: %s/100
                Security score: %s/100
                Maintainability score: %s/100
                Documentation score: %s/100
                Testing score: %s/100
                Completed at: %s
                """.formatted(
                scoped.project().getOwnerName(),
                scoped.project().getRepositoryName(),
                analysis.getStatus(),
                rounded(analysis.getOverallScore()),
                rounded(analysis.getProjectReadinessScore()),
                rounded(analysis.getArchitectureScore()),
                rounded(analysis.getSecurityScore()),
                rounded(analysis.getMaintainabilityScore()),
                rounded(analysis.getDocumentationScore()),
                rounded(analysis.getTestingScore()),
                analysis.getCompletedAt()
        );
    }

    @Tool(name = "repositoryFiles", description = "List important imported repository files for the current repository.")
    public String repositoryFiles(ToolContext toolContext) {
        ScopedRepository scoped = scopedRepository(toolContext);
        List<RepositoryFile> files = repositoryFileRepository.findByRepositoryIdOrderByPathAsc(scoped.repositoryId());
        if (files.isEmpty()) {
            return "No repository files are stored yet.";
        }

        String fileList = files.stream()
                .limit(MAX_FILES)
                .map(file -> "- " + file.getPath() + " | " + safe(file.getLanguage()) + " | " + file.getSizeBytes() + " bytes")
                .collect(Collectors.joining("\n"));
        return "Imported files: " + files.size() + "\n" + fileList;
    }

    @Tool(name = "securityFindings", description = "Get security findings and security report details for the current repository.")
    public String securityFindings(ToolContext toolContext) {
        ScopedRepository scoped = scopedRepository(toolContext);
        Analysis analysis = latestAnalysis(scoped.repositoryId());
        String findings = linesContaining(analysis.getFindingsText(), "security", MAX_LINES);
        if (findings.isBlank()) {
            findings = firstLines(analysis.getSecurityReport(), MAX_LINES);
        }
        return "Security score: " + rounded(analysis.getSecurityScore()) + "/100\n" + findings;
    }

    @Tool(name = "architectureScore", description = "Get architecture score and architecture explanation for the current repository.")
    public String architectureScore(ToolContext toolContext) {
        ScopedRepository scoped = scopedRepository(toolContext);
        Analysis analysis = latestAnalysis(scoped.repositoryId());
        return "Architecture score: " + rounded(analysis.getArchitectureScore()) + "/100\n"
                + firstLines(analysis.getArchitectureExplanation(), MAX_LINES);
    }

    @Tool(name = "improvementPlan", description = "Get a practical improvement plan from the latest repository recommendations.")
    public String improvementPlan(ToolContext toolContext) {
        ScopedRepository scoped = scopedRepository(toolContext);
        Analysis analysis = latestAnalysis(scoped.repositoryId());
        String recommendations = firstLines(analysis.getRecommendations(), MAX_LINES);
        if (recommendations.isBlank() || "Not available.".equals(recommendations)) {
            recommendations = firstLines(analysis.getReadinessChecklist(), MAX_LINES);
        }
        return "Improvement plan for " + scoped.project().getOwnerName() + "/" + scoped.project().getRepositoryName() + ":\n" + recommendations;
    }

    private ScopedRepository scopedRepository(ToolContext toolContext) {
        Map<String, Object> context = toolContext == null ? Map.of() : toolContext.getContext();
        Long repositoryId = asLong(context.get(RepositoryToolContextKeys.REPOSITORY_ID));
        Long userId = asLong(context.get(RepositoryToolContextKeys.USER_ID));
        if (repositoryId == null || userId == null) {
            throw new IllegalArgumentException("Repository tool context is missing repository or user scope.");
        }

        RepositoryProject project = repositoryProjectRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found."));
        if (project.getOwner() == null || !project.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("Repository does not belong to the current user.");
        }
        return new ScopedRepository(repositoryId, project);
    }

    private Analysis latestAnalysis(Long repositoryId) {
        return analysisRepository.findTopByRepositoryIdOrderByCreatedAtDesc(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found."));
    }

    private Long asLong(Object value) {
        if (value instanceof Long number) {
            return number;
        }
        if (value instanceof Integer number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    private String rounded(double value) {
        return String.valueOf(Math.round(value));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "unknown" : value;
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

    private String linesContaining(String text, String needle, int maxLines) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String lowerNeedle = needle.toLowerCase();
        return Arrays.stream(text.split("\\R"))
                .filter(line -> line.toLowerCase().contains(lowerNeedle))
                .limit(maxLines)
                .collect(Collectors.joining("\n"));
    }

    private record ScopedRepository(Long repositoryId, RepositoryProject project) {
    }
}