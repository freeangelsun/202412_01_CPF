package com.cpf.starter.openapi.webmvc.api;

/** Public operations contract without Springdoc or Swagger types. */
public interface CpfOpenApiOperations {
    CpfOpenApiSnapshot snapshot();

    /** Reconciles the local Web MVC route inventory. A non-blank audited reason is mandatory. */
    CpfOpenApiSnapshot refresh(String reason);
}
