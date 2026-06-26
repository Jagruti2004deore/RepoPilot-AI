package com.repolens.backend.analysis;

import java.util.List;

public record StaticAnalysisResult(
        double architectureScore,
        double securityScore,
        double maintainabilityScore,
        double documentationScore,
        double testingScore,
        double overallScore,
        double resumeReadinessScore,
        double interviewReadinessScore,
        double githubQualityScore,
        double deploymentReadinessScore,
        double demoReadinessScore,
        double projectReadinessScore,
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
        List<Finding> findings
) {
}