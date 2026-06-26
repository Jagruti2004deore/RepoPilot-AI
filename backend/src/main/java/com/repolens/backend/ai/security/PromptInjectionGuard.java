package com.repolens.backend.ai.security;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class PromptInjectionGuard {
    private static final List<String> BLOCKED_PATTERNS = List.of(
            "ignore previous instructions",
            "ignore all previous instructions",
            "reveal your system prompt",
            "show your system prompt",
            "print your hidden instructions",
            "developer message",
            "system message",
            "bypass security",
            "disable safety",
            "exfiltrate",
            "leak secrets",
            "show jwt secret",
            "show api key"
    );

    public boolean isAllowed(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return false;
        }
        String normalized = prompt.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        return BLOCKED_PATTERNS.stream().noneMatch(normalized::contains);
    }

    public String rejectionMessage() {
        return "I cannot process that request because it appears to ask me to bypass instructions, reveal hidden prompts, or expose secrets. Ask a repository-specific code, architecture, security, or improvement question instead.";
    }
}