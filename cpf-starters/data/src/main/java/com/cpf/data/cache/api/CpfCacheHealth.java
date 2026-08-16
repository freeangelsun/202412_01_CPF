package com.cpf.data.cache.api;
import java.time.Instant;
import java.util.List;
/** CpfCacheHealth 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCacheHealth(boolean ready, String provider, String topology, boolean tls,
        boolean durableInvalidationConfigured, long lastSuccessEpochMillis, List<String> reasonCodes, Instant observedAt) {
    public CpfCacheHealth { reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes); }
}
