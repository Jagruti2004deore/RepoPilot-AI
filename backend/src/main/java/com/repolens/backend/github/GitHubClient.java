package com.repolens.backend.github;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class GitHubClient {
    private static final int MAX_FILES = 70;
    private static final int MAX_FILE_BYTES = 120_000;
    private static final Set<String> IMPORTANT_EXTENSIONS = Set.of(
            ".java", ".kt", ".js", ".jsx", ".ts", ".tsx", ".json", ".xml", ".yml", ".yaml", ".properties", ".md"
    );
    private static final List<String> IMPORTANT_PATH_HINTS = List.of(
            "controller", "service", "entity", "model", "repository", "security", "config", "auth", "util", "pom.xml", "package.json", "readme"
    );

    private final RestClient.Builder restClientBuilder;

    @Value("${app.github.token:}")
    private String githubToken;

    public GitHubRepositorySnapshot fetchRepository(String owner, String repositoryName) {
        RestClient api = restClientBuilder.baseUrl("https://api.github.com").build();
        Map<?, ?> repo = api.get()
                .uri("/repos/{owner}/{repo}", owner, repositoryName)
                .headers(this::applyHeaders)
                .retrieve()
                .body(Map.class);

        if (repo == null || repo.get("default_branch") == null) {
            throw new IllegalArgumentException("GitHub repository not found or not accessible.");
        }

        String defaultBranch = repo.get("default_branch").toString();
        Map<?, ?> tree = api.get()
                .uri("/repos/{owner}/{repo}/git/trees/{branch}?recursive=1", owner, repositoryName, defaultBranch)
                .headers(this::applyHeaders)
                .retrieve()
                .body(Map.class);

        Object treeNodes = tree == null ? null : tree.get("tree");
        List<?> nodes = treeNodes instanceof List<?> list ? list : List.of();
        List<TreeNode> candidates = nodes.stream()
                .filter(Map.class::isInstance)
                .map(node -> (Map<?, ?>) node)
                .filter(node -> "blob".equals(node.get("type")))
                .map(node -> new TreeNode(
                        node.get("path").toString(),
                        sizeOf(node.get("size"))
                ))
                .filter(node -> shouldImport(node.path(), node.sizeBytes()))
                .sorted(Comparator.comparingInt((TreeNode node) -> importance(node.path())).reversed()
                        .thenComparing(TreeNode::path))
                .limit(MAX_FILES)
                .toList();

        RestClient raw = restClientBuilder.build();
        List<GitHubFile> files = new ArrayList<>();
        for (TreeNode candidate : candidates) {
            try {
                String encodedPath = encodePath(candidate.path());
                URI rawUri = URI.create("https://raw.githubusercontent.com/" + owner + "/" + repositoryName + "/" + defaultBranch + "/" + encodedPath);
                String content = raw.get()
                        .uri(rawUri)
                        .headers(this::applyHeaders)
                        .retrieve()
                        .body(String.class);
                if (content != null && !content.isBlank()) {
                    files.add(new GitHubFile(candidate.path(), languageFor(candidate.path()), candidate.sizeBytes(), content));
                }
            } catch (RuntimeException ignored) {
                // Some generated/binary-looking files can fail raw retrieval; skip and keep the analysis moving.
            }
        }

        return new GitHubRepositorySnapshot(owner, repositoryName, defaultBranch, files);
    }

    private void applyHeaders(HttpHeaders headers) {
        headers.set("Accept", "application/vnd.github+json");
        headers.set("X-GitHub-Api-Version", "2022-11-28");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.setBearerAuth(githubToken.trim());
        }
    }

    private long sizeOf(Object size) {
        if (size instanceof Number number) {
            return number.longValue();
        }
        return 0;
    }

    private boolean shouldImport(String path, long sizeBytes) {
        String normalized = path.toLowerCase(Locale.ROOT);
        if (sizeBytes <= 0 || sizeBytes > MAX_FILE_BYTES) {
            return false;
        }
        if (normalized.contains("node_modules/") || normalized.contains("target/") || normalized.contains("dist/")
                || normalized.contains("build/") || normalized.contains(".git/") || normalized.contains("package-lock.json")) {
            return false;
        }
        return IMPORTANT_EXTENSIONS.stream().anyMatch(normalized::endsWith);
    }

    private int importance(String path) {
        String normalized = path.toLowerCase(Locale.ROOT);
        int score = 0;
        for (String hint : IMPORTANT_PATH_HINTS) {
            if (normalized.contains(hint)) {
                score += 10;
            }
        }
        if (normalized.startsWith("src/")) {
            score += 4;
        }
        return score;
    }

    private String languageFor(String path) {
        String normalized = path.toLowerCase(Locale.ROOT);
        if (normalized.endsWith(".java")) return "Java";
        if (normalized.endsWith(".ts") || normalized.endsWith(".tsx")) return "TypeScript";
        if (normalized.endsWith(".js") || normalized.endsWith(".jsx")) return "JavaScript";
        if (normalized.endsWith(".xml")) return "XML";
        if (normalized.endsWith(".json")) return "JSON";
        if (normalized.endsWith(".yml") || normalized.endsWith(".yaml")) return "YAML";
        if (normalized.endsWith(".md")) return "Markdown";
        if (normalized.endsWith(".properties")) return "Properties";
        return "Text";
    }

    private String encodePath(String path) {
        return UriUtils.encodePath(path, StandardCharsets.UTF_8);
    }

    private record TreeNode(String path, long sizeBytes) {
    }
}
