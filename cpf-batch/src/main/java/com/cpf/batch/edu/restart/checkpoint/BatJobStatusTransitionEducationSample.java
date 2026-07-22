package com.cpf.batch.edu.restart.checkpoint;

import java.util.Set;

/**
 * Job 상태 전이 정책을 코드로 고정하는 샘플입니다.
 */
public class BatJobStatusTransitionEducationSample {
    private static final Set<String> RETRYABLE = Set.of("FAILED", "STOPPED", "UNKNOWN");

    public boolean canRetry(String status) {
        return RETRYABLE.contains(status);
    }

    public String nextStatus(boolean success) {
        return success ? "COMPLETED" : "FAILED";
    }
}
