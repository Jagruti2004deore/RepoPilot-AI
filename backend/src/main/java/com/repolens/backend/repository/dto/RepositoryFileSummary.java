package com.repolens.backend.repository.dto;

import com.repolens.backend.repository.file.RepositoryFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public record RepositoryFileSummary(
        Long id,
        String path,
        String language,
        long sizeBytes,
        long lineCount,
        String role,
        List<String> signals
) {
    public static RepositoryFileSummary from(RepositoryFile file) {
        String path = file.getPath();
        String normalized = path.toLowerCase(Locale.ROOT);
        String content = file.getContent() == null ? "" : file.getContent();
        List<String> signals = new ArrayList<>();

        if (content.contains("@RestController") || normalized.contains("controller")) {
            signals.add("API boundary");
        }
        if (content.contains("@Service") || normalized.contains("service")) {
            signals.add("Business logic");
        }
        if (content.contains("@Entity") || normalized.contains("entity") || normalized.contains("model")) {
            signals.add("Domain model");
        }
        if (content.contains("SecurityFilterChain") || normalized.contains("security")) {
            signals.add("Security config");
        }
        if (content.lines().count() > 300) {
            signals.add("Large file");
        }
        if (content.contains("TODO") || content.contains("FIXME")) {
            signals.add("Needs follow-up");
        }

        return new RepositoryFileSummary(
                file.getId(),
                path,
                file.getLanguage(),
                file.getSizeBytes(),
                content.lines().count(),
                roleFor(normalized, content),
                signals
        );
    }

    private static String roleFor(String normalizedPath, String content) {
        if (content.contains("@RestController") || normalizedPath.contains("controller")) return "Controller/API";
        if (content.contains("@Service") || normalizedPath.contains("service")) return "Service";
        if (content.contains("@Repository") || normalizedPath.contains("repository")) return "Repository";
        if (content.contains("@Entity") || normalizedPath.contains("entity")) return "Entity/Model";
        if (content.contains("SecurityFilterChain") || normalizedPath.contains("security")) return "Security";
        if (normalizedPath.endsWith("pom.xml") || normalizedPath.endsWith("package.json")) return "Dependency manifest";
        if (normalizedPath.contains("readme")) return "Documentation";
        return "Source/config";
    }
}
