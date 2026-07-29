package com.cpf.core.api.logging;

import com.cpf.core.common.logging.CpfTransactionContextAnomalyMonitor;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.logging.TransactionHeader;
import com.cpf.core.common.logging.segment.TransactionSegmentContext;

import java.util.Map;

/**
 * Generated Domain이 거래 Context 내부 구현에 의존하지 않고 표준 식별자/사용자/헤더를 조회하는 공개 facade입니다.
 */
public final class CpfTransactionContext {
    private CpfTransactionContext() { }

    public static String transactionId() { return TransactionContext.getOrCreateTransactionId(); }
    public static String traceId() { return TransactionContext.getOrCreateTraceId(); }
    public static String spanId() { return TransactionContext.getOrCreateSpanId(); }
    public static String currentTransactionId() { return TransactionContext.currentTransactionId(); }
    public static String currentTraceId() { return TransactionContext.currentTraceId(); }
    public static String memberNo() { return TransactionContext.memberNo(); }
    public static String customerNo() { return TransactionContext.customerNo(); }
    public static String channelCode() { return TransactionContext.channelCode(); }
    public static long nextSequence() { return TransactionContext.nextSequenceNo(); }
    public static String userId() { return TransactionContext.userId(); }
    public static String operatorId() { return TransactionContext.operatorId(); }
    public static String idempotencyKey() {
        TransactionHeader h = TransactionContext.currentHeader();
        return h == null ? null : h.getIdempotencyKey();
    }
    public static Map<String,String> propagationHeaders() { return TransactionContext.propagationHeaders(); }
    public static Map<String,String> outboundHeaders() {
        return com.cpf.core.common.header.CpfHeaderPropagator.outboundHeaders();
    }
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
    public static void clear() { TransactionContext.clear(); }
}
