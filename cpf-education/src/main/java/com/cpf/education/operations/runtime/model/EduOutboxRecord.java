package com.cpf.education.operations.runtime.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
/** EduOutboxRecord 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record EduOutboxRecord(String eventId, String operationId, String destination,
        String eventKey, Map<String,Object> payload, String state, int attemptCount,
        Instant nextAttemptAt, String claimedBy, long fencingToken, Instant createdAt,
        Instant updatedAt) implements Serializable { public EduOutboxRecord { payload = Map.copyOf(payload); } }
