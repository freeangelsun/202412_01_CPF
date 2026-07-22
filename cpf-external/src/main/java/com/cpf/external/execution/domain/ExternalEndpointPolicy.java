package com.cpf.external.execution.domain;

/** 운영자가 등록한 대외 endpoint와 결과 조회 정책입니다. */
public record ExternalEndpointPolicy(
        String institutionCode,
        String endpointCode,
        String serviceId,
        String requestPath,
        String resultQueryPath,
        int timeoutMillis,
        int retryCount) {
}
