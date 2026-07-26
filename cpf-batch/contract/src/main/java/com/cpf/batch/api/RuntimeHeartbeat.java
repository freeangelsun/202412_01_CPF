package com.cpf.batch.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Runtime liveness/capacity/fencing heartbeat contract. */
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
