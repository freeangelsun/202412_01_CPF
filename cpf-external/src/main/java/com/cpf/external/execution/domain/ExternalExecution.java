package com.cpf.external.execution.domain;

import java.time.Instant;
import java.util.Map;

/** 대외 호출의 멱등 상태와 결과 불명 복구 연결 정보를 표현합니다. */
public record ExternalExecution(
        String executionId,
        String institutionCode,
        String endpointCode,
        String externalRequestId,
        String idempotencyKey,
        String requestHash,
        Status status,
        Map<String, Object> response,
        String unknownResultId,
        String failureCode,
        String failureMessage,
        Instant createdAt,
        Instant updatedAt) {

    public enum Status {
        REQUESTED,
        COMPLETED,
        FAILED,
        UNKNOWN_RESULT
    }
}
