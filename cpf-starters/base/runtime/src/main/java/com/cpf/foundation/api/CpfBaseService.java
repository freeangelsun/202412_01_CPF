package com.cpf.foundation.api;

import com.cpf.core.api.error.CpfValidationException;

/**
 * CPF 업무 Service가 공통으로 재사용하는 최소 Base 계약입니다.
 *
 * <p>특정 Spring Runtime, Repository, HTTP, Batch 의미를 소유하지 않고 입력 정규화처럼
 * 거의 모든 업무 Service에서 재사용 가능한 동작만 제공합니다. 도메인 Base Service는
 * 실제 재사용 동작이 있을 때만 이 클래스를 상속합니다.</p>
 */
public abstract class CpfBaseService {
    /** null/blank 문자열을 거부하고 trim된 값을 반환합니다. */
    protected final String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            String name = fieldName == null || fieldName.isBlank() ? "value" : fieldName;
            throw new CpfValidationException(name + " 값은 필수입니다.");
        }
        return value.trim();
    }
}
