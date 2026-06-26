package com.repolens.backend.ai.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AiAuditService {
    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);

    private final AiAuditLogRepository repository;
    private final AiSecretRedactor redactor;
    private final boolean enabled;

    public AiAuditService(
            AiAuditLogRepository repository,
            AiSecretRedactor redactor,
            @Value("${app.ai.security.audit-enabled:true}") boolean enabled
    ) {
        this.repository = repository;
        this.redactor = redactor;
        this.enabled = enabled;
    }

    public void record(Long userId, Long repositoryId, String eventType, String status, String model, String detail) {
        String safeDetail = redactor.redact(detail);
        log.info("AI audit event={} status={} userId={} repositoryId={} model={} detail={}", eventType, status, userId, repositoryId, model, safeDetail);
        if (!enabled) {
            return;
        }
        try {
            AiAuditLog entry = new AiAuditLog();
            entry.setUserId(userId);
            entry.setRepositoryId(repositoryId);
            entry.setEventType(eventType);
            entry.setStatus(status);
            entry.setModel(model);
            entry.setDetail(safeDetail);
            repository.save(entry);
        } catch (RuntimeException ex) {
            log.debug("AI audit persistence failed.", ex);
        }
    }
}