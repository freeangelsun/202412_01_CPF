package com.cpf.core.api.error;

import java.util.Map;

/**
 * CPF 프레임워크 계층에서 발생한 오류를 표준 응답코드로 전달하는 예외입니다.
 */
public class CpfFrameworkException extends CpfException {

    public CpfFrameworkException(CpfFrameworkErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    /** CpfFrameworkException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfFrameworkException(
            CpfFrameworkErrorCode errorCode,
            String detail,
            Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}
