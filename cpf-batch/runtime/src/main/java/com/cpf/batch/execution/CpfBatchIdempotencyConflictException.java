package com.cpf.batch.execution;

/** 동일 idempotency key가 다른 승인/Plan/Parameter 범위로 재사용됐을 때의 명시적 충돌입니다. */
public final class CpfBatchIdempotencyConflictException extends CpfBatchExecutionException {
    public CpfBatchIdempotencyConflictException(String message) {
        super("BATCH_IDEMPOTENCY_CONFLICT", message);
    }
}
