package com.repolens.backend.analysis;

import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.file.RepositoryFile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StaticAnalysisService {
    public StaticAnalysisResult analyze(RepositoryProject project, List<RepositoryFile> files) {
        List<Finding> findings = new ArrayList<>();
        Set<String> paths = files.stream().map(RepositoryFile::getPath).collect(Collectors.toSet());
        String allPaths = String.join("\n", paths).toLowerCase(Locale.ROOT);
        String allContentLower = files.stream().map(RepositoryFile::getContent).collect(Collectors.joining("\n")).toLowerCase(Locale.ROOT);

        boolean hasControllers = allPaths.contains("controller") || allContentLower.contains("@restcontroller");
        boolean hasServices = allPaths.contains("service") || allContentLower.contains("@service");
        boolean hasRepositories = allPaths.contains("repository") || allContentLower.contains("@repository");
        boolean hasEntities = allPaths.contains("entity") || allPaths.contains("model") || allContentLower.contains("@entity");
        boolean hasSecurity = allPaths.contains("security") || allContentLower.contains("springsecurity") || allContentLower.contains("securityfilterchain");
        boolean hasReadme = allPaths.contains("readme.md");
        boolean hasTests = allPaths.contains("/test/") || allPaths.contains("\\test\\") || allPaths.contains(".test.") || allPaths.contains("spec.");
        boolean hasCi = allPaths.contains(".github/workflows") || allPaths.contains("gitlab-ci") || allPaths.contains("jenkinsfile");

        for (RepositoryFile file : files) {
            inspectFile(file, findings);
        }

        if (files.isEmpty()) {
            findings.add(new Finding("Import", "HIGH", "repository", "No analyzable files imported", "Push source files to GitHub or verify the repository URL and branch."));
        }
        if (!hasControllers) {
            findings.add(new Finding("Architecture", "MEDIUM", "repository", "No controller/API layer detected", "Add a clear HTTP/API entry layer or document why the project is not API-based."));
        }
        if (!hasServices) {
            findings.add(new Finding("Architecture", "MEDIUM", "repository", "No service layer detected", "Keep business logic in services instead of controllers or repositories."));
        }
        if (!hasReadme) {
            findings.add(new Finding("Documentation", "LOW", "README.md", "README not detected", "Add setup, architecture, API, screenshots, and demo instructions."));
        }
        if (!hasTests) {
            findings.add(new Finding("Testing", "MEDIUM", "src/test", "Test files not detected", "Add unit and integration tests for authentication, repository import, and analysis flows."));
        }
        if (!hasCi && files.size() > 8) {
            findings.add(new Finding("Delivery", "LOW", ".github/workflows", "CI workflow not detected", "Add a GitHub Actions workflow that builds backend and frontend on every push."));
        }

        double architectureScore = clamp(55 + (hasControllers ? 10 : 0) + (hasServices ? 12 : 0) + (hasRepositories ? 8 : 0) + (hasEntities ? 8 : 0) - count(findings, "Architecture") * 6 - count(findings, "Import") * 10);
        double securityScore = clamp(82 + (hasSecurity ? 6 : -8) - weighted(findings, "Security"));
        double maintainabilityScore = clamp(84 - weighted(findings, "Maintainability") - count(findings, "Code Quality") * 4 - count(findings, "Scalability") * 3);
        double documentationScore = clamp((hasReadme ? 78 : 52) + (hasCi ? 5 : 0) - count(findings, "Documentation") * 4);
        double testingScore = clamp(hasTests ? 72 : 45);
        double overallScore = Math.round((architectureScore + securityScore + maintainabilityScore + documentationScore + testingScore) / 5.0 * 10.0) / 10.0;

        String architecture = architectureReport(project, files, hasControllers, hasServices, hasRepositories, hasEntities, hasCi, findings);
        String codeQuality = codeQualityReport(files, findings);
        String security = securityReport(hasSecurity, findings);
        String recommendations = recommendations(findings);
        String interview = interviewQuestions(project, hasSecurity, hasControllers, hasServices, hasRepositories, files);
        String resume = resumeSummary(project, hasSecurity, hasControllers, hasServices, hasRepositories, files);

        return new StaticAnalysisResult(
                architectureScore,
                securityScore,
                maintainabilityScore,
                documentationScore,
                testingScore,
                overallScore,
                architecture,
                codeQuality,
                security,
                recommendations,
                interview,
                resume,
                findings
        );
    }

    private void inspectFile(RepositoryFile file, List<Finding> findings) {
        String content = file.getContent() == null ? "" : file.getContent();
        String lower = content.toLowerCase(Locale.ROOT);
        String path = file.getPath();
        String normalizedPath = path.toLowerCase(Locale.ROOT);
        long lineCount = content.lines().count();

        if (content.contains("@Autowired\n") || content.contains("@Autowired\r\n")) {
            findings.add(new Finding("Code Quality", "MEDIUM", path, "Possible field injection", "Prefer constructor injection for immutability and easier testing."));
        }
        if ((normalizedPath.contains("controller") || content.contains("@RestController")) && content.contains("@RequestBody") && !content.contains("@Valid")) {
            findings.add(new Finding("Security", "MEDIUM", path, "Missing request validation", "Validate request DTOs with @Valid and bean validation annotations."));
        }
        if (looksLikeHardcodedSecret(lower)) {
            findings.add(new Finding("Security", "HIGH", path, "Possible hardcoded secret", "Move secrets to environment variables or a secrets manager."));
        }
        if ((lower.contains("permitall()") && lower.contains("/**")) || lower.contains("anyrequest().permitall()")) {
            findings.add(new Finding("Security", "HIGH", path, "Broad permitAll rule", "Avoid permitting all endpoints; scope public routes explicitly."));
        }
        if (lower.contains("@crossorigin(\"*\")") || lower.contains("allowedorigins(list.of(\"*\"))")) {
            findings.add(new Finding("Security", "MEDIUM", path, "Wildcard CORS origin", "Use environment-specific allowed origins instead of permitting every browser origin."));
        }
        if (lower.contains("printstacktrace()")) {
            findings.add(new Finding("Maintainability", "LOW", path, "printStackTrace usage", "Use structured logging and consistent exception handling."));
        }
        if (lower.contains("catch (") && lower.contains("ignored")) {
            findings.add(new Finding("Maintainability", "LOW", path, "Ignored exception", "Log context or return a clear partial-failure signal when recoverable work is skipped."));
        }
        if (lower.contains("select *") && lower.contains("+")) {
            findings.add(new Finding("Security", "HIGH", path, "Possible SQL injection risk", "Use parameterized queries or Spring Data repositories."));
        }
        if (lineCount > 350) {
            findings.add(new Finding("Maintainability", "MEDIUM", path, "Large file", "Split large classes/components into smaller cohesive units."));
        }
        if (content.contains("List<") && (normalizedPath.contains("controller") || normalizedPath.contains("repository")) && !content.contains("Pageable")) {
            findings.add(new Finding("Scalability", "MEDIUM", path, "Possible missing pagination", "Use Pageable/Page for list endpoints that may grow."));
        }
        if (lower.contains("console.log") || lower.contains("system.out.println")) {
            findings.add(new Finding("Code Quality", "LOW", path, "Debug logging left in code", "Replace ad-hoc console output with structured logging or remove it before release."));
        }
        if (lower.contains("todo") || lower.contains("fixme")) {
            findings.add(new Finding("Maintainability", "LOW", path, "TODO/FIXME marker", "Turn TODO comments into tracked issues or finish them before demo."));
        }
    }

    private boolean looksLikeHardcodedSecret(String lower) {
        boolean secretWord = lower.contains("password=") || lower.contains("secret=") || lower.contains("api_key") || lower.contains("token=");
        return secretWord && !lower.contains("${") && !lower.contains("changeme") && !lower.contains("change-this");
    }

    private String architectureReport(RepositoryProject project, List<RepositoryFile> files, boolean controllers, boolean services, boolean repositories, boolean entities, boolean ci, List<Finding> findings) {
        String style = controllers && services && repositories ? "Layered MVC architecture" : "Partially layered application";
        return "Current architecture: " + style + "\n"
                + "Repository: " + project.getOwnerName() + "/" + project.getRepositoryName() + "\n"
                + "Files analyzed: " + files.size() + "\n"
                + "Tech stack detected: " + techStack(files) + "\n"
                + "Language breakdown: " + languageBreakdown(files) + "\n"
                + "Detected layers: " + layerText(controllers, services, repositories, entities) + "\n"
                + "CI/CD detected: " + (ci ? "yes" : "not yet") + "\n"
                + "Main architecture risks: " + summarize(findings, "Architecture");
    }

    private String codeQualityReport(List<RepositoryFile> files, List<Finding> findings) {
        return "Reviewed source across " + languages(files) + ".\n"
                + "Complexity hotspots: " + hotspots(files) + "\n"
                + "API endpoint hints: " + endpointHints(files) + "\n"
                + "Code quality findings: " + summarize(findings, "Code Quality") + "\n"
                + "Maintainability findings: " + summarize(findings, "Maintainability") + "\n"
                + "Scalability findings: " + summarize(findings, "Scalability");
    }

    private String securityReport(boolean hasSecurity, List<Finding> findings) {
        long high = findings.stream().filter(f -> "Security".equals(f.category()) && "HIGH".equals(f.severity())).count();
        long medium = findings.stream().filter(f -> "Security".equals(f.category()) && "MEDIUM".equals(f.severity())).count();
        return "Security configuration detected: " + (hasSecurity ? "yes" : "not clearly detected") + ".\n"
                + "Security risk level: " + riskLabel(high, medium) + "\n"
                + "Security findings: " + summarize(findings, "Security") + "\n"
                + "Focus areas: validation, authorization boundaries, secret management, CORS, and safe query construction.";
    }

    private String recommendations(List<Finding> findings) {
        if (findings.isEmpty()) {
            return "No major rule-based issues detected. Add more tests, document architecture decisions, and set up CI next.";
        }
        return findings.stream()
                .sorted(Comparator.comparingInt((Finding finding) -> severityRank(finding.severity())).reversed())
                .limit(10)
                .map(finding -> "- [" + finding.severity() + "] " + finding.title() + " in " + finding.filePath() + ": " + finding.recommendation())
                .collect(Collectors.joining("\n"));
    }

    private String interviewQuestions(RepositoryProject project, boolean security, boolean controllers, boolean services, boolean repositories, List<RepositoryFile> files) {
        List<String> questions = new ArrayList<>();
        questions.add("Explain the architecture of " + project.getRepositoryName() + " and the tradeoffs behind the current layering.");
        questions.add("Which files are the main complexity hotspots and how would you refactor them?");
        if (controllers) questions.add("How do controllers validate, authorize, and route incoming requests?");
        if (services) questions.add("What business logic belongs in the service layer, and how is it tested?");
        if (repositories) questions.add("How does the persistence layer handle queries, pagination, and transaction boundaries?");
        if (security) questions.add("Explain the authentication and authorization flow in this project.");
        if (!files.isEmpty()) questions.add("What would you improve first if the project needed to support 10x more users?");
        return questions.stream().map(question -> "- " + question).collect(Collectors.joining("\n"));
    }

    private String resumeSummary(RepositoryProject project, boolean security, boolean controllers, boolean services, boolean repositories, List<RepositoryFile> files) {
        List<String> parts = new ArrayList<>();
        parts.add("GitHub repository import and static code analysis");
        if (controllers && services && repositories) parts.add("layered REST architecture review");
        if (security) parts.add("security configuration assessment");
        parts.add("maintainability scoring across " + files.size() + " files");
        return "Built RepoLensAI analysis for " + project.getRepositoryName() + " with " + String.join(", ", parts) + ".";
    }

    private String languageBreakdown(List<RepositoryFile> files) {
        if (files.isEmpty()) return "no files imported";
        Map<String, Long> counts = files.stream().collect(Collectors.groupingBy(RepositoryFile::getLanguage, LinkedHashMap::new, Collectors.counting()));
        return counts.entrySet().stream()
                .map(entry -> entry.getKey() + " " + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    private String languages(List<RepositoryFile> files) {
        if (files.isEmpty()) return "no imported files";
        return files.stream().map(RepositoryFile::getLanguage).distinct().collect(Collectors.joining(", "));
    }

    private String techStack(List<RepositoryFile> files) {
        String joined = files.stream().map(file -> file.getPath() + "\n" + file.getContent()).collect(Collectors.joining("\n")).toLowerCase(Locale.ROOT);
        List<String> tech = new ArrayList<>();
        if (joined.contains("spring-boot") || joined.contains("@springbootapplication")) tech.add("Spring Boot");
        if (joined.contains("jjwt") || joined.contains("jsonwebtoken") || joined.contains("jwts.builder")) tech.add("JWT");
        if (joined.contains("postgresql") || joined.contains("jdbc:postgresql")) tech.add("PostgreSQL");
        if (joined.contains("pgvector")) tech.add("pgvector");
        if (joined.contains("react") || joined.contains("react-dom")) tech.add("React");
        if (joined.contains("vite")) tech.add("Vite");
        if (joined.contains("tailwindcss") || joined.contains("@import \"tailwindcss\"")) tech.add("Tailwind CSS");
        if (joined.contains("docker-compose") || joined.contains("services:")) tech.add("Docker Compose");
        if (joined.contains("maven") || joined.contains("pom.xml")) tech.add("Maven");
        return tech.isEmpty() ? "not enough evidence" : String.join(", ", tech);
    }

    private String hotspots(List<RepositoryFile> files) {
        List<String> top = files.stream()
                .sorted(Comparator.comparingLong((RepositoryFile file) -> file.getContent() == null ? 0 : file.getContent().lines().count()).reversed())
                .limit(4)
                .map(file -> file.getPath() + " (" + (file.getContent() == null ? 0 : file.getContent().lines().count()) + " lines)")
                .toList();
        return top.isEmpty() ? "no files imported" : String.join("; ", top);
    }

    private String endpointHints(List<RepositoryFile> files) {
        long mappings = files.stream()
                .map(RepositoryFile::getContent)
                .filter(content -> content != null)
                .mapToLong(content -> countOccurrences(content, "@GetMapping") + countOccurrences(content, "@PostMapping") + countOccurrences(content, "@PutMapping") + countOccurrences(content, "@DeleteMapping") + countOccurrences(content, "@PatchMapping"))
                .sum();
        return mappings == 0 ? "no Spring mapping annotations detected" : mappings + " Spring endpoint annotations detected";
    }

    private long countOccurrences(String content, String token) {
        long count = 0;
        int index = content.indexOf(token);
        while (index >= 0) {
            count++;
            index = content.indexOf(token, index + token.length());
        }
        return count;
    }

    private String layerText(boolean controllers, boolean services, boolean repositories, boolean entities) {
        List<String> layers = new ArrayList<>();
        if (controllers) layers.add("controllers");
        if (services) layers.add("services");
        if (repositories) layers.add("repositories");
        if (entities) layers.add("entities/models");
        return layers.isEmpty() ? "no clear application layers" : String.join(", ", layers);
    }

    private String riskLabel(long high, long medium) {
        if (high > 0) return "high";
        if (medium > 1) return "medium";
        if (medium == 1) return "low-medium";
        return "low from current rules";
    }

    private String summarize(List<Finding> findings, String category) {
        List<Finding> filtered = findings.stream().filter(finding -> category.equals(finding.category())).limit(4).toList();
        if (filtered.isEmpty()) {
            return "no major issues from current rules";
        }
        return filtered.stream().map(Finding::title).collect(Collectors.joining("; "));
    }

    private long count(List<Finding> findings, String category) {
        return findings.stream().filter(finding -> category.equals(finding.category())).count();
    }

    private double weighted(List<Finding> findings, String category) {
        return findings.stream()
                .filter(finding -> category.equals(finding.category()))
                .mapToDouble(finding -> switch (finding.severity()) {
                    case "HIGH" -> 12;
                    case "MEDIUM" -> 7;
                    default -> 3;
                })
                .sum();
    }

    private int severityRank(String severity) {
        return switch (severity) {
            case "HIGH" -> 3;
            case "MEDIUM" -> 2;
            default -> 1;
        };
    }

    private double clamp(double score) {
        return Math.max(10, Math.min(100, Math.round(score * 10.0) / 10.0));
    }
}
