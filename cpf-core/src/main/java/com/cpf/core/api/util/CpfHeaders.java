package com.cpf.core.api.util;

import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.core.common.header.CpfHeaderNames;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF 표준 Header 이름과 안전한 전달 Map 생성을 제공하는 Public API입니다.
 *
 * <p>신규 업무 코드는 문자열 literal을 직접 만들기보다 이 API를 사용하고,
 * 신뢰 경계/검증/마스킹/자동 전파는 Core Header Engine에 맡깁니다.</p>
 */
public final class CpfHeaders {
    private CpfHeaders() {}

    public static String transactionId() { return CpfHeaderNames.TRANSACTION_ID; }
    public static String segmentId() { return CpfHeaderNames.TRANSACTION_SEGMENT_ID; }
    public static String parentSegmentId() { return CpfHeaderNames.PARENT_TRANSACTION_SEGMENT_ID; }
    public static String standardExecutionId() { return CpfHeaderNames.STANDARD_EXECUTION_ID; }
    public static String idempotencyKey() { return CpfHeaderNames.IDEMPOTENCY_KEY; }
    public static String traceId() { return CpfHeaderNames.TRACE_ID; }
    public static String spanId() { return CpfHeaderNames.SPAN_ID; }
    public static String channelCode() { return CpfHeaderNames.CHANNEL_CODE; }
    public static String originalChannelCode() { return CpfHeaderNames.ORIGINAL_CHANNEL_CODE; }
    public static String userId() { return CpfHeaderNames.USER_ID; }
    public static String operatorId() { return CpfHeaderNames.OPERATOR_ID; }
    public static String tenantId() { return CpfHeaderNames.TENANT_ID; }
    public static String callerService() { return CpfHeaderNames.CALLER_SERVICE; }
    public static String callerInstanceId() { return CpfHeaderNames.CALLER_INSTANCE_ID; }

    /**
     * 개발자/Generator가 자주 사용하는 핵심 표준 Header 이름입니다.
     * 전체 내부 신뢰/보안 Header 목록의 정본은 {@link CpfHeaderNames}와 Core Header Engine입니다.
     */
    public static List<String> standardNames() {
        return List.of(
                transactionId(), standardExecutionId(), segmentId(), parentSegmentId(), idempotencyKey(),
                traceId(), spanId(), originalChannelCode(), channelCode(), userId(), operatorId(), tenantId(),
                callerService(), callerInstanceId());
    }

    public static Map<String,String> transaction(String transactionId) {
        return transaction(transactionId, null, null);
    }

    /** Canonical transactionId와 선택 Segment 계층을 순서 보존 immutable Map으로 만듭니다. */
    public static Map<String,String> transaction(String transactionId, String segmentIdValue, String parentSegmentIdValue) {
        LinkedHashMap<String,String> headers = new LinkedHashMap<>();
        headers.put(transactionId(), CpfTransactionIds.requireCanonical(transactionId));
        if (CpfStrings.hasText(segmentIdValue)) headers.put(segmentId(), segmentIdValue.trim());
        if (CpfStrings.hasText(parentSegmentIdValue)) headers.put(parentSegmentId(), parentSegmentIdValue.trim());
        return Collections.unmodifiableMap(headers);
    }
}
