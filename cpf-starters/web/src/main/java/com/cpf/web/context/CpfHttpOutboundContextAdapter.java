package com.cpf.web.context;

import com.cpf.core.api.context.CpfContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Core Context를 bounded HTTP Header carrier로 변환하는 Web Owner adapter입니다.
 * 내부 호출은 Tx/Exec lineage를 반드시 전파하고 외부 호출에는 필요한 end-to-end 값만 노출합니다.
 */
public final class CpfHttpOutboundContextAdapter {
    public Map<String,String> headers(CpfContext context, CpfWebContext interaction, CpfHttpOutboundRequest request) {
        Objects.requireNonNull(context, "context");
        CpfHttpOutboundRequest target = request == null
                ? new CpfHttpOutboundRequest(null, null, null, false)
                : request;
        Map<String,String> headers = new LinkedHashMap<>();
        put(headers, CpfHttpHeaderNames.CORRELATION_ID, context.transaction().correlationId());
        if (context.operation() != null) {
            put(headers, CpfHttpHeaderNames.IDEMPOTENCY_KEY, context.operation().idempotencyKey());
        }
        if (interaction != null) {
            put(headers, CpfHttpHeaderNames.TRACEPARENT, interaction.traceparent());
            put(headers, CpfHttpHeaderNames.TRACESTATE, interaction.tracestate());
        }
        if (!target.trustedInternal()) return Map.copyOf(headers);

        // 신뢰된 내부 hop은 추적 lineage가 끊기지 않도록 필수 거래 Header를 항상 생성합니다.
        putRequired(headers, CpfHttpHeaderNames.TRANSACTION_ID, context.transaction().transactionId());
        putRequired(headers, CpfHttpHeaderNames.EXECUTION_ID, context.execution().executionId());
        put(headers, CpfHttpHeaderNames.ROOT_TRANSACTION_ID, context.transaction().rootTransactionId());
        if (context.transaction().businessDate() != null) {
            put(headers, CpfHttpHeaderNames.BUSINESS_DATE, context.transaction().businessDate().toString());
        }
        put(headers, CpfHttpHeaderNames.ROOT_EXECUTION_ID, context.execution().rootExecutionId());
        put(headers, CpfHttpHeaderNames.PARENT_EXECUTION_ID, context.execution().parentExecutionId());
        put(headers, CpfHttpHeaderNames.SEGMENT_ID, context.execution().segmentId());
        put(headers, CpfHttpHeaderNames.PARENT_SEGMENT_ID, context.execution().parentSegmentId());
        put(headers, CpfHttpHeaderNames.STANDARD_EXECUTION_ID, context.execution().standardExecutionId());
        putRequired(headers, CpfHttpHeaderNames.CALLER, context.transaction().callerSystemCode());
        putRequired(headers, CpfHttpHeaderNames.TARGET, target.targetSystem());
        if (context.identity() != null) {
            put(headers, CpfHttpHeaderNames.USER_ID, context.identity().subjectId());
            put(headers, CpfHttpHeaderNames.OPERATOR_ID, context.identity().actorId());
        }
        if (context.tenant() != null) put(headers, CpfHttpHeaderNames.TENANT_ID, context.tenant().tenantId());
        put(headers, CpfHttpHeaderNames.API_VERSION, target.apiVersion());
        return Map.copyOf(headers);
    }

    private static void put(Map<String,String> headers, String name, String value) {
        if (value != null && !value.isBlank()) headers.put(name, value);
    }

    private static void putRequired(Map<String,String> headers, String name, String value) {
        if (value == null || value.isBlank()) {
            throw new CpfHeaderValidationException(
                    com.cpf.core.api.error.CpfFrameworkErrorCode.MISSING_TRANSACTION_HEADER,
                    name,
                    "내부 거래 필수 Header를 생성할 Context 값이 없습니다: " + name);
        }
        headers.put(name, value);
    }
}
