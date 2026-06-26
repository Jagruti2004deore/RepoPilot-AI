package com.repolens.backend.ai.memory;

import com.repolens.backend.chat.ChatHistory;
import com.repolens.backend.chat.ChatHistoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryChatMemoryService {
    private final ChatHistoryRepository chatHistoryRepository;
    private final int maxMessages;
    private final int maxAnswerChars;

    public RepositoryChatMemoryService(
            ChatHistoryRepository chatHistoryRepository,
            @Value("${app.ai.memory.max-messages:6}") int maxMessages,
            @Value("${app.ai.memory.max-answer-chars:600}") int maxAnswerChars
    ) {
        this.chatHistoryRepository = chatHistoryRepository;
        this.maxMessages = Math.max(0, maxMessages);
        this.maxAnswerChars = Math.max(120, maxAnswerChars);
    }

    public String buildMemory(Long userId, Long repositoryId) {
        if (maxMessages == 0 || userId == null || repositoryId == null) {
            return "No previous conversation for this repository.";
        }

        List<ChatHistory> messages = chatHistoryRepository.findByUserIdAndRepositoryIdOrderByCreatedAtAsc(userId, repositoryId);
        if (messages.isEmpty()) {
            return "No previous conversation for this repository.";
        }

        int start = Math.max(0, messages.size() - maxMessages);
        StringBuilder memory = new StringBuilder("Recent repository conversation:\n");
        messages.subList(start, messages.size()).forEach(message -> memory
                .append("User: ").append(trim(message.getQuestion(), 300)).append("\n")
                .append("RepoPilot AI: ").append(trim(message.getAnswer(), maxAnswerChars)).append("\n---\n"));
        return memory.toString().trim();
    }

    private String trim(String value, int maxChars) {
        if (value == null || value.isBlank()) {
            return "Not available.";
        }
        String normalized = value.replace("\r\n", "\n").replace('\r', '\n').trim();
        return normalized.length() <= maxChars
                ? normalized
                : normalized.substring(0, maxChars) + "... [truncated]";
    }
}