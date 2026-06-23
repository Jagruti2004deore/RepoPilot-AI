package com.repolens.backend.repository.dto;

import com.repolens.backend.analysis.dto.AnalysisScores;
import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.RepositoryProject;

import java.time.Instant;

public record RepositorySummary(
        Long id,
        Long analysisId,
        String githubUrl,
        String owner,
        String name,
        String status,
        String defaultBranch,
        int importedFileCount,
        AnalysisScores scores,
        Instant importedAt
) {
    public static RepositorySummary from(RepositoryProject project) {
        return new RepositorySummary(
                project.getId(),
                null,
                project.getGithubUrl(),
                project.getOwnerName(),
                project.getRepositoryName(),
                "IMPORTED",
                project.getDefaultBranch(),
                project.getImportedFileCount(),
                null,
                project.getImportedAt()
        );
    }

    public static RepositorySummary from(RepositoryProject project, Analysis analysis) {
        return new RepositorySummary(
                project.getId(),
                analysis.getId(),
                project.getGithubUrl(),
                project.getOwnerName(),
                project.getRepositoryName(),
                analysis.getStatus(),
                project.getDefaultBranch(),
                project.getImportedFileCount(),
                AnalysisScores.from(analysis),
                project.getImportedAt()
        );
    }
}
