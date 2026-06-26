package com.repolens.backend.ai.multimodal;

import com.repolens.backend.ai.security.AiAuditService;
import com.repolens.backend.ai.security.AiSecretRedactor;
import com.repolens.backend.ai.security.AiTimeoutExecutor;
import com.repolens.backend.ai.security.PromptInjectionGuard;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

@Service
public class MultimodalAiService {
    private final ChatModel chatModel;
    private final PromptInjectionGuard injectionGuard;
    private final AiSecretRedactor redactor;
    private final AiTimeoutExecutor timeoutExecutor;
    private final AiAuditService auditService;
    private final boolean enabled;
    private final String model;

    public MultimodalAiService(
            ChatModel chatModel,
            PromptInjectionGuard injectionGuard,
            AiSecretRedactor redactor,
            AiTimeoutExecutor timeoutExecutor,
            AiAuditService auditService,
            @Value("${app.ai.multimodal.enabled:true}") boolean enabled,
            @Value("${app.ai.multimodal.model:llava}") String model
    ) {
        this.chatModel = chatModel;
        this.injectionGuard = injectionGuard;
        this.redactor = redactor;
        this.timeoutExecutor = timeoutExecutor;
        this.auditService = auditService;
        this.enabled = enabled;
        this.model = model;
    }

    public MultimodalAnalysisResponse analyze(MultimodalAnalysisRequest request) {
        if (!enabled) {
            return new MultimodalAnalysisResponse(false, model, "Multimodal AI is disabled.", Instant.now());
        }
        if (!injectionGuard.isAllowed(request.prompt())) {
            auditService.record(null, null, "MULTIMODAL_GUARD", "BLOCKED", model, request.prompt());
            return new MultimodalAnalysisResponse(true, model, injectionGuard.rejectionMessage(), Instant.now());
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(stripDataUriPrefix(request.imageBase64()));
            Media media = new Media(MimeTypeUtils.parseMimeType(contentType(request.contentType())), new ByteArrayResource(imageBytes));
            UserMessage userMessage = UserMessage.builder()
                    .text(redactor.redact(request.prompt()))
                    .media(media)
                    .build();
            Prompt prompt = new Prompt(List.of(
                    new SystemMessage("You are RepoPilot AI visual reviewer. Explain images concisely and never infer secrets."),
                    userMessage
            ), OllamaOptions.builder().model(model).temperature(0.2).build());

            String response = timeoutExecutor.execute(() -> chatModel.call(prompt).getResult().getOutput().getText());
            auditService.record(null, null, "MULTIMODAL_ANALYSIS", "SUCCESS", model, request.prompt());
            return new MultimodalAnalysisResponse(true, model, response, Instant.now());
        } catch (RuntimeException ex) {
            auditService.record(null, null, "MULTIMODAL_ANALYSIS", "FAILED", model, ex.getMessage());
            return new MultimodalAnalysisResponse(true, model, "Multimodal analysis failed. Check that Ollama is running and the llava model is installed.", Instant.now());
        }
    }

    private String stripDataUriPrefix(String imageBase64) {
        int comma = imageBase64.indexOf(',');
        return comma >= 0 ? imageBase64.substring(comma + 1) : imageBase64;
    }

    private String contentType(String contentType) {
        return contentType == null || contentType.isBlank() ? "image/png" : contentType;
    }
}