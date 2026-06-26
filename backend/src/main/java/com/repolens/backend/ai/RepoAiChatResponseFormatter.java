package com.repolens.backend.ai;

import com.repolens.backend.ai.dto.RepoAiChatEvidence;
import com.repolens.backend.ai.dto.RepoAiChatResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class RepoAiChatResponseFormatter {

    public Optional<String> format(RepoAiChatResponse response) {
        if (response == null || !hasText(response.directAnswer())) {
            return Optional.empty();
        }

        StringBuilder answer = new StringBuilder(response.directAnswer().trim());
        appendList(answer, "Key points", response.keyPoints());
        appendEvidence(answer, response.evidence());
        appendList(answer, "Recommended next steps", response.recommendedNextSteps());

        if (hasText(response.limitation())) {
            answer.append("\n\nLimitation: ").append(response.limitation().trim());
        }
        if (hasText(response.confidence())) {
            answer.append("\n\nConfidence: ").append(response.confidence().trim());
        }

        return Optional.of(answer.toString().trim());
    }

    private void appendList(StringBuilder answer, String heading, List<String> values) {
        List<String> cleanValues = safe(values).stream()
                .filter(this::hasText)
                .map(String::trim)
                .toList();
        if (cleanValues.isEmpty()) {
            return;
        }

        answer.append("\n\n").append(heading).append(":\n");
        cleanValues.forEach(value -> answer.append("- ").append(value).append("\n"));
    }

    private void appendEvidence(StringBuilder answer, List<RepoAiChatEvidence> evidence) {
        List<RepoAiChatEvidence> cleanEvidence = safe(evidence).stream()
                .filter(item -> hasText(item.filePath()) || hasText(item.reason()) || hasText(item.excerpt()))
                .toList();
        if (cleanEvidence.isEmpty()) {
            return;
        }

        answer.append("\n\nSource evidence:\n");
        cleanEvidence.forEach(item -> {
            if (hasText(item.filePath())) {
                answer.append("- ").append(item.filePath().trim());
            } else {
                answer.append("- Repository context");
            }
            if (hasText(item.reason())) {
                answer.append(": ").append(item.reason().trim());
            }
            if (hasText(item.excerpt())) {
                answer.append("\n  Evidence: ").append(item.excerpt().trim());
            }
            answer.append("\n");
        });
    }

    private <T> List<T> safe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}