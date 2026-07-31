package com.cpf.batch.api;

import java.time.Instant;

/** CPF 업무 원장과 Spring Batch Metadata를 양방향으로 연결하는 식별 계약입니다. */
public record BatchExecutionLink(
        String cpfExecutionId,
        String jobId,
        long definitionVersion,
        Long jobInstanceId,
        Long jobExecutionId,
        Long stepExecutionId,
        String status,
        long fencingToken,
        Instant observedAt) {
    public BatchExecutionLink {
        if (cpfExecutionId == null || cpfExecutionId.isBlank()) throw new IllegalArgumentException("cpfExecutionId is required.");
        if (jobId == null || jobId.isBlank()) throw new IllegalArgumentException("jobId is required.");
        status = status == null || status.isBlank() ? "UNKNOWN" : status;
        observedAt = observedAt == null ? Instant.now() : observedAt;
    }
}
