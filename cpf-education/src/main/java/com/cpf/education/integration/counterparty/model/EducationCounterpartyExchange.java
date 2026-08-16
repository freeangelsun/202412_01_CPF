package com.cpf.education.integration.counterparty.model;
import java.time.Instant;
import java.util.Map;

/** Durable EDU-owned external-institution simulation state. */
/** EducationCounterpartyExchange 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EducationCounterpartyExchange(
        String requestId, String requirementId, String idempotencyKey, String requestHash,
        String businessKey, String familyCode, String scenarioCode, String state,
        int responseStatus, Map<String,Object> response, int attemptCount, String traceId,
        Instant createdAt, Instant updatedAt, Instant completedAt) { }
