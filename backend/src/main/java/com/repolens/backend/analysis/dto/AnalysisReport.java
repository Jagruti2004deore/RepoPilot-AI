package com.repolens.backend.analysis.dto;

import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.RepositoryProject;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public record AnalysisReport(
        Long analysisId,
        Long repositoryId,
        String repositoryName,
        String owner,
        String githubUrl,
        String status,
        String defaultBranch,
        int importedFileCount,
        AnalysisScores scores,
        ReadinessScores readinessScores,
        String architectureReport,
        String codeQualityReport,
        String securityReport,
        String recommendations,
        String interviewQuestions,
        String interviewAnswers,
        String vivaQuestions,
        String presentationScript,
        String architectureExplanation,
        String readinessChecklist,
        String readinessReport,
        String resumeSummary,
        List<String> findings,
        Instant completedAt
) {
    public static AnalysisReport from(Analysis analysis) {
        RepositoryProject repository = analysis.getRepository();
        List<String> findings = analysis.getFindingsText() == null || analysis.getFindingsText().isBlank()
                ? List.of()
                : Arrays.stream(analysis.getFindingsText().split("\\R"))
                .filter(line -> !line.isBlank())
                .toList();
        return new AnalysisReport(
                analysis.getId(),
                repository.getId(),
                repository.getRepositoryName(),
                repository.getOwnerName(),
                repository.getGithubUrl(),
                analysis.getStatus(),
                repository.getDefaultBranch(),
                repository.getImportedFileCount(),
                AnalysisScores.from(analysis),
                ReadinessScores.from(analysis),
                analysis.getArchitectureReport(),
                analysis.getCodeQualityReport(),
                analysis.getSecurityReport(),
                analysis.getRecommendations(),
                analysis.getInterviewQuestions(),
                analysis.getInterviewAnswers(),
                analysis.getVivaQuestions(),
                analysis.getPresentationScript(),
                analysis.getArchitectureExplanation(),
                analysis.getReadinessChecklist(),
                analysis.getReadinessReport(),
                analysis.getResumeSummary(),
                findings,
                analysis.getCompletedAt()
        );
    }
}