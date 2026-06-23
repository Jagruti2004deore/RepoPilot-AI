package com.repolens.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryProjectRepository extends JpaRepository<RepositoryProject, Long> {
    List<RepositoryProject> findByOwnerIdOrderByImportedAtDesc(Long ownerId);
}
