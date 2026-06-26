package com.repolens.backend.ai;

import com.repolens.backend.ai.dto.AiTestRequest;
import com.repolens.backend.ai.dto.AiTestResponse;
import com.repolens.backend.ai.mcp.McpContextService;
import com.repolens.backend.ai.multimodal.MultimodalAiService;
import com.repolens.backend.ai.multimodal.MultimodalAnalysisRequest;
import com.repolens.backend.ai.multimodal.MultimodalAnalysisResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;
    private final AiModelHealthService aiModelHealthService;
    private final McpContextService mcpContextService;
    private final MultimodalAiService multimodalAiService;

    @GetMapping("/health")
    public AiTestResponse health() {
        return aiService.status();
    }

    @GetMapping("/model-health")
    public AiModelHealthService.AiModelHealthResponse modelHealth() {
        return aiModelHealthService.check();
    }

    @GetMapping("/mcp/status")
    public McpContextService.McpStatus mcpStatus() {
        return mcpContextService.status();
    }

    @PostMapping("/test")
    public AiTestResponse test(@Valid @RequestBody AiTestRequest request) {
        return aiService.test(request.prompt());
    }

    @PostMapping("/multimodal/analyze")
    public MultimodalAnalysisResponse analyzeImage(@Valid @RequestBody MultimodalAnalysisRequest request) {
        return multimodalAiService.analyze(request);
    }
}