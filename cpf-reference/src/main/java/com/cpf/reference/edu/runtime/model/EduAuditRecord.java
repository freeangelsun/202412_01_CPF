package com.cpf.reference.edu.runtime.model;
import java.io.Serializable;
import java.time.Instant;
public record EduAuditRecord(String auditId, String operationId, String requirementId,
        String action, String beforeState, String afterState, String actorId, String reason,
        String traceId, Instant createdAt) implements Serializable {}
