package com.repolens.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RepoQuestionRequest(
        @NotBlank(message = "Question is required.")
        @Size(max = 1000, message = "Question must be 1000 characters or less.")
        String question
) {
}