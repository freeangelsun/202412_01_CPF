package com.cpf.web.api.openapi;

/**
 * Web Profile이 제공하는 OpenAPI 운영 조회·재대사 Public Contract입니다.
 * Springdoc/Swagger 구현 타입을 Public API에 노출하지 않습니다.
 */
public interface CpfOpenApiOperations {
    CpfOpenApiSnapshot snapshot();
    /** 감사 사유를 남기고 현재 Web MVC Route Inventory를 다시 계산합니다. */
    CpfOpenApiSnapshot refresh(String reason);
}
