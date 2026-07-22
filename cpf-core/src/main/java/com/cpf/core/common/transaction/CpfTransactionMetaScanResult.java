package com.cpf.core.common.transaction;

import java.util.List;

/**
 * 온라인 거래 메타 scan 결과입니다.
 */
public record CpfTransactionMetaScanResult(
        boolean available,
        int detectedCount,
        int upsertedCount,
        int inactivatedCount,
        List<String> transactionIds,
        String message) {
}
