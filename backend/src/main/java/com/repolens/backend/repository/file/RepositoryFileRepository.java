package com.repolens.backend.repository.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, Long> {
    List<RepositoryFile> findByRepositoryIdOrderByPathAsc(Long repositoryId);

    long countByRepositoryId(Long repositoryId);
}
