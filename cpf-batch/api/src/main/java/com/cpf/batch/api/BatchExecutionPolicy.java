package com.cpf.batch.api;

import java.time.Duration;

/**
 * Job Pack이 선언하는 공통 실행 정책입니다. Runtime은 이 값을 운영 Preview/검증에 사용하고,
 * Spring Batch 세부 retry/skip 정책은 실제 Job Step 정의와 함께 일치해야 합니다.
 */
public record BatchExecutionPolicy(
        Duration timeout,
        int maxRetries,
        int skipLimit,
        int errorThreshold,
        boolean rollbackOnFailure,
        boolean checkpointRequired,
        boolean pauseSupported
) {
    public BatchExecutionPolicy {
        timeout = timeout == null ? Duration.ofHours(1) : timeout;
        if (timeout.isNegative() || timeout.isZero()) throw new IllegalArgumentException("timeout must be positive");
        if (maxRetries < 0 || skipLimit < 0 || errorThreshold < 0) throw new IllegalArgumentException("policy counters must be >= 0");
    }
    public static BatchExecutionPolicy defaults() {
        return new BatchExecutionPolicy(Duration.ofHours(1), 0, 0, 1, true, true, true);
    }
}
