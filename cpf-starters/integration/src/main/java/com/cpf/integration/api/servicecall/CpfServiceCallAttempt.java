package com.cpf.integration.api.servicecall;

import java.time.Instant;

/** Retry/Failover를 포함한 서비스 호출 단일 시도의 공개 원장 Event입니다. */
public record CpfServiceCallAttempt(
        int attemptNo,
        CpfServiceCallTarget target,
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
