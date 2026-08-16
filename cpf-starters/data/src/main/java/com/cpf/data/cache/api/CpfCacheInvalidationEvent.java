package com.cpf.data.cache.api;
import java.time.Instant;
import java.util.Objects;
/** CpfCacheInvalidationEvent 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCacheInvalidationEvent(long eventId, String eventKey, String tenantId, String namespace,
        String cacheKey, long version, String reason, String requestedBy, Instant createdAt) {
    public CpfCacheInvalidationEvent {
        if (eventId < 0 || version < 0) throw new IllegalArgumentException("eventId/version must not be negative");
        eventKey = required(eventKey, "eventKey", 180); tenantId = required(tenantId, "tenantId", 180);
        namespace = required(namespace, "namespace", 180); cacheKey = cacheKey == null ? "" : cacheKey.trim();
        reason = required(reason, "reason", 500); requestedBy = required(requestedBy, "requestedBy", 180);
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
    }
    private static String required(String v,String n,int max){String x=Objects.requireNonNull(v,n).trim();if(x.isEmpty()||x.length()>max)throw new IllegalArgumentException(n+" format is invalid");return x;}
}
