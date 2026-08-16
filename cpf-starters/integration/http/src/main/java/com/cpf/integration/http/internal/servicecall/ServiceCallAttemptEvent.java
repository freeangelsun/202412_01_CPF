package com.cpf.integration.http.internal.servicecall;

import java.time.Instant;

/** Core internal 서비스 호출 단일 시도 Event입니다. */
record ServiceCallAttemptEvent(
        int attemptNo,
        ServiceCallResolvedTarget target,
        boolean failover,
        String status,
        Integer httpStatus,
        long durationMillis,
        String failureCode,
        String failureMessage,
        boolean unknownResult,
        Instant startedAt,
        Instant finishedAt) {
}
