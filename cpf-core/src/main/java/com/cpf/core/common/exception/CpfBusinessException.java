package com.cpf.core.common.exception;

import java.util.Map;

/** 업무 규칙 위반을 CPF 표준 응답 코드와 메시지 인자로 전달하는 예외입니다. */
public class CpfBusinessException extends CpfException {
    public CpfBusinessException(String detail) {
        super(CpfErrorCode.BUSINESS_RULE_VIOLATION, detail);
    }

    public CpfBusinessException(CpfErrorDefinition errorCode, String detail) {
        super(errorCode, detail);
    }

    public CpfBusinessException(CpfErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }

    public CpfBusinessException(String responseCode, String detail, Map<String, Object> messageArguments) {
        super(responseCode, detail, messageArguments);
    }

    public CpfBusinessException(
            CpfErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, externalMessage, internalMessage, detail, null, messageArguments);
    }
}

