package com.cpf.starter.openapi.webmvc.api;

import java.time.Instant;
import java.util.Objects;

/** Sanitized operational snapshot. It never contains controller source, credentials, or raw schemas. */
public record CpfOpenApiSnapshot(
        CpfOpenApiStatus status,
        boolean enabled,
        boolean apiDocsEnabled,
        String apiDocsPath,
        String instanceId,
        long operationCount,
        Instant refreshedAt,
        String refreshReason,
        String failureCode) {

    public CpfOpenApiSnapshot {
        Objects.requireNonNull(status, "status");
        apiDocsPath = sanitizePath(apiDocsPath);
        instanceId = sanitize(instanceId, "unknown");
        refreshedAt = Objects.requireNonNullElse(refreshedAt, Instant.EPOCH);
        refreshReason = sanitize(refreshReason, "not-refreshed");
        failureCode = sanitize(failureCode, "");
        if (operationCount < 0) throw new IllegalArgumentException("operationCount must be >= 0");
    }

    private static String sanitizePath(String value) {
        String result = sanitize(value, "/v3/api-docs");
        if (!result.startsWith("/")) throw new IllegalArgumentException("apiDocsPath must start with /");
        return result;
    }

    private static String sanitize(String value, String fallback) {
        if (value == null || value.isBlank()) return fallback;
        String trimmed = value.trim();
        return trimmed.length() <= 500 ? trimmed : trimmed.substring(0, 500);
    }
}
