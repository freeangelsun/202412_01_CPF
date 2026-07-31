package com.cpf.batch.api;

import java.time.Instant;

/** 멱등성·대사에 필요한 CPF 실행 예약의 불변 Snapshot입니다. */
public record BatchExecutionReservation(
        String cpfExecutionId,
        String jobId,
        long definitionVersion,
        String approvalId,
        String idempotencyScope,
        String idempotencyKey,
        String requestHash,
        String planChecksum,
        long fencingToken,
        BatchControlState state,
        Long jobInstanceId,
        Long jobExecutionId,
        int reconcileAttempts,
        Instant reconcileAfter,
        Instant updatedAt) {
    public BatchExecutionReservation {
        if (cpfExecutionId == null || cpfExecutionId.isBlank()) throw new IllegalArgumentException("cpfExecutionId is required");
        if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId is required");
        if (definitionVersion <= 0) throw new IllegalArgumentException("definitionVersion must be positive");
        if (approvalId == null || approvalId.isBlank()) throw new IllegalArgumentException("approvalId is required");
        if (idempotencyScope == null || idempotencyScope.isBlank()) throw new IllegalArgumentException("idempotencyScope is required");
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey is required");
        if (requestHash == null || !requestHash.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("requestHash must be SHA-256");
        if (planChecksum == null || !planChecksum.matches("[0-9a-f]{64}")) throw new IllegalArgumentException("planChecksum must be SHA-256");
        if (fencingToken <= 0) throw new IllegalArgumentException("fencingToken must be positive");
        if (state == null) throw new IllegalArgumentException("state is required");
        if (reconcileAttempts < 0) throw new IllegalArgumentException("reconcileAttempts must not be negative");
        if (updatedAt == null) throw new IllegalArgumentException("updatedAt is required");
    }
}
