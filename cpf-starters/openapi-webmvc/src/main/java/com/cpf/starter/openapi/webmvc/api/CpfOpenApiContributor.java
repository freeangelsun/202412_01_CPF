package com.cpf.starter.openapi.webmvc.api;

/** Customer extension SPI. Implementations must be deterministic and must not add secrets or personal data. */
@FunctionalInterface
public interface CpfOpenApiContributor {
    void contribute(CpfOpenApiDocument document);
}
