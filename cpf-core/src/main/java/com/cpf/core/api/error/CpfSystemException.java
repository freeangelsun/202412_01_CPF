package com.cpf.core.api.error;

import java.util.Map;

/** CPF/framework/system/infrastructure의 예상하지 못한 fault를 표현합니다. 외부에는 안전한 fallback만 노출합니다. */
public class CpfSystemException extends CpfException {
    public CpfSystemException(String detail) {
        super(CpfErrorCode.INTERNAL_SERVER_ERROR, detail);
    }

    public CpfSystemException(String detail, Throwable cause) {
        super(CpfErrorCode.INTERNAL_SERVER_ERROR, detail, cause);
    }

    /** CpfSystemException 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfSystemException(CpfErrorCode fallback, String detail, Throwable cause) {
        super(fallback, detail, cause);
    }

    public CpfSystemException(String errorReference, CpfErrorCode fallback, String detail,
                              Throwable cause, Map<String, Object> arguments) {
        super(errorReference, fallback, detail, cause, arguments);
    }
}
