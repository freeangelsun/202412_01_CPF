package com.cpf.batch.api.error;

/** Batch 실행 결과를 Process/Job 상태와 독립적으로 표현하는 Owner Contract입니다. */
public enum CpfBatchOutcome { COMPLETED, REJECTED, RETRYABLE_FAILURE, RECONCILE_REQUIRED, FAILED }
