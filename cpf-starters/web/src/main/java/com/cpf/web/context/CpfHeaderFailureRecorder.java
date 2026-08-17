package com.cpf.web.context;

import java.time.Instant;

/** Observability SPI for header failures that happen before Controller/ControllerAdvice. */
@FunctionalInterface
public interface CpfHeaderFailureRecorder {
    void record(Failure failure);

    record Failure(
            String transactionId,
            String traceReference,
            String systemCode,
            String application,
            String instance,
            String headerName,
            String category,
            String errorCode,
            int httpStatus,
            String method,
            String uri,
            String clientIp,
            Instant occurredAt) { }
}
