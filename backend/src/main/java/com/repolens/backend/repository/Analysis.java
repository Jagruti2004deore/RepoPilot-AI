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

    @Column(nullable = true)
    private double resumeReadinessScore;

    @Column(nullable = true)
    private double interviewReadinessScore;

    @Column(nullable = true)
    private double githubQualityScore;

    @Column(nullable = true)
    private double deploymentReadinessScore;

    @Column(nullable = true)
    private double demoReadinessScore;

    @Column(nullable = true)
    private double projectReadinessScore;

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

    @Column(columnDefinition = "TEXT")
    private String interviewAnswers = "";

    @Column(columnDefinition = "TEXT")
    private String vivaQuestions = "";

    @Column(columnDefinition = "TEXT")
    private String presentationScript = "";

    @Column(columnDefinition = "TEXT")
    private String architectureExplanation = "";

    @Column(columnDefinition = "TEXT")
    private String readinessChecklist = "";

    @Column(columnDefinition = "TEXT")
    private String readinessReport = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumeSummary = "";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String findingsText = "";
}