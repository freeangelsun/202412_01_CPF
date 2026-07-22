package com.cpf.core.common.runtime;

import java.time.Duration;

/**
 * 분산 lock 획득 요청입니다.
 */
public record CpfLockAcquireRequest(
        String lockKey,
        String ownerId,
        Duration ttl) {

    public CpfLockAcquireRequest {
        if (lockKey == null || lockKey.isBlank()) {
            throw new IllegalArgumentException("lockKey는 필수입니다.");
        }
        ttl = ttl == null ? Duration.ofSeconds(30) : ttl;
    }
}
