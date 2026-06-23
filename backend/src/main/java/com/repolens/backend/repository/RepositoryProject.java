package com.repolens.backend.repository;

import com.repolens.backend.user.UserAccount;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "repositories")
@Getter
@Setter
public class RepositoryProject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String githubUrl;
    private String repositoryName;
    private String ownerName;
    private String defaultBranch;
    private int importedFileCount;
    private Instant importedAt = Instant.now();

    @ManyToOne(fetch = FetchType.LAZY)
    private UserAccount owner;
}
