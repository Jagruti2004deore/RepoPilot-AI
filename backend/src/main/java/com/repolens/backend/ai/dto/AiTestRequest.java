package com.repolens.backend.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiTestRequest(
        @NotBlank(message = "Prompt is required.")
        @Size(max = 1000, message = "Prompt must be 1000 characters or less.")
        String prompt
) {
}
