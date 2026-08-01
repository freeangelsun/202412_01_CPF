package com.cpf.reference.edu.counterparty.model;

import java.time.Instant;
import java.util.Map;

/** Durable REF-owned external-institution simulation state. */
public record ReferenceCounterpartyExchange(
        String requestId, String requirementId, String idempotencyKey, String requestHash,
        String businessKey, String familyCode, String scenarioCode, String state,
        int responseStatus, Map<String,Object> response, int attemptCount, String traceId,
        Instant createdAt, Instant updatedAt, Instant completedAt) { }
