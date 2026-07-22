package com.cpf.core.common.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 거래 context가 필수인 경계에서 누락을 정상 로그와 분리해 집계합니다.
 */
public final class CpfTransactionContextAnomalyMonitor {
    private static final Logger log = LoggerFactory.getLogger(CpfTransactionContextAnomalyMonitor.class);
    private static final AtomicLong MISSING_CONTEXT_COUNT = new AtomicLong();

    private CpfTransactionContextAnomalyMonitor() {
    }

    public static long recordMissing(String boundary) {
        long count = MISSING_CONTEXT_COUNT.incrementAndGet();
        log.error("CPF transaction context is missing. boundary={}, missingCount={}", boundary, count);
        return count;
    }

    public static long missingCount() {
        return MISSING_CONTEXT_COUNT.get();
    }

    static void resetForTest() {
        MISSING_CONTEXT_COUNT.set(0L);
    }
}
