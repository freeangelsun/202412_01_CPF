package com.cpf.foundation.observability;

import java.time.Instant;

/**
 * A sanitized failure that occurred before an application handler could create its normal transaction evidence.
 *
 * <p>This Foundation contract deliberately contains no Servlet/Spring/DB type. Boundary adapters publish it and
 * the observability owner decides how to persist or project it. Raw request headers and bodies must not be placed
 * in this record.</p>
 */
public record CpfBoundaryFailureEvidence(
        String boundaryType,
        String transactionId,
        String traceReference,
        String systemCode,
        String application,
        String instanceId,
        String hostName,
        String hostIp,
        String processId,
        String fieldName,
        String category,
        String errorCode,
        int httpStatus,
        String method,
        String uri,
        String clientIp,
        Instant occurredAt) {
}
