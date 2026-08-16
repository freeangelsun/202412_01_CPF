package com.cpf.messaging.error;

import com.cpf.core.api.error.CpfErrorDefinition;
import com.cpf.core.api.error.CpfErrorDefinition.RetryDisposition;

/** Core 오류 의미를 Broker retry/DLQ/reconcile 정책 입력으로 변환합니다. */
public final class CpfMessageErrorMapper {
    private CpfMessageErrorMapper() { }
    public static CpfMessageFailureAction action(CpfErrorDefinition error) {
        RetryDisposition disposition = error == null ? RetryDisposition.UNKNOWN : error.retryDisposition();
        return switch (disposition) {
            case NEVER -> CpfMessageFailureAction.DLQ;
            case SAFE -> CpfMessageFailureAction.RETRY;
            case RECONCILE, UNKNOWN -> CpfMessageFailureAction.RECONCILE;
        };
    }
}
