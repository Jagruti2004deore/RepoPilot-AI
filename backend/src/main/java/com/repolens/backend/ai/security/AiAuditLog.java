package com.repolens.backend.ai.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ai_audit_logs")
@Getter
@Setter
public class AiAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long repositoryId;

    @Column(nullable = false, length = 80)
    private String eventType;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(length = 120)
    private String model;

    @Column(columnDefinition = "TEXT")
    private String detail;

    private Instant createdAt = Instant.now();
}