package com.repolens.backend.repository;

import com.repolens.backend.analysis.dto.AnalysisHistoryItem;
import com.repolens.backend.analysis.dto.AnalysisReport;
import com.repolens.backend.repository.dto.ImportRepositoryRequest;
import com.repolens.backend.repository.dto.RepositoryFileSummary;
import com.repolens.backend.repository.dto.RepositorySummary;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/repositories")
@RequiredArgsConstructor
public class RepositoryController {
    private final RepositoryImportService repositoryImportService;

    @PostMapping("/import")
    public RepositorySummary importRepository(@Valid @RequestBody ImportRepositoryRequest request, Principal principal) {
        return repositoryImportService.importRepository(request.githubUrl(), principal.getName());
    }

    @GetMapping
    public List<RepositorySummary> listRepositories(Principal principal) {
        return repositoryImportService.listRepositories(principal.getName());
    }

    @GetMapping("/{repositoryId}/analysis")
    public AnalysisReport getLatestAnalysis(@PathVariable Long repositoryId, Principal principal) {
        return repositoryImportService.getLatestAnalysis(repositoryId, principal.getName());
    }

    @GetMapping("/{repositoryId}/analyses")
    public List<AnalysisHistoryItem> listAnalysisHistory(@PathVariable Long repositoryId, Principal principal) {
        return repositoryImportService.listAnalysisHistory(repositoryId, principal.getName());
    }

    @GetMapping("/{repositoryId}/files")
    public List<RepositoryFileSummary> listRepositoryFiles(@PathVariable Long repositoryId, Principal principal) {
        return repositoryImportService.listRepositoryFiles(repositoryId, principal.getName());
    }

    @PostMapping("/{repositoryId}/reanalyze")
    public AnalysisReport reanalyzeRepository(@PathVariable Long repositoryId, Principal principal) {
        return repositoryImportService.reanalyzeRepository(repositoryId, principal.getName());
    }
}
