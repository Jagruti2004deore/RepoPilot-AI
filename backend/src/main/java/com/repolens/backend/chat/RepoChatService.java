package com.repolens.backend.chat;

import com.repolens.backend.ai.RepoAiChatService;
import com.repolens.backend.ai.memory.RepositoryChatMemoryService;

import com.repolens.backend.chat.dto.RepoChatMessage;
import com.repolens.backend.repository.Analysis;
import com.repolens.backend.repository.AnalysisRepository;
import com.repolens.backend.repository.RepositoryProject;
import com.repolens.backend.repository.RepositoryProjectRepository;
import com.repolens.backend.repository.file.RepositoryFile;
import com.repolens.backend.repository.file.RepositoryFileRepository;
import com.repolens.backend.user.UserAccount;
import com.repolens.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RepoChatService {
    private final ChatHistoryRepository chatHistoryRepository;
    private final UserRepository userRepository;
    private final RepositoryProjectRepository repositoryProjectRepository;
    private final RepositoryFileRepository repositoryFileRepository;
    private final AnalysisRepository analysisRepository;
    private final RepoAiChatService repoAiChatService;
    private final RepositoryChatMemoryService repositoryChatMemoryService;

    @Transactional(readOnly = true)
    public List<RepoChatMessage> listMessages(Long repositoryId, String userEmail) {
        OwnedRepository owned = getOwnedRepository(repositoryId, userEmail);
        return chatHistoryRepository.findByUserIdAndRepositoryIdOrderByCreatedAtAsc(owned.user().getId(), owned.repository().getId()).stream()
                .map(RepoChatMessage::from)
                .toList();
    }

    @Transactional
    public RepoChatMessage askQuestion(Long repositoryId, String userEmail, String question) {
        OwnedRepository owned = getOwnedRepository(repositoryId, userEmail);
        List<RepositoryFile> files = repositoryFileRepository.findByRepositoryIdOrderByPathAsc(owned.repository().getId());
        Analysis analysis = analysisRepository.findTopByRepositoryIdOrderByCreatedAtDesc(owned.repository().getId()).orElse(null);

        String memoryContext = repositoryChatMemoryService.buildMemory(owned.user().getId(), owned.repository().getId());
        String answer = repoAiChatService.answerQuestion(owned.repository(), owned.user().getId(), files, analysis, question, memoryContext)
                .orElseGet(() -> answerQuestion(owned.repository(), files, analysis, question));

        ChatHistory history = new ChatHistory();
        history.setUser(owned.user());
        history.setRepository(owned.repository());
        history.setQuestion(question.trim());
        history.setAnswer(answer);
        history.setCreatedAt(Instant.now());
        return RepoChatMessage.from(chatHistoryRepository.save(history));
    }

    private OwnedRepository getOwnedRepository(Long repositoryId, String userEmail) {
        UserAccount user = userRepository.findByEmailIgnoreCase(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        RepositoryProject repository = repositoryProjectRepository.findById(repositoryId)
                .orElseThrow(() -> new IllegalArgumentException("Repository not found."));
        if (!repository.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Repository does not belong to the current user.");
        }
        return new OwnedRepository(user, repository);
    }

    private String answerQuestion(RepositoryProject repository, List<RepositoryFile> files, Analysis analysis, String question) {
        List<RepositoryFile> matches = topMatches(files, question);
        StringBuilder answer = new StringBuilder();
        answer.append("Based on ").append(repository.getOwnerName()).append("/").append(repository.getRepositoryName()).append(":\n\n");
        answer.append(intentAnswer(question, analysis, matches));

        if (!matches.isEmpty()) {
            answer.append("\n\nMost relevant files:\n");
            matches.forEach(file -> answer.append("- ").append(file.getPath()).append(" (").append(file.getLanguage()).append(")").append("\n"));
            answer.append("\nCode evidence:\n");
            matches.stream().limit(3).forEach(file -> answer.append("- ").append(file.getPath()).append(": ").append(snippet(file.getContent(), question)).append("\n"));
        }

        if (analysis != null) {
            answer.append("\nCurrent review signal: overall ").append(Math.round(analysis.getOverallScore()))
                    .append("/100, readiness ").append(Math.round(analysis.getProjectReadinessScore()))
                    .append("/100. ");
            if (analysis.getRecommendations() != null && !analysis.getRecommendations().isBlank()) {
                answer.append("Top recommendation: ").append(firstLine(analysis.getRecommendations())).append("\n");
            }
        }

        answer.append("\nUse this as project guidance, then open the matched files to verify exact implementation details.");
        return answer.toString();
    }

    private String intentAnswer(String question, Analysis analysis, List<RepositoryFile> matches) {
        String lower = question.toLowerCase(Locale.ROOT);
        if (lower.contains("architecture") || lower.contains("flow") || lower.contains("design")) {
            if (analysis != null && analysis.getArchitectureExplanation() != null && !analysis.getArchitectureExplanation().isBlank()) {
                return "Architecture answer:\n" + firstLines(analysis.getArchitectureExplanation(), 6);
            }
            return "Architecture answer:\nThis project is best understood from the selected API/service/repository files below. Follow the request from controller/API entry points into service logic, persistence, and response DTOs.";
        }
        if (lower.contains("security") || lower.contains("auth") || lower.contains("jwt")) {
            if (analysis != null && analysis.getSecurityReport() != null && !analysis.getSecurityReport().isBlank()) {
                return "Security answer:\n" + firstLines(analysis.getSecurityReport(), 5);
            }
            return "Security answer:\nCheck authentication filters, security configuration, request validation, secret handling, and CORS settings in the matched files.";
        }
        if (lower.contains("improve") || lower.contains("issue") || lower.contains("problem") || lower.contains("fix")) {
            if (analysis != null && analysis.getRecommendations() != null && !analysis.getRecommendations().isBlank()) {
                return "Improvement answer:\n" + firstLines(analysis.getRecommendations(), 6);
            }
            return "Improvement answer:\nStart with tests, validation, error handling, security boundaries, and documentation for the matched files.";
        }
        if (lower.contains("explain") || lower.contains("what") || lower.contains("how")) {
            return "Explanation answer:\nThe relevant implementation appears in the matched files below. Read them in order and connect each file to its role: entry point, business logic, persistence, UI, or configuration.";
        }
        return "Repo answer:\nI matched your question against imported paths, source content, and the latest analysis. The files below are the best starting points for this question.";
    }

    private List<RepositoryFile> topMatches(List<RepositoryFile> files, String question) {
        Set<String> terms = Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(term -> term.length() > 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return files.stream()
                .sorted(Comparator.comparingInt((RepositoryFile file) -> score(file, terms)).reversed()
                        .thenComparing(RepositoryFile::getPath))
                .filter(file -> score(file, terms) > 0)
                .limit(5)
                .toList();
    }

    private int score(RepositoryFile file, Set<String> terms) {
        String haystack = (file.getPath() + "\n" + file.getLanguage() + "\n" + file.getContent()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            if (file.getPath().toLowerCase(Locale.ROOT).contains(term)) score += 6;
            if (haystack.contains(term)) score += 2;
        }
        String path = file.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("controller")) score += 2;
        if (path.contains("service")) score += 2;
        if (path.contains("security")) score += 2;
        return score;
    }

    private String snippet(String content, String question) {
        if (content == null || content.isBlank()) return "No content stored for this file.";
        List<String> terms = Arrays.stream(question.toLowerCase(Locale.ROOT).split("[^a-z0-9]+"))
                .filter(term -> term.length() > 2)
                .toList();
        List<String> lines = content.lines().toList();
        int index = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).toLowerCase(Locale.ROOT);
            if (terms.stream().anyMatch(line::contains)) {
                index = i;
                break;
            }
        }
        int start = Math.max(0, index - 1);
        int end = Math.min(lines.size(), index + 3);
        return lines.subList(start, end).stream()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(3)
                .collect(Collectors.joining(" | "));
    }

    private String firstLine(String text) {
        return Arrays.stream(text.split("\\R"))
                .filter(line -> !line.isBlank())
                .findFirst()
                .orElse(text);
    }

    private String firstLines(String text, int maxLines) {
        return Arrays.stream(text.split("\\R"))
                .filter(line -> !line.isBlank())
                .limit(maxLines)
                .collect(Collectors.joining("\n"));
    }

    private record OwnedRepository(UserAccount user, RepositoryProject repository) {
    }
}
