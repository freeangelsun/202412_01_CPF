package com.cpf.education.operations.runtime.model;
import java.io.Serializable;
import java.time.Instant;
/** EduAuditRecord 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduAuditRecord(String auditId, String operationId, String requirementId,
        String action, String beforeState, String afterState, String actorId, String reason,
        String traceId, Instant createdAt) implements Serializable {}
