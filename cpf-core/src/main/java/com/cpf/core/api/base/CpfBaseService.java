package com.cpf.core.api.base;

import com.cpf.core.api.error.CpfValidationException;

/** Generated Domain과 고객 업무 Service가 사용할 수 있는 CPF 공개 기반 Service입니다. */
public abstract class CpfBaseService {
    protected final String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }
}
