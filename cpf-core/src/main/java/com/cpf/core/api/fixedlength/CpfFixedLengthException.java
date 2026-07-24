package com.cpf.core.api.fixedlength;

import java.util.List;

/**
 * 고정길이 layout 또는 전문을 안전하게 처리할 수 없을 때 발생합니다.
 *
 * <p>예외 메시지는 원문 값을 포함하지 않으며, 상세 오류의 거절 값은 필드의
 * 민감도 정책에 따라 마스킹됩니다.</p>
 */
public class CpfFixedLengthException extends RuntimeException {
    private final List<CpfFixedLengthError> errors;

    public CpfFixedLengthException(String message, List<CpfFixedLengthError> errors) {
        super(message);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public CpfFixedLengthException(String message, Throwable cause, List<CpfFixedLengthError> errors) {
        super(message, cause);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<CpfFixedLengthError> errors() {
        return errors;
    }

    public List<CpfFixedLengthError> getErrors() {
        return errors;
    }
}
