package com.repolens.backend.ai.security;

import org.springframework.stereotype.Component;

@Component
public class AiSecretRedactor {
    public String redact(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replaceAll("(?i)(password|passwd|pwd)\\s*[:=]\\s*[^\\s\\n]+", "$1=[REDACTED]")
                .replaceAll("(?i)(secret|token|api[_-]?key|access[_-]?key)\\s*[:=]\\s*[^\\s\\n]+", "$1=[REDACTED]")
                .replaceAll("ghp_[A-Za-z0-9_]{20,}", "[REDACTED_GITHUB_TOKEN]")
                .replaceAll("sk-[A-Za-z0-9]{20,}", "[REDACTED_API_KEY]")
                .trim();
    }
}