package com.repolens.backend.chat.dto;

import com.repolens.backend.chat.ChatHistory;

import java.time.Instant;

public record RepoChatMessage(
        Long id,
        String question,
        String answer,
        Instant createdAt
) {
    public static RepoChatMessage from(ChatHistory history) {
        return new RepoChatMessage(
                history.getId(),
                history.getQuestion(),
                history.getAnswer(),
                history.getCreatedAt()
        );
    }
}