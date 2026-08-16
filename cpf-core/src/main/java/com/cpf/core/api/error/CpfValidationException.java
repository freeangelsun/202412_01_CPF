package com.cpf.core.api.error;

import java.util.Map;

/** 입력/계약 검증 실패를 표현하는 Core convenience subtype입니다. */
public final class CpfValidationException extends CpfException {
    public CpfValidationException(String detail) {
        super(CpfErrorCode.VALIDATION_FAILED, detail);
    }

    public CpfValidationException(CpfErrorCode fallback, String detail, Map<String, Object> arguments) {
        super(fallback, detail, arguments);
    }

    /** CpfValidationException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfValidationException(String errorReference, String detail, Map<String, Object> arguments) {
        super(errorReference, CpfErrorCode.VALIDATION_FAILED, detail, arguments);
    }
}
