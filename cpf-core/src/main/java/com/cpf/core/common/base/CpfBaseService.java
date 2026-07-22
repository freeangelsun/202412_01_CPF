package com.cpf.core.common.base;

import com.cpf.core.common.exception.CpfValidationException;

/**
 * CPF 업무 Service가 사용하는 canonical 기술 확장점입니다.
 *
 * <p>특정 업무나 저장소에 의존하지 않으며 공통 validation hook만 제공합니다.
 * 트랜잭션 경계와 업무 규칙은 주제영역 Base와 Feature Service가 소유합니다.</p>
 *
 * @since 1.0.0
 */
public abstract class CpfBaseService {

    /**
     * 필수 문자열을 검증하고 앞뒤 공백을 제거합니다.
     *
     * @param value 검증할 값
     * @param fieldName 오류 메시지에 사용할 필드명
     * @return 정규화한 문자열
     * @throws CpfValidationException 값이 없을 때
     */
    protected final String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }
}
