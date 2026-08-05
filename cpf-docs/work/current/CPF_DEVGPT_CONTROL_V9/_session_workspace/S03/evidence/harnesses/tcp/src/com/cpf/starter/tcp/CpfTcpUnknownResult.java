package com.cpf.starter.tcp;

import java.time.Instant;
import java.util.Objects;

public record CpfTcpUnknownResult(
        String correlationId,
        Instant writtenAt,
        byte[] request,
        String detail) {
    public CpfTcpUnknownResult {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
        correlationId = correlationId.trim();
        writtenAt = Objects.requireNonNull(writtenAt, "writtenAt must not be null");
        request = Objects.requireNonNull(request, "request must not be null").clone();
        detail = detail == null || detail.isBlank() ? "UNKNOWN" : detail.trim();
    }

    @Override
    public byte[] request() {
        return request.clone();
    }
}
