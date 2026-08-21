package com.cpf.batch.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Runtime liveness/capacity heartbeat contract.
 *
 * <p>{@code fencingToken}은 과거 Batch 독립 Runtime Registry 호환 필드이며 중앙 lifecycle authority에서는
 * 사용하지 않습니다. Runtime registration fencing은 CPF Runtime Control Agent가 별도로 소유합니다.</p>
 */
public record RuntimeHeartbeat(
        String instanceId,
        Instant timestamp,
        ActualState actualState,
        boolean ready,
        List<String> currentExecutions,
        List<String> activeLeases,
        int availableCapacity,
        long queueDepth,
        boolean draining,
        Map<String, String> dependencyHealth,
        String lastErrorCode,
        Map<String, Number> metrics,
        String deploymentVersion,
        long fencingToken
) {
    public RuntimeHeartbeat {
        Objects.requireNonNull(instanceId, "instanceId");
        Objects.requireNonNull(actualState, "actualState");
        timestamp = timestamp == null ? Instant.now() : timestamp;
        currentExecutions = currentExecutions == null ? List.of() : List.copyOf(currentExecutions);
        activeLeases = activeLeases == null ? List.of() : List.copyOf(activeLeases);
        dependencyHealth = dependencyHealth == null ? Map.of() : Map.copyOf(dependencyHealth);
        metrics = metrics == null ? Map.of() : Map.copyOf(metrics);
    }
}
