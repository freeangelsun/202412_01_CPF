package com.cpf.web.api.openapi;

/** Public operations for observing and explicitly refreshing the OpenAPI route inventory. */
/** 현재 Runtime의 OpenAPI operation 목록을 조회하는 Public 계약입니다. */
public interface CpfOpenAPIOperations {
    CpfOpenAPISnapshot snapshot();

    CpfOpenAPISnapshot refresh(String reason);
}
