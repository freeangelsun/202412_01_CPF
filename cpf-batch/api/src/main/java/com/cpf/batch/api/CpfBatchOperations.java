package com.cpf.batch.api;

/**
 * 업무 Application이 Spring Batch 구현을 직접 다루지 않고 On-Demand Batch를 실행/조회하는 공개 API입니다.
 * 권한/감사/멱등성/UNKNOWN 처리는 BAT Owner Runtime이 책임집니다.
 */
public interface CpfBatchOperations {
    CpfBatchExecutionResult launch(CpfBatchExecutionRequest request);
    CpfBatchExecutionResult status(String executionRequestId);
}
