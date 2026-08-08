package com.cpf.core.api.transaction;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** XA in-doubt/heuristic 복구 조회를 위한 불변 Snapshot입니다. */
public record CpfXaRecoveryRecord(
        String transactionId,
        CpfTransactionOutcome outcome,
        List<String> resourceIds,
        long fencingToken,
        Instant updatedAt,
        String detail) {
    public CpfXaRecoveryRecord {
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId must not be blank");
        Objects.requireNonNull(outcome, "outcome");
        resourceIds = resourceIds == null ? List.of() : List.copyOf(resourceIds);
        if (fencingToken < 0) throw new IllegalArgumentException("fencingToken must not be negative");
        Objects.requireNonNull(updatedAt, "updatedAt");
        detail = detail == null ? "" : detail;
    }
}
