package com.cpf.core.api.async;
import java.time.Instant;
/** Async 실행건의 read-only 상태 View입니다. */
public record CpfAsyncOperationStatus(
        String executionId, String operationId, String transactionId, CpfAsyncState state,
        Instant submittedAt, Instant startedAt, Instant updatedAt, Instant completedAt,
        Instant expiresAt, Instant heartbeatAt, String leaseOwner, String cancellationReason, long version) { }
