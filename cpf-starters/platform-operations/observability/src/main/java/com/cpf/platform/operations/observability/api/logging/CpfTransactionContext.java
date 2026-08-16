package com.cpf.platform.operations.observability.api.logging;

import com.cpf.platform.operations.observability.internal.logging.CpfTransactionContextAnomalyMonitor;
import com.cpf.platform.operations.observability.internal.logging.TransactionContext;
import com.cpf.platform.operations.observability.internal.logging.TransactionHeader;
import com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentContext;

import java.util.Map;

/**
 * Generated Domain이 거래 Context 내부 구현에 의존하지 않고 표준 식별자/사용자/헤더를 조회하는 공개 facade입니다.
 */
public final class CpfTransactionContext {
    private CpfTransactionContext() { }

    public static String transactionId() { return TransactionContext.getOrCreateTransactionId(); }
    public static String traceId() { return TransactionContext.getOrCreateTraceId(); }
    public static String spanId() { return TransactionContext.getOrCreateSpanId(); }
    /** currentTransactionId 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String currentTransactionId() { return TransactionContext.currentTransactionId(); }
    public static String currentTraceId() { return TransactionContext.currentTraceId(); }
    /** 현재 CPF 실행 인스턴스 ID입니다. */
    public static String executionId() { return com.cpf.core.api.context.CpfContexts.currentExecutionId(); }
    /** 현재 실행의 재시도 순번입니다. Context가 없으면 0입니다. */
    public static int attempt() {
        var context = com.cpf.core.api.context.CpfContexts.current();
        return context == null ? 0 : context.execution().attempt();
    }
    public static String memberNo() { return TransactionContext.memberNo(); }
    public static String customerNo() { return TransactionContext.customerNo(); }
    public static String channelCode() { return TransactionContext.channelCode(); }
    /** nextSequence는 거래 로그 lineage에 필요한 표준 실행·사용자 문맥을 일관되게 제공합니다. */
    public static long nextSequence() { return TransactionContext.nextSequenceNo(); }
    /** userId는 표준 Header wire name을 직접 문자열로 반복하지 않도록 제공하는 개발자용 접근 API입니다. */
    public static String userId() { return TransactionContext.userId(); }
    /** operatorId 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String operatorId() { return TransactionContext.operatorId(); }
    public static String idempotencyKey() {
        TransactionHeader h = TransactionContext.currentHeader();
        return h == null ? null : h.getIdempotencyKey();
    }
    public static Map<String,String> propagationHeaders() { return TransactionContext.propagationHeaders(); }
    public static Map<String,String> outboundHeaders() {
        return com.cpf.platform.operations.observability.internal.logging.header.CpfHeaderPropagator.outboundHeaders();
    }
    /** currentSegmentId 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String currentSegmentId() { return TransactionSegmentContext.currentSegmentId(); }
    /** 거래 Context 누락 탐지 누적 건수의 운영용 공개 View입니다. */
    public static long missingCount() { return CpfTransactionContextAnomalyMonitor.missingCount(); }

    /** Generated Domain 단위테스트에서만 사용하는 간단한 Context 초기화 helper입니다. */
    public static void initializeForTest(String transactionId, String idempotencyKey, String userId) {
        TransactionHeader header = TransactionHeader.builder()
                .idempotencyKey(idempotencyKey)
                .userId(userId)
                .build();
        TransactionContext.initialize(transactionId, "test-trace", null, transactionId, header);
    }
    /** clear 작업을 CPF 표준 계약에 따라 수행한다. */
    public static void clear() { TransactionContext.clear(); }
}
