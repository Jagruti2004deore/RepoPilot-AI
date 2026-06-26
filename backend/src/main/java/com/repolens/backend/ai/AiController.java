package com.repolens.backend.ai;

import com.repolens.backend.ai.dto.AiTestRequest;
import com.repolens.backend.ai.dto.AiTestResponse;
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

    @GetMapping("/health")
    public AiTestResponse health() {
        return aiService.status();
    }

    @PostMapping("/test")
    public AiTestResponse test(@Valid @RequestBody AiTestRequest request) {
        return aiService.test(request.prompt());
    }
}
