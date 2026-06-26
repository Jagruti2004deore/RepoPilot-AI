package com.repolens.backend.analysis.dto;

import com.repolens.backend.repository.Analysis;

public record ReadinessScores(
        double resume,
        double interview,
        double github,
        double deployment,
        double demo,
        double overall
) {
    public static ReadinessScores from(Analysis analysis) {
        return new ReadinessScores(
                analysis.getResumeReadinessScore(),
                analysis.getInterviewReadinessScore(),
                analysis.getGithubQualityScore(),
                analysis.getDeploymentReadinessScore(),
                analysis.getDemoReadinessScore(),
                analysis.getProjectReadinessScore()
        );
    }
}