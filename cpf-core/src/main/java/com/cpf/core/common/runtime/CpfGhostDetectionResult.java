package com.cpf.core.common.runtime;

import java.time.Instant;
import java.util.List;

/**
 * ghost runtime 탐지 결과입니다.
 */
public record CpfGhostDetectionResult(
        Instant detectedAt,
        List<CpfRuntimeGhostCandidate> candidates) {

    public CpfGhostDetectionResult {
        detectedAt = detectedAt == null ? Instant.now() : detectedAt;
        candidates = candidates == null ? List.of() : List.copyOf(candidates);
    }
}
