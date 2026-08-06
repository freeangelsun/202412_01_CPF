
package com.cpf.core.api.data.quality;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Topology-independent data-quality operations.
 *
 * <p>Public callers can validate, inspect, replay and reconcile. Correction mutation is intentionally
 * absent from this public contract and is owned by the ADM approval-engine command boundary.</p>
 */
public interface CpfDataQualityOperations {
    CpfDataQualityRule register(CpfDataQualityRule rule, String actorId, String reason);

    CpfDataQualityDecision validate(String recordId, Map<String, Object> record);

    Optional<QuarantineItem> quarantine(String quarantineId);

    /** @deprecated Client-provided approval flags are not a valid authorization source. */
    @Deprecated(forRemoval = true)
    default QuarantineItem correct(
            String quarantineId,
            long expectedVersion,
            Map<String, Object> corrected,
            String actorId,
            String reason,
            boolean approved) {
        throw new SecurityException(
                "Client approval flags are not accepted; use the ADM approved owner-command path");
    }

    CpfDataQualityDecision replay(String quarantineId, String actorId, String reason);

    ReconcileResult reconcile(String actorId, String reason);

    record QuarantineItem(
            String quarantineId,
            String recordId,
            Map<String, Object> original,
            Map<String, Object> corrected,
            String state,
            long version,
            List<CpfDataQualityDecision.Violation> violations) {
    }

    record ReconcileResult(int inspected, int replayed, int remaining) {
    }
}
