package com.repolens.backend.ai.multimodal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MultimodalAnalysisRequest(
        @NotBlank(message = "Prompt is required.")
        @Size(max = 1000, message = "Prompt must be 1000 characters or less.")
        String prompt,

        @NotBlank(message = "Image data is required.")
        String imageBase64,

        @Size(max = 80, message = "Content type must be 80 characters or less.")
        String contentType
) {
}