package com.cpf.core.common.batch;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPF 공통 배치 실행 결과 모델입니다.
 *
 * @param executed               실제 Spring Batch 실행 여부
 * @param jobId                  배치 Job ID
 * @param cpfExecutionId         CPF 운영 메타 실행 ID
 * @param springBatchExecutionId Spring Batch 실행 ID
 * @param status                 실행 상태
 * @param message                운영자에게 보여줄 요약 메시지
 * @param detail                 상세 데이터
 */
public record CpfBatchExecutionResult(
        boolean executed,
        String jobId,
        Long cpfExecutionId,
        Long springBatchExecutionId,
        String status,
        String message,
        Map<String, Object> detail) {

    public CpfBatchExecutionResult {
        detail = detail == null ? Map.of() : new LinkedHashMap<>(detail);
    }

    public static CpfBatchExecutionResult of(
            boolean executed,
            String jobId,
            Long cpfExecutionId,
            Long springBatchExecutionId,
            String status,
            String message,
            Map<String, Object> detail) {
        return new CpfBatchExecutionResult(
                executed,
                jobId,
                cpfExecutionId,
                springBatchExecutionId,
                status,
                message,
                detail);
    }
}
