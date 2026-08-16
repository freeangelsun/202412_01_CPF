package com.cpf.core.api.error;

/**
 * Fully resolved response metadata derived from a response code and message code.
 */
/** CpfResolvedResponse 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfResolvedResponse(
        int httpStatus,
        String responseCode,
        String messageCode,
        String externalMessage,
        String internalMessage,
        String errorCode,
        String errorMessage) {
}

