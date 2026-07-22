package com.cpf.core.common.idempotency;

import java.time.Duration;

/**
 * HTTP, Broker, Batch, FileTransfer가 동일하게 사용하는 멱등 실행 명령입니다.
 */
public record CpfIdempotencyCommand(
        String scope,
        String idempotencyKey,
        String requestHash,
        String payloadHash,
        String transactionGlobalId,
        String segmentId,
        Duration ttl) {

    public CpfIdempotencyCommand {
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("scope는 필수입니다.");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("idempotencyKey는 필수입니다.");
        }
        if (requestHash == null || requestHash.isBlank()) {
            throw new IllegalArgumentException("requestHash는 필수입니다.");
        }
        if (payloadHash == null || payloadHash.isBlank()) {
            throw new IllegalArgumentException("payloadHash는 필수입니다.");
        }
        ttl = ttl == null || ttl.isNegative() || ttl.isZero() ? Duration.ofMinutes(5) : ttl;
    }
}
