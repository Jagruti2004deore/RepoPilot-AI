package com.repolens.backend.analysis;

import java.util.List;

public record StaticAnalysisResult(
        double architectureScore,
        double securityScore,
        double maintainabilityScore,
        double documentationScore,
        double testingScore,
        double overallScore,
        String architectureReport,
        String codeQualityReport,
        String securityReport,
        String recommendations,
        String interviewQuestions,
        String resumeSummary,
        List<Finding> findings
) {
}
