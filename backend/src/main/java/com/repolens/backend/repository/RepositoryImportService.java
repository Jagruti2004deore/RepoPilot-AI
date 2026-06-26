package com.repolens.backend.repository;

import com.repolens.backend.analysis.Finding;
import com.repolens.backend.analysis.StaticAnalysisResult;
import com.repolens.backend.analysis.StaticAnalysisService;
import com.repolens.backend.ai.rag.RepositoryEmbeddingIndexer;
import com.repolens.backend.analysis.dto.AnalysisHistoryItem;
import com.repolens.backend.analysis.dto.AnalysisReport;
import com.repolens.backend.github.GitHubClient;
import com.repolens.backend.github.GitHubFile;
import com.repolens.backend.github.GitHubRepositorySnapshot;
import com.repolens.backend.repository.dto.RepositoryFileSummary;
import com.repolens.backend.repository.dto.RepositorySummary;
import com.repolens.backend.repository.file.RepositoryFile;
import com.repolens.backend.repository.file.RepositoryFileRepository;
import com.repolens.backend.user.UserAccount;
import com.repolens.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepositoryImportService {
    private final RepositoryProjectRepository repositoryProjectRepository;
    private final AnalysisRepository analysisRepository;
    private final RepositoryFileRepository repositoryFileRepository;
    private final UserRepository userRepository;
    private final GitHubClient gitHubClient;
    private final StaticAnalysisService staticAnalysisService;
    private final RepositoryEmbeddingIndexer repositoryEmbeddingIndexer;

    @Transactional
    public RepositorySummary importRepository(String githubUrl, String userEmail) {
        GitHubRepoRef ref = parseGitHubUrl(githubUrl);
        UserAccount owner = getUser(userEmail);

        GitHubRepositorySnapshot snapshot = gitHubClient.fetchRepository(ref.owner(), ref.name());

        RepositoryProject project = new RepositoryProject();
        project.setGithubUrl(ref.normalizedUrl());
        project.setOwnerName(ref.owner());
        project.setRepositoryName(ref.name());
        project.setDefaultBranch(snapshot.defaultBranch());
        project.setImportedAt(Instant.now());
        project.setOwner(owner);

        RepositoryProject savedProject = repositoryProjectRepository.save(project);
        List<RepositoryFile> savedFiles = snapshot.files().stream()
                .map(file -> toEntity(savedProject, file))
                .map(repositoryFileRepository::save)
                .toList();
        savedProject.setImportedFileCount(savedFiles.size());
        repositoryEmbeddingIndexer.indexRepository(savedProject, savedFiles);

        Analysis analysis = new Analysis();
        analysis.setRepository(savedProject);
        applyAnalysis(analysis, staticAnalysisService.analyze(savedProject, savedFiles));
        analysisRepository.save(analysis);

        return RepositorySummary.from(savedProject, analysis);
    }

    @Transactional(readOnly = true)
    public List<RepositorySummary> listRepositories(String userEmail) {
        UserAccount owner = getUser(userEmail);
        return repositoryProjectRepository.findByOwnerIdOrderByImportedAtDesc(owner.getId()).stream()
                .map(project -> analysisRepository.findTopByRepositoryIdOrderByCreatedAtDesc(project.getId())
                        .map(analysis -> RepositorySummary.from(project, analysis))
                        .orElseGet(() -> RepositorySummary.from(project)))
                .toList();
    }

    @Transactional(readOnly = true)
    public AnalysisReport getLatestAnalysis(Long repositoryId, String userEmail) {
        RepositoryProject project = getOwnedProject(repositoryId, userEmail);
        Analysis analysis = analysisRepository.findTopByRepositoryIdOrderByCreatedAtDesc(project.getId())
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found."));
        return AnalysisReport.from(analysis);
    }

    @Transactional(readOnly = true)
    public List<AnalysisHistoryItem> listAnalysisHistory(Long repositoryId, String userEmail) {
        RepositoryProject project = getOwnedProject(repositoryId, userEmail);
        return analysisRepository.findByRepositoryIdOrderByCreatedAtDesc(project.getId()).stream()
                .map(AnalysisHistoryItem::from)
                .toList();
    }
    @Transactional(readOnly = true)
    public List<RepositoryFileSummary> listRepositoryFiles(Long repositoryId, String userEmail) {
        RepositoryProject project = getOwnedProject(repositoryId, userEmail);
        return repositoryFileRepository.findByRepositoryIdOrderByPathAsc(project.getId()).stream()
                .map(RepositoryFileSummary::from)
                .toList();
    }

    @Transactional
    public AnalysisReport reanalyzeRepository(Long repositoryId, String userEmail) {
        RepositoryProject project = getOwnedProject(repositoryId, userEmail);
        List<RepositoryFile> files = repositoryFileRepository.findByRepositoryIdOrderByPathAsc(project.getId());
        project.setImportedFileCount(files.size());
        repositoryEmbeddingIndexer.indexRepository(project, files);

        Analysis analysis = new Analysis();
        analysis.setRepository(project);
        applyAnalysis(analysis, staticAnalysisService.analyze(project, files));
        Analysis saved = analysisRepository.save(analysis);
        return AnalysisReport.from(saved);
    }

    private UserAccount getUser(String userEmail) {
        return userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    private RepositoryProject getOwnedProject(Long repositoryId, String userEmail) {
        UserAccount owner = getUser(userEmail);
        RepositoryProject project = repositoryProjectRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found."));
        if (!project.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Repository does not belong to the current user.");
        }
        return project;
    }

    private RepositoryFile toEntity(RepositoryProject project, GitHubFile file) {
        RepositoryFile entity = new RepositoryFile();
        entity.setRepository(project);
        entity.setPath(file.path());
        entity.setLanguage(file.language());
        entity.setSizeBytes(file.sizeBytes());
        entity.setContent(file.content());
        return entity;
    }

    private void applyAnalysis(Analysis analysis, StaticAnalysisResult result) {
        analysis.setArchitectureScore(result.architectureScore());
        analysis.setSecurityScore(result.securityScore());
        analysis.setMaintainabilityScore(result.maintainabilityScore());
        analysis.setDocumentationScore(result.documentationScore());
        analysis.setTestingScore(result.testingScore());
        analysis.setOverallScore(result.overallScore());
        analysis.setResumeReadinessScore(result.resumeReadinessScore());
        analysis.setInterviewReadinessScore(result.interviewReadinessScore());
        analysis.setGithubQualityScore(result.githubQualityScore());
        analysis.setDeploymentReadinessScore(result.deploymentReadinessScore());
        analysis.setDemoReadinessScore(result.demoReadinessScore());
        analysis.setProjectReadinessScore(result.projectReadinessScore());
        analysis.setArchitectureReport(result.architectureReport());
        analysis.setCodeQualityReport(result.codeQualityReport());
        analysis.setSecurityReport(result.securityReport());
        analysis.setRecommendations(result.recommendations());
        analysis.setInterviewQuestions(result.interviewQuestions());
        analysis.setInterviewAnswers(result.interviewAnswers());
        analysis.setVivaQuestions(result.vivaQuestions());
        analysis.setPresentationScript(result.presentationScript());
        analysis.setArchitectureExplanation(result.architectureExplanation());
        analysis.setReadinessChecklist(result.readinessChecklist());
        analysis.setReadinessReport(result.readinessReport());
        analysis.setResumeSummary(result.resumeSummary());
        analysis.setFindingsText(result.findings().stream().map(this::formatFinding).collect(Collectors.joining("\n")));
        analysis.setStatus("ANALYZED");
        analysis.setCompletedAt(Instant.now());
    }

    private String formatFinding(Finding finding) {
        return "[" + finding.severity() + "] " + finding.category() + " - " + finding.filePath() + " - " + finding.title() + " - " + finding.recommendation();
    }

    private GitHubRepoRef parseGitHubUrl(String githubUrl) {
        URI uri = URI.create(githubUrl.trim());
        if (!"github.com".equalsIgnoreCase(uri.getHost())) {
            throw new IllegalArgumentException("Only github.com repository URLs are supported.");
        }

        String[] parts = uri.getPath().replaceFirst("^/", "").replaceFirst("\\.git$", "").split("/");
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("GitHub URL must look like https://github.com/owner/repository");
        }

        String normalizedUrl = "https://github.com/" + parts[0] + "/" + parts[1];
        return new GitHubRepoRef(parts[0], parts[1], normalizedUrl);
    }

    private record GitHubRepoRef(String owner, String name, String normalizedUrl) {
    }
}
