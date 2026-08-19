package com.cpf.web.api.openapi;

/** OpenAPI runtime inventory health state. */
/** OpenAPI Runtime 상태를 외부 Consumer가 안정적으로 판별하는 Public 상태값입니다. */
public enum CpfOpenAPIStatus {
    UP,
    DEGRADED,
    DOWN
}
