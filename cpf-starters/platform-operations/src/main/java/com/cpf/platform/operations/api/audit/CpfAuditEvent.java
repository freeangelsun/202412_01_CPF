package com.cpf.platform.operations.api.audit;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** 변경/위험 조치의 불변 감사 이벤트입니다. 민감 payload 원문을 보관하지 않습니다. */
public record CpfAuditEvent(
        String eventId,
        String action,
        Phase phase,
        String transactionId,
        String executionId,
        String subjectId,
        String actorId,
        String reason,
        String outcome,
        String errorType,
        String safeResultSummary,
        Instant occurredAt,
        Map<String,String> attributes) {
    /** Phase 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Phase { STARTED, COMPLETED, FAILED }
    public CpfAuditEvent {
        eventId = required(eventId, "eventId");
        action = required(action, "action");
        Objects.requireNonNull(phase, "phase");
        transactionId = required(transactionId, "transactionId");
        executionId = required(executionId, "executionId");
        reason = normalize(reason);
        outcome = normalize(outcome);
        errorType = normalize(errorType);
        safeResultSummary = normalize(safeResultSummary);
        Objects.requireNonNull(occurredAt, "occurredAt");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
    private static String required(String v,String f){v=normalize(v);if(v==null)throw new IllegalArgumentException(f+" is required");return v;}
    private static String normalize(String v){if(v==null)return null;v=v.trim();return v.isEmpty()?null:v;}
}
