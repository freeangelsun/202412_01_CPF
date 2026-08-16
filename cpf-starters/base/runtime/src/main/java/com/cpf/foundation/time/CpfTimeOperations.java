package com.cpf.foundation.time;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * CPF Base가 제공하는 wall-clock/monotonic-clock 공통 계약입니다.
 * Core Context와 분리하여 Runtime 교체 가능성을 Base Capability가 소유합니다.
 */
public interface CpfTimeOperations {
    Instant now();
    long monotonicNanos();

    default CpfDeadline deadline(Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("ttl must be positive");
        }
        return new CpfDeadline(Math.addExact(monotonicNanos(), ttl.toNanos()));
    }

    CpfTimeSnapshot snapshot(ZoneId zone, Duration maximumAllowedSkew);
}
