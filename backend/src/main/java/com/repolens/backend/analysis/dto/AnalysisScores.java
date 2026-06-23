package com.repolens.backend.analysis.dto;

import com.repolens.backend.repository.Analysis;

public record AnalysisScores(
        double architecture,
        double security,
        double maintainability,
        double documentation,
        double testing,
        double overall
) {
    public static AnalysisScores from(Analysis analysis) {
        return new AnalysisScores(
                analysis.getArchitectureScore(),
                analysis.getSecurityScore(),
                analysis.getMaintainabilityScore(),
                analysis.getDocumentationScore(),
                analysis.getTestingScore(),
                analysis.getOverallScore()
        );
    }
}
