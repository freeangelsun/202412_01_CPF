package com.cpf.core.api.error;

import java.util.Objects;

/**
 * DB/설정 Catalog에서 실제 오류 정의를 조회하기 위한 기술 중립 참조입니다.
 *
 * <p>업무 오류 코드는 Java enum 확장 대상이 아닙니다. 애플리케이션은 responseCode와
 * arguments만 예외에 싣고, Common Error Catalog가 런타임에 category/retry/message metadata를
 * 해석합니다. fallbackDefinition은 Catalog 장애·누락 시에도 안전한 외부 응답 의미를 보장합니다.</p>
 */
public record CpfErrorReference(String responseCode, CpfErrorDefinition fallbackDefinition) {
    public CpfErrorReference {
        if (responseCode == null || responseCode.isBlank()) {
            throw new IllegalArgumentException("responseCode is required");
        }
        responseCode = responseCode.trim();
        fallbackDefinition = Objects.requireNonNull(fallbackDefinition, "fallbackDefinition");
    }

    /** of 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfErrorReference of(CpfErrorDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        return new CpfErrorReference(definition.statusCode(), definition);
    }

    public static CpfErrorReference business(String responseCode) {
        return new CpfErrorReference(responseCode, CpfErrorCode.BUSINESS_RULE_VIOLATION);
    }

    /** validation 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfErrorReference validation(String responseCode) {
        return new CpfErrorReference(responseCode, CpfErrorCode.VALIDATION_FAILED);
    }

    public static CpfErrorReference system(String responseCode) {
        return new CpfErrorReference(responseCode, CpfErrorCode.INTERNAL_SERVER_ERROR);
    }
}
