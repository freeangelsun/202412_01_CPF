package com.cpf.web.api.openapi;

import java.time.Instant;

/** Immutable snapshot of the current OpenAPI route inventory. */
/** 현재 OpenAPI route inventory와 Runtime 상태를 함께 전달하는 불변 Public snapshot입니다. */
public record CpfOpenAPISnapshot(
        CpfOpenAPIStatus status,
        boolean enabled,
        boolean apiDocsEnabled,
        String apiDocsPath,
        String instanceId,
        long operationCount,
        Instant refreshedAt,
        String refreshReason,
        String failure) {
}
