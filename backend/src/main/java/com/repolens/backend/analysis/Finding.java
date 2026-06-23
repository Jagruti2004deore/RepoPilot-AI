package com.repolens.backend.analysis;

public record Finding(
        String category,
        String severity,
        String filePath,
        String title,
        String recommendation
) {
}
