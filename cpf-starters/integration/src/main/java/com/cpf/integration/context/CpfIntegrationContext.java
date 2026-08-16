package com.cpf.integration.context;

import java.time.Instant;

/**
 * 외부/타 시스템 연계 호출에서 Integration Owner가 유지하는 전문 Context입니다.
 *
 * <p>Partner/System, 논리 Endpoint, 호출 실행, Retry/UNKNOWN-Reconcile 식별 의미만 보유합니다.
 * HTTP Client, Kafka, SOAP, 인증 Token 같은 Runtime/Transport 타입과 Credential 원문은 포함하지 않습니다.</p>
 */
public record CpfIntegrationContext(
        String partnerSystemCode,
        String logicalEndpointId,
        String callExecutionId,
        int attempt,
        String idempotencyKey,
        String unknownOutcomeId,
        String recoveryId,
        Instant startedAt) {
    public CpfIntegrationContext {
        partnerSystemCode = required(partnerSystemCode, "partnerSystemCode", 128);
        logicalEndpointId = required(logicalEndpointId, "logicalEndpointId", 160);
        callExecutionId = required(callExecutionId, "callExecutionId", 160);
        if (attempt < 1) attempt = 1;
        idempotencyKey = optional(idempotencyKey, 256);
        unknownOutcomeId = optional(unknownOutcomeId, 160);
        recoveryId = optional(recoveryId, 160);
        if (startedAt == null) startedAt = Instant.now();
    }
    /** UNKNOWN 결과를 Reconcile 가능한 식별자로 승격한 새 불변 Context를 반환합니다. */
    public CpfIntegrationContext withUnknownOutcome(String unknownId, String recovery) {
        return new CpfIntegrationContext(partnerSystemCode, logicalEndpointId, callExecutionId, attempt,
                idempotencyKey, required(unknownId, "unknownOutcomeId", 160), optional(recovery, 160), startedAt);
    }

    private static String required(String value,String name,int max){String v=optional(value,max);if(v==null)throw new IllegalArgumentException(name+" is required");return v;}
    private static String optional(String value,int max){if(value==null||value.isBlank())return null;String v=value.trim();if(v.length()>max||v.chars().anyMatch(Character::isISOControl))throw new IllegalArgumentException("invalid integration context value");return v;}
}
