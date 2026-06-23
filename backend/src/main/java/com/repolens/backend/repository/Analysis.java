package com.repolens.backend.repository;

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

import java.time.Instant;

@Entity
@Table(name = "analyses")
@Getter
@Setter
public class Analysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private RepositoryProject repository;

    private double architectureScore;
    private double securityScore;
    private double maintainabilityScore;
    private double documentationScore;
    private double testingScore;
    private double overallScore;
    private String status = "PENDING";
    private Instant createdAt = Instant.now();
    private Instant completedAt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String architectureReport = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String codeQualityReport = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String securityReport = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendations = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String interviewQuestions = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumeSummary = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String findingsText = "";
}

