package cpf.bat.edu.ondemand;

import java.time.Instant;
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
        result = result == null ? Map.of() : Map.copyOf(result);
    }
}
