package com.cpf.web.openapi.webmvc.api;

/** Topology-independent status of the local CPF OpenAPI Web MVC capability. */
/** CpfOpenApiStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum CpfOpenApiStatus {
    UP,
    DEGRADED,
    DOWN,
    UNKNOWN
}
