package com.cpf.core.api.cache;

import java.time.Instant;
import java.util.List;

/** Cache Provider의 Readiness/Topology/TLS/Drift 상태입니다. */
public record CpfCacheHealth(boolean ready, String provider, String topology, boolean tls,
                             boolean durableInvalidationReady, long lastSuccessfulOperationEpochMillis,
                             List<String> reasonCodes, Instant observedAt) {
    public CpfCacheHealth {
        provider = provider == null || provider.isBlank() ? "UNKNOWN" : provider;
        topology = topology == null || topology.isBlank() ? "UNKNOWN" : topology;
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        observedAt = observedAt == null ? Instant.now() : observedAt;
    }
}
