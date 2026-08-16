package com.cpf.batch.api.error;

import com.cpf.core.api.error.CpfErrorDefinition;
import com.cpf.core.api.error.CpfErrorDefinition.RetryDisposition;

/** Core 오류 의미를 Batch Outcome으로 변환합니다. */
public final class CpfBatchErrorMapper {
    private CpfBatchErrorMapper() { }
    public static CpfBatchOutcome outcome(CpfErrorDefinition error) {
        if (error == null) return CpfBatchOutcome.FAILED;
        return switch (error.retryDisposition()) {
            case NEVER -> CpfBatchOutcome.REJECTED;
            case SAFE -> CpfBatchOutcome.RETRYABLE_FAILURE;
            case RECONCILE, UNKNOWN -> CpfBatchOutcome.RECONCILE_REQUIRED;
        };
    }
}
