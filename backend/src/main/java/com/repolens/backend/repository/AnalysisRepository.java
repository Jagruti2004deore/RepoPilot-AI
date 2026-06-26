package com.repolens.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {
    List<Analysis> findByRepositoryOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<Analysis> findByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);

    Optional<Analysis> findTopByRepositoryIdOrderByCreatedAtDesc(Long repositoryId);
}