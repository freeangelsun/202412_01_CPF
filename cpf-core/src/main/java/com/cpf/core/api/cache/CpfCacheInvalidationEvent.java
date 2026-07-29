package com.cpf.core.api.cache;

import java.time.Instant;
import java.util.Objects;

/** DB Durable 원장과 Redis Fast Channel이 공유하는 무효화 Event입니다. */
public record CpfCacheInvalidationEvent(long eventId, String eventKey, String tenantId,
                                        String namespace, String cacheKey, long version,
                                        String reason, String requestedBy, Instant createdAt) {
    public CpfCacheInvalidationEvent {
        eventKey = required(eventKey, "eventKey", 180);
        tenantId = tenantId == null || tenantId.isBlank() ? "GLOBAL" : required(tenantId, "tenantId", 80);
        namespace = required(namespace, "namespace", 80);
        cacheKey = cacheKey == null ? "" : cacheKey.trim();
        reason = required(reason, "reason", 500);
        requestedBy = requestedBy == null || requestedBy.isBlank() ? "SYSTEM" : required(requestedBy, "requestedBy", 180);
        createdAt = createdAt == null ? Instant.now() : createdAt;
        if (cacheKey.length() > 512) throw new IllegalArgumentException("cacheKey 허용 길이를 초과했습니다.");
        if (version < 0 || eventId < 0) throw new IllegalArgumentException("eventId/version은 음수일 수 없습니다.");
    }

    private static String required(String value, String field, int maxLength) {
        String normalized = Objects.requireNonNull(value, field + "는 필수입니다.").trim();
        if (normalized.isEmpty()) throw new IllegalArgumentException(field + "는 비어 있을 수 없습니다.");
        if (normalized.length() > maxLength) throw new IllegalArgumentException(field + " 허용 길이를 초과했습니다.");
        if (normalized.indexOf('\n') >= 0 || normalized.indexOf('\r') >= 0 || normalized.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(field + "에 제어문자를 사용할 수 없습니다.");
        }
        return normalized;
    }
}
