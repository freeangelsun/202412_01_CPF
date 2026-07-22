package cpf.bat.edu.ondemand;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** 온디맨드 접수와 Spring Batch 실행을 연결하는 상태 응답입니다. */
public record BatOnDemandStatus(
        String executionRequestId,
        String standardBatchId,
        String idempotencyKey,
        String transactionGlobalId,
        String businessDate,
        String requestStatus,
        Long pfwExecutionId,
        Long springBatchExecutionId,
        Map<String, Object> result,
        String failureCode,
        String failureMessage,
        Instant requestedAt,
        Instant completedAt) {

    public BatOnDemandStatus {
        result = immutableNullableMap(result);
    }

    private static Map<String, Object> immutableNullableMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
