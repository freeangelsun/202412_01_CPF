package com.cpf.data.error;

import com.cpf.core.api.error.CpfErrorCode;

/** Data Owner가 Provider 예외를 Core 의미로 정규화한 결과입니다. Provider 원문 메시지는 포함하지 않습니다. */
public record CpfPersistenceFailure(CpfErrorCode errorCode, String operation, boolean retryable, String providerCode) {
    public CpfPersistenceFailure {
        if (errorCode == null) errorCode = CpfErrorCode.DATABASE_ERROR;
        if (operation == null) operation = "";
        if (providerCode == null) providerCode = "";
    }
}
