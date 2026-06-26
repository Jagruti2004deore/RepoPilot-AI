package com.repolens.backend.chat;

import com.repolens.backend.chat.dto.RepoChatMessage;
import com.repolens.backend.chat.dto.RepoQuestionRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/repositories/{repositoryId}/chat")
@RequiredArgsConstructor
public class RepoChatController {
    private final RepoChatService repoChatService;

    @GetMapping
    public List<RepoChatMessage> listMessages(@PathVariable Long repositoryId, Principal principal) {
        return repoChatService.listMessages(repositoryId, principal.getName());
    }

    @PostMapping("/ask")
    public RepoChatMessage askQuestion(@PathVariable Long repositoryId, @Valid @RequestBody RepoQuestionRequest request, Principal principal) {
        return repoChatService.askQuestion(repositoryId, principal.getName(), request.question());
    }
}