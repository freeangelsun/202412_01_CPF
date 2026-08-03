package com.cpf.batch.worker;

import java.util.Locale;
import java.util.Objects;

/**
 * Stable process identity plus the current lease/fencing generation for a batch worker.
 *
 * <p>The canonical identifier is intentionally self-describing so two JVMs, restarts, and
 * recovered leases cannot collapse to the same registry row.</p>
 */
public record CpfBatchWorkerIdentity(
        String systemId,
        String instanceId,
        String processId,
        String restartId,
        long leaseEpoch,
        long fencingToken) {

    public CpfBatchWorkerIdentity {
        systemId = segment(systemId, "systemId");
        instanceId = segment(instanceId, "instanceId");
        processId = segment(processId, "processId");
        restartId = segment(restartId, "restartId");
        if (leaseEpoch < 0) {
            throw new IllegalArgumentException("leaseEpoch must be non-negative");
        }
        if (fencingToken < 0) {
            throw new IllegalArgumentException("fencingToken must be non-negative");
        }
    }

    public String canonicalId() {
        return String.join(":",
                systemId,
                instanceId,
                processId,
                restartId,
                Long.toUnsignedString(leaseEpoch),
                Long.toUnsignedString(fencingToken));
    }

    private static String segment(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim().toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "-")
                .replaceAll("-+", "-");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return normalized.length() <= 40 ? normalized : normalized.substring(0, 40);
    }
}
