package com.cpf.core.api.result;

/** 분산 Boundary 호출의 표준 4상태 결과입니다. */
public enum CpfResultStatus {
    SUCCESS,
    BUSINESS_FAILURE,
    TECHNICAL_FAILURE,
    UNKNOWN
}
