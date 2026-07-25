package com.cpf.core.api.error;

import java.util.Map;

/** 업무 규칙 위반을 CPF 표준 오류 계약으로 전달하는 공개 예외입니다. */
public class CpfBusinessException extends com.cpf.core.common.exception.CpfBusinessException {
    public CpfBusinessException(String detail) {
        super(detail);
    }

    public CpfBusinessException(CpfErrorCode errorCode, String detail) {
        super(errorCode.internalDefinition(), detail);
    }

    public CpfBusinessException(CpfErrorCode errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode.internalDefinition(), detail, messageArguments);
    }
}
