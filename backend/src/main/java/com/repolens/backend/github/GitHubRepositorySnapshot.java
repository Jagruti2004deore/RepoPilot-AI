package com.repolens.backend.github;

import java.util.List;

public record GitHubRepositorySnapshot(
        String owner,
        String repositoryName,
        String defaultBranch,
        List<GitHubFile> files
) {
}
