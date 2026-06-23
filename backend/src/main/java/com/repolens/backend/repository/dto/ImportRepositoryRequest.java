package com.repolens.backend.repository.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record ImportRepositoryRequest(@URL @NotBlank String githubUrl) {
}
