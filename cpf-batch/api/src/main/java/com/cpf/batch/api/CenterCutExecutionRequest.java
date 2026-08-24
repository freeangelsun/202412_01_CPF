package com.cpf.batch.api;

import java.util.Map;

/** Center-Cut 실행 생성 시 고정하는 파라미터/처리 정책 계약. */
public record CenterCutExecutionRequest(
        String centerCutJobId,
        String idempotencyKey,
        Map<String,Object> parameters,
        String parameterSchemaVersion,
        int tpsLimit,
        int concurrencyLimit,
        String requestedBy,
        String reason,
        String transactionId,
        String parentSegmentId
) {
    /** 개발자는 CPF transaction/segment를 직접 조립하지 않고 Runtime에 위임합니다. */
    public CenterCutExecutionRequest(
            String centerCutJobId,
            String idempotencyKey,
            Map<String,Object> parameters,
            String parameterSchemaVersion,
            int tpsLimit,
            int concurrencyLimit,
            String requestedBy,
            String reason) {
        this(centerCutJobId, idempotencyKey, parameters, parameterSchemaVersion,
                tpsLimit, concurrencyLimit, requestedBy, reason, null, null);
    }

    public CenterCutExecutionRequest {
        if (centerCutJobId == null || centerCutJobId.isBlank()) throw new IllegalArgumentException("centerCutJobId is required");
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey is required");
        if (requestedBy == null || requestedBy.isBlank()) throw new IllegalArgumentException("requestedBy is required");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
        if (tpsLimit < 0 || concurrencyLimit < 1) throw new IllegalArgumentException("invalid center-cut execution policy");
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        parameterSchemaVersion = parameterSchemaVersion == null || parameterSchemaVersion.isBlank() ? "1" : parameterSchemaVersion;
    }
}
