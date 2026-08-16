package com.cpf.integration.error;

import com.cpf.core.api.error.CpfErrorDefinition.RetryDisposition;

/** Core의 기술 중립 retry semantics를 외부 연계 실행 의미로 변환합니다. */
public final class CpfExternalErrorMapper {
    private CpfExternalErrorMapper() { }
    public static CpfExternalFailureDisposition disposition(RetryDisposition retryDisposition) {
        if (retryDisposition == null) return CpfExternalFailureDisposition.RECONCILE;
        return switch (retryDisposition) {
            case NEVER -> CpfExternalFailureDisposition.FAIL_FAST;
            case SAFE -> CpfExternalFailureDisposition.RETRY;
            case RECONCILE, UNKNOWN -> CpfExternalFailureDisposition.RECONCILE;
        };
    }
}
