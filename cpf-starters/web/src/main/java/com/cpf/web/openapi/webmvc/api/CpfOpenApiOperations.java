package com.cpf.web.openapi.webmvc.api;

/** Public operations contract without Springdoc or Swagger types. */
/** CpfOpenApiOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfOpenApiOperations {
    CpfOpenApiSnapshot snapshot();

    /** Reconciles the local Web MVC route inventory. A non-blank audited reason is mandatory. */
    CpfOpenApiSnapshot refresh(String reason);
}
