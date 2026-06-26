package com.repolens.backend.analysis.dto;

import com.repolens.backend.repository.Analysis;

import java.time.Instant;

public record AnalysisHistoryItem(
        Long analysisId,
        String status,
        AnalysisScores scores,
        ReadinessScores readinessScores,
        int findingCount,
        Instant completedAt
) {
    public static AnalysisHistoryItem from(Analysis analysis) {
        int findingCount = analysis.getFindingsText() == null || analysis.getFindingsText().isBlank()
                ? 0
                : analysis.getFindingsText().split("\\R").length;
        return new AnalysisHistoryItem(
                analysis.getId(),
                analysis.getStatus(),
                AnalysisScores.from(analysis),
                ReadinessScores.from(analysis),
                findingCount,
                analysis.getCompletedAt()
        );
    }
}