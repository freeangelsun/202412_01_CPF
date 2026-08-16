package com.cpf.web.openapi.webmvc.api;

/** Customer extension SPI. Implementations must be deterministic and must not add secrets or personal data. */
@FunctionalInterface
/** CpfOpenApiContributor 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfOpenApiContributor {
    void contribute(CpfOpenApiDocument document);
}
