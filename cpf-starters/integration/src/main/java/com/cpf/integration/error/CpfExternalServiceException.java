package com.cpf.integration.error;

import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfException;

/** 외부 Provider 호출 실패와 UNKNOWN 결과는 Integration Owner가 소유합니다. */
public class CpfExternalServiceException extends CpfException {
    private final boolean unknownOutcome;

    public CpfExternalServiceException(String detail, Throwable cause) {
        this(false, detail, cause);
    }
    public CpfExternalServiceException(String detail) {
        this(false, detail, null);
    }
    public CpfExternalServiceException(boolean unknownOutcome, String detail, Throwable cause) {
        super(unknownOutcome ? CpfErrorCode.EXTERNAL_UNKNOWN_OUTCOME : CpfErrorCode.EXTERNAL_SERVICE_ERROR, detail, cause);
        this.unknownOutcome = unknownOutcome;
    }
    public boolean unknownOutcome() { return unknownOutcome; }
}
