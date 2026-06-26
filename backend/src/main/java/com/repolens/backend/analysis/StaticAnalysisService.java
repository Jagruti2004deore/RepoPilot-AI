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

        ProjectSignals signals = new ProjectSignals(
                allPaths.contains("controller") || allContentLower.contains("@restcontroller"),
                allPaths.contains("service") || allContentLower.contains("@service"),
                allPaths.contains("repository") || allContentLower.contains("@repository"),
                allPaths.contains("entity") || allPaths.contains("model") || allContentLower.contains("@entity"),
                allPaths.contains("security") || allContentLower.contains("springsecurity") || allContentLower.contains("securityfilterchain"),
                allPaths.contains("readme.md"),
                allPaths.contains("/test/") || allPaths.contains("\\test\\") || allPaths.contains(".test.") || allPaths.contains("spec."),
                allPaths.contains(".github/workflows") || allPaths.contains("gitlab-ci") || allPaths.contains("jenkinsfile"),
                allPaths.contains("docker-compose") || allContentLower.contains("docker compose") || allContentLower.contains("services:"),
                allPaths.contains(".env.example") || allContentLower.contains("env.example"),
                allContentLower.contains("screenshot") || allContentLower.contains("demo") || allContentLower.contains("localhost"),
                allContentLower.contains("deploy") || allContentLower.contains("render") || allContentLower.contains("vercel") || allContentLower.contains("railway")
        );

        for (RepositoryFile file : files) {
            inspectFile(file, findings);
        }

        if (files.isEmpty()) {
            findings.add(new Finding("Import", "HIGH", "repository", "No analyzable files imported", "Push source files to GitHub or verify the repository URL and branch."));
        }
        if (!signals.controllers()) {
            findings.add(new Finding("Architecture", "MEDIUM", "repository", "No controller/API layer detected", "Add a clear HTTP/API entry layer or document why the project is not API-based."));
        }
        if (!signals.services()) {
            findings.add(new Finding("Architecture", "MEDIUM", "repository", "No service layer detected", "Keep business logic in services instead of controllers or repositories."));
        }
        if (!signals.readme()) {
            findings.add(new Finding("Documentation", "LOW", "README.md", "README not detected", "Add setup, architecture, API, screenshots, and demo instructions."));
        }
        if (!signals.tests()) {
            findings.add(new Finding("Testing", "MEDIUM", "src/test", "Test files not detected", "Add unit and integration tests for authentication, repository import, and analysis flows."));
        }
        if (!signals.ci() && files.size() > 8) {
            findings.add(new Finding("Delivery", "LOW", ".github/workflows", "CI workflow not detected", "Add a GitHub Actions workflow that builds backend and frontend on every push."));
        }

        double architectureScore = clamp(55 + (signals.controllers() ? 10 : 0) + (signals.services() ? 12 : 0) + (signals.repositories() ? 8 : 0) + (signals.entities() ? 8 : 0) - count(findings, "Architecture") * 6 - count(findings, "Import") * 10);
        double securityScore = clamp(82 + (signals.security() ? 6 : -8) - weighted(findings, "Security"));
        double maintainabilityScore = clamp(84 - weighted(findings, "Maintainability") - count(findings, "Code Quality") * 4 - count(findings, "Scalability") * 3);
        double documentationScore = clamp((signals.readme() ? 78 : 52) + (signals.ci() ? 5 : 0) - count(findings, "Documentation") * 4);
        double testingScore = clamp(signals.tests() ? 72 : 45);
        double overallScore = Math.round((architectureScore + securityScore + maintainabilityScore + documentationScore + testingScore) / 5.0 * 10.0) / 10.0;

        double resumeReadinessScore = clamp(52 + files.size() / 2.0 + (signals.readme() ? 10 : 0) + (signals.security() ? 8 : 0) + (signals.docker() ? 6 : 0) + (signals.tests() ? 6 : 0));
        double interviewReadinessScore = clamp(50 + (signals.controllers() ? 8 : 0) + (signals.services() ? 8 : 0) + (signals.repositories() ? 8 : 0) + (signals.security() ? 10 : 0) + (signals.tests() ? 8 : 0));
        double githubQualityScore = clamp(45 + (signals.readme() ? 18 : 0) + (signals.envExample() ? 8 : 0) + (signals.demoEvidence() ? 8 : 0) + (signals.ci() ? 8 : 0) + (files.size() > 15 ? 8 : 0));
        double deploymentReadinessScore = clamp(40 + (signals.docker() ? 18 : 0) + (signals.envExample() ? 14 : 0) + (signals.deploymentNotes() ? 14 : 0) + (signals.ci() ? 8 : 0));
        double demoReadinessScore = clamp(45 + (overallScore * 0.25) + (signals.readme() ? 10 : 0) + (signals.demoEvidence() ? 10 : 0) + (files.size() > 0 ? 10 : 0));
        double projectReadinessScore = Math.round((resumeReadinessScore + interviewReadinessScore + githubQualityScore + deploymentReadinessScore + demoReadinessScore) / 5.0 * 10.0) / 10.0;

        String architecture = architectureReport(project, files, signals, findings);
        String codeQuality = codeQualityReport(files, findings);
        String security = securityReport(signals.security(), findings);
        String recommendations = recommendations(findings);
        String interview = interviewQuestions(project, signals, files);
        String interviewAnswers = interviewAnswers(project, signals, files);
        String vivaQuestions = vivaQuestions(project, signals);
        String presentationScript = presentationScript(project, files, signals, projectReadinessScore);
        String architectureExplanation = architectureExplanation(project, files, signals);
        String readinessChecklist = readinessChecklist(signals);
        String readinessReport = readinessReport(projectReadinessScore, resumeReadinessScore, interviewReadinessScore, githubQualityScore, deploymentReadinessScore, demoReadinessScore, signals);
        String resume = resumeSummary(project, signals, files);
        String resumeBullets = resumeBullets(project, signals, files, projectReadinessScore);
        String githubProfileTips = githubProfileTips(signals);
        String readmeSuggestions = readmeSuggestions(project, signals);
        String projectTitleSuggestions = projectTitleSuggestions(project);

        return new StaticAnalysisResult(
                architectureScore,
                securityScore,
                maintainabilityScore,
                documentationScore,
                testingScore,
                overallScore,
                resumeReadinessScore,
                interviewReadinessScore,
                githubQualityScore,
                deploymentReadinessScore,
                demoReadinessScore,
                projectReadinessScore,
                architecture,
                codeQuality,
                security,
                recommendations,
                interview,
                interviewAnswers,
                vivaQuestions,
                presentationScript,
                architectureExplanation,
                readinessChecklist,
                readinessReport,
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

    private String architectureReport(RepositoryProject project, List<RepositoryFile> files, ProjectSignals signals, List<Finding> findings) {
        String style = signals.controllers() && signals.services() && signals.repositories() ? "Layered MVC architecture" : "Partially layered application";
        return "Current architecture: " + style + "\n"
                + "Repository: " + project.getOwnerName() + "/" + project.getRepositoryName() + "\n"
                + "Files analyzed: " + files.size() + "\n"
                + "Tech stack detected: " + techStack(files) + "\n"
                + "Language breakdown: " + languageBreakdown(files) + "\n"
                + "Detected layers: " + layerText(signals) + "\n"
                + "CI/CD detected: " + (signals.ci() ? "yes" : "not yet") + "\n"
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

    private String readinessChecklist(ProjectSignals signals) {
        return checklistLine(signals.readme(), "README includes setup and feature overview") + "\n"
                + checklistLine(signals.envExample(), "Environment example is available") + "\n"
                + checklistLine(signals.tests(), "Tests exist for core behavior") + "\n"
                + checklistLine(signals.docker(), "Docker or Compose setup is present") + "\n"
                + checklistLine(signals.ci(), "CI workflow is configured") + "\n"
                + checklistLine(signals.demoEvidence(), "Demo notes, screenshots, or localhost instructions are documented") + "\n"
                + checklistLine(signals.deploymentNotes(), "Deployment notes are present") + "\n"
                + checklistLine(signals.controllers() && signals.services(), "Architecture layers are explainable");
    }

    private String checklistLine(boolean done, String label) {
        return (done ? "[DONE] " : "[TODO] ") + label;
    }

    private String readinessReport(double overall, double resume, double interview, double github, double deployment, double demo, ProjectSignals signals) {
        return "Project readiness: " + Math.round(overall) + "/100\n"
                + "Resume readiness: " + Math.round(resume) + "/100\n"
                + "Interview readiness: " + Math.round(interview) + "/100\n"
                + "GitHub quality: " + Math.round(github) + "/100\n"
                + "Deployment readiness: " + Math.round(deployment) + "/100\n"
                + "Demo readiness: " + Math.round(demo) + "/100\n"
                + "Next best move: " + nextBestMove(signals);
    }

    private String nextBestMove(ProjectSignals signals) {
        if (!signals.readme()) return "Write a professional README with setup, screenshots, and architecture.";
        if (!signals.envExample()) return "Add .env.example so reviewers can run the project safely.";
        if (!signals.tests()) return "Add focused tests for the main backend services and API paths.";
        if (!signals.deploymentNotes()) return "Add deployment notes and environment variable documentation.";
        if (!signals.ci()) return "Add GitHub Actions to build backend and frontend on every push.";
        return "Polish the demo script and push final screenshots.";
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

    private String interviewQuestions(RepositoryProject project, ProjectSignals signals, List<RepositoryFile> files) {
        List<String> questions = new ArrayList<>();
        questions.add("Explain the architecture of " + project.getRepositoryName() + " and the tradeoffs behind the current layering.");
        questions.add("Which files are the main complexity hotspots and how would you refactor them?");
        if (signals.controllers()) questions.add("How do controllers validate, authorize, and route incoming requests?");
        if (signals.services()) questions.add("What business logic belongs in the service layer, and how is it tested?");
        if (signals.repositories()) questions.add("How does the persistence layer handle queries, pagination, and transaction boundaries?");
        if (signals.security()) questions.add("Explain the authentication and authorization flow in this project.");
        if (!files.isEmpty()) questions.add("What would you improve first if the project needed to support 10x more users?");
        return questions.stream().map(question -> "- " + question).collect(Collectors.joining("\n"));
    }

    private String interviewAnswers(RepositoryProject project, ProjectSignals signals, List<RepositoryFile> files) {
        return "Q: Explain this project in one minute.\n"
                + "A: " + project.getRepositoryName() + " is a GitHub project analysis platform. It imports repository files, detects architecture and quality signals, stores analysis history, and turns the result into interview, resume, and demo guidance.\n\n"
                + "Q: What architecture did you use?\n"
                + "A: The project is organized as " + layerText(signals) + ". The goal is to keep API handling, business logic, persistence, and security concerns separate so the code is easier to test and explain.\n\n"
                + "Q: What is the strongest technical part?\n"
                + "A: The strongest part is the end-to-end analysis workflow: GitHub import, file inventory, static scoring, persisted reports, and re-analysis. It shows backend API design, database modeling, and frontend state handling together.\n\n"
                + "Q: What would you improve next?\n"
                + "A: I would add deeper tests, exportable reports, and deployment automation. That would make the project more production-ready and easier to demonstrate.\n\n"
                + "Q: What files should you discuss in an interview?\n"
                + "A: Start with the application entry point, repository import service, static analysis service, security configuration, and the main React dashboard. Current hotspots are " + hotspots(files) + ".";
    }

    private String vivaQuestions(RepositoryProject project, ProjectSignals signals) {
        List<String> questions = new ArrayList<>();
        questions.add("What problem does " + project.getRepositoryName() + " solve?");
        questions.add("How does the system import and store GitHub repository files?");
        questions.add("Why is a layered architecture useful in this project?");
        questions.add("How are analysis scores calculated?");
        questions.add("How is user authentication handled?");
        questions.add("What database tables are required and why?");
        questions.add("What are the limitations of rule-based static analysis?");
        questions.add("How would you deploy this project for real users?");
        if (!signals.tests()) questions.add("Which tests would you add first before final submission?");
        return questions.stream().map(question -> "- " + question).collect(Collectors.joining("\n"));
    }

    private String presentationScript(RepositoryProject project, List<RepositoryFile> files, ProjectSignals signals, double readiness) {
        return "Hi, my project is " + project.getRepositoryName() + ". It is an AI-style project mentor for GitHub repositories. "
                + "Instead of only showing code issues, it checks whether a project is ready for GitHub, resume, viva, and interviews. "
                + "The backend imports repository files, stores them in PostgreSQL, runs static analysis rules, and produces architecture, security, maintainability, readiness, and interview reports. "
                + "The frontend gives a dashboard with scores, file inventory, findings, recommendations, and coaching content. "
                + "For this analysis, RepoPilot AI reviewed " + files.size() + " files and detected " + layerText(signals) + ". "
                + "The current project readiness score is " + Math.round(readiness) + " out of 100. "
                + "The main future scope is deeper analysis history, Markdown/PDF export, deployment, and more language-specific rules.";
    }

    private String architectureExplanation(RepositoryProject project, List<RepositoryFile> files, ProjectSignals signals) {
        return "Architecture explanation for " + project.getRepositoryName() + ":\n"
                + "1. Frontend: React UI handles login, repository import, report viewing, file inventory, and re-analysis actions.\n"
                + "2. Backend API: Spring Boot controllers expose authentication and repository analysis endpoints.\n"
                + "3. Service layer: Import and analysis services coordinate GitHub fetching, ownership checks, scoring, and report generation.\n"
                + "4. Persistence: JPA repositories store users, repositories, files, and analysis records in PostgreSQL.\n"
                + "5. Security: JWT authentication protects repository and report endpoints.\n"
                + "Detected layers: " + layerText(signals) + ".\n"
                + "Files analyzed: " + files.size() + ".";
    }

    private String resumeSummary(RepositoryProject project, ProjectSignals signals, List<RepositoryFile> files) {
        List<String> parts = new ArrayList<>();
        parts.add("GitHub repository import and static code analysis");
        if (signals.controllers() && signals.services() && signals.repositories()) parts.add("layered REST architecture review");
        if (signals.security()) parts.add("security configuration assessment");
        parts.add("project readiness scoring across " + files.size() + " files");
        parts.add("interview and viva preparation report generation");
        return "Built RepoPilot AI analysis for " + project.getRepositoryName() + " with " + String.join(", ", parts) + ".";
    }

    private String resumeBullets(RepositoryProject project, ProjectSignals signals, List<RepositoryFile> files, double readiness) {
        List<String> bullets = new ArrayList<>();
        bullets.add("Built RepoPilot AI-style GitHub repository analyzer for " + project.getOwnerName() + "/" + project.getRepositoryName() + " with " + files.size() + " imported files and " + Math.round(readiness) + "/100 project readiness scoring.");
        bullets.add("Designed static analysis reports covering architecture, security, maintainability, documentation, testing, and interview preparation outputs.");
        if (signals.controllers() && signals.services()) {
            bullets.add("Implemented explainable layered architecture review across API, service, persistence, and model boundaries.");
        }
        if (signals.security()) {
            bullets.add("Added security-focused checks for validation, authorization, CORS, secrets, and risky query patterns.");
        }
        if (signals.docker()) {
            bullets.add("Packaged local development with Docker-based PostgreSQL support for repeatable setup.");
        }
        return bullets.stream().map(bullet -> "- " + bullet).collect(Collectors.joining("\n"));
    }

    private String githubProfileTips(ProjectSignals signals) {
        List<String> tips = new ArrayList<>();
        tips.add("Pin this project near the top of your GitHub profile with a short description focused on project readiness, code review, and architecture analysis.");
        tips.add("Add screenshots of the dashboard, readiness checklist, file inventory, and coach sections.");
        tips.add("Keep the repository topics specific: spring-boot, react, postgres, github-api, static-analysis, portfolio-project.");
        if (!signals.ci()) tips.add("Add a GitHub Actions build badge after CI is configured.");
        if (!signals.deploymentNotes()) tips.add("Add a demo link or deployment notes so reviewers can understand how to run it quickly.");
        return tips.stream().map(tip -> "- " + tip).collect(Collectors.joining("\n"));
    }

    private String readmeSuggestions(RepositoryProject project, ProjectSignals signals) {
        List<String> sections = new ArrayList<>();
        sections.add("Project summary: Explain that RepoPilot AI turns a GitHub repository into architecture, quality, readiness, and interview guidance.");
        sections.add("Architecture: Include React frontend, Spring Boot API, PostgreSQL persistence, GitHub import, JWT security, and static analysis flow.");
        sections.add("Run locally: Document Docker Compose, backend Maven command, frontend npm command, and required environment variables.");
        sections.add("Demo workflow: Register, import " + project.getOwnerName() + "/" + project.getRepositoryName() + ", open analysis, re-analyze, compare history, and copy coach outputs.");
        if (!signals.tests()) sections.add("Testing: Add a note about planned backend service/API tests and frontend smoke tests.");
        if (!signals.ci()) sections.add("CI/CD: Add GitHub Actions setup once builds are automated.");
        return sections.stream().map(section -> "- " + section).collect(Collectors.joining("\n"));
    }

    private String projectTitleSuggestions(RepositoryProject project) {
        return List.of(
                "RepoPilot AI - GitHub Project Readiness Mentor",
                "RepoPilot AI - Intelligent Code Review and Architecture Coach",
                project.getRepositoryName() + " powered by RepoPilot AI",
                "AI-style Project Review Dashboard for Developers"
        ).stream().map(title -> "- " + title).collect(Collectors.joining("\n"));
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

    private String layerText(ProjectSignals signals) {
        List<String> layers = new ArrayList<>();
        if (signals.controllers()) layers.add("controllers");
        if (signals.services()) layers.add("services");
        if (signals.repositories()) layers.add("repositories");
        if (signals.entities()) layers.add("entities/models");
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

    private record ProjectSignals(
            boolean controllers,
            boolean services,
            boolean repositories,
            boolean entities,
            boolean security,
            boolean readme,
            boolean tests,
            boolean ci,
            boolean docker,
            boolean envExample,
            boolean demoEvidence,
            boolean deploymentNotes
    ) {
    }
}