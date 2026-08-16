package com.cpf.batch.control.deploy;

/** 동일 Idempotency scope/key에 다른 승인·Manifest가 재사용됐음을 나타냅니다. */
public final class DeploymentIdempotencyConflictException extends RuntimeException {
    public DeploymentIdempotencyConflictException(String message) { super(message); }
}
