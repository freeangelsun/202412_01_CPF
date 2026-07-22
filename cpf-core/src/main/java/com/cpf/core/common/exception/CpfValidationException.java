package com.cpf.core.common.exception;

import java.util.Map;

/**
 * Exception for request and business input validation failures.
 */
public class CpfValidationException extends CpfException {
    public CpfValidationException(String detail) {
        super(CpfErrorCode.INVALID_PARAMETER, detail);
    }

    public CpfValidationException(CpfErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode, detail, messageArguments);
    }
}
