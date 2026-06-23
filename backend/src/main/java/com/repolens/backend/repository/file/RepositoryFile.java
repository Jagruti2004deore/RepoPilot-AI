package com.repolens.backend.repository.file;

import com.repolens.backend.repository.RepositoryProject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "repository_files")
@Getter
@Setter
public class RepositoryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private RepositoryProject repository;

    @Column(nullable = false, length = 700)
    private String path;

    private String language;
    private long sizeBytes;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
}

