package com.cpf.reference.edu.runtime.model;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
public record EduOutboxRecord(String eventId, String operationId, String destination,
        String eventKey, Map<String,Object> payload, String state, int attemptCount,
        Instant nextAttemptAt, String claimedBy, long fencingToken, Instant createdAt,
        Instant updatedAt) implements Serializable { public EduOutboxRecord { payload = Map.copyOf(payload); } }
