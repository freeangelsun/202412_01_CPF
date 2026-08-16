package com.cpf.security.api;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** Provider와 무관한 불변 Session Snapshot. */
public record CpfSessionSnapshot(
        String sessionId,
        String tenantId,
        String principalId,
        Instant createdAt,
        Instant lastAccessedAt,
        Instant expiresAt,
        long generation,
        boolean revoked,
        Map<String, String> attributes) {
    public CpfSessionSnapshot {
        sessionId = requireText(sessionId, "sessionId");
        tenantId = requireText(tenantId, "tenantId");
        principalId = requireText(principalId, "principalId");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        lastAccessedAt = Objects.requireNonNull(lastAccessedAt, "lastAccessedAt");
        expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");
        if (lastAccessedAt.isBefore(createdAt)) throw new IllegalArgumentException("lastAccessedAt before createdAt");
        if (!expiresAt.isAfter(lastAccessedAt)) throw new IllegalArgumentException("expiresAt must be after lastAccessedAt");
        if (generation < 1) throw new IllegalArgumentException("generation must be >= 1");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " required");
        return value;
    }
}
