package com.repolens.backend.github;

public record GitHubFile(
        String path,
        String language,
        long sizeBytes,
        String content
) {
}
