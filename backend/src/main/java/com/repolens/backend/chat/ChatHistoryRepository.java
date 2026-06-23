package com.repolens.backend.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {
    List<ChatHistory> findByUserIdAndRepositoryIdOrderByCreatedAtAsc(Long userId, Long repositoryId);
}
