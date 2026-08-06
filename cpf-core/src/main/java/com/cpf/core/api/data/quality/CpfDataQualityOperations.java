
package com.cpf.core.api.data.quality;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Topology-independent data-quality operations.
 *
 * <p>Corrections are accepted only through an immutable server-side approval reference.
 * The legacy boolean overload is retained temporarily for source migration but always
 * fails closed, so a caller can never manufacture approval with a request flag.</p>
 */
public interface CpfDataQualityOperations {
    CpfDataQualityRule register(CpfDataQualityRule rule, String actorId, String reason);

    CpfDataQualityDecision validate(String recordId, Map<String, Object> record);

    Optional<QuarantineItem> quarantine(String quarantineId);

    QuarantineItem correctAuthorized(
            String quarantineId,
            long expectedVersion,
            Map<String, Object> corrected,
            String actorId,
            String reason,
            CorrectionAuthorization authorization);

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
                "Client approval flags are not accepted; use a server-issued CorrectionAuthorization");
    }

    CpfDataQualityDecision replay(String quarantineId, String actorId, String reason);

    ReconcileResult reconcile(String actorId, String reason);

    record CorrectionAuthorization(
            String approvalReference,
            String approvedBy,
            Instant approvedAt) {
        public CorrectionAuthorization {
            approvalReference = required(approvalReference, "approvalReference");
            approvedBy = required(approvedBy, "approvedBy");
            approvedAt = Objects.requireNonNull(approvedAt, "approvedAt");
            if (approvedAt.isAfter(Instant.now().plusSeconds(300))) {
                throw new IllegalArgumentException("approvedAt cannot be materially in the future");
            }
        }

        private static String required(String value, String field) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(field + " is required");
            }
            return value.trim();
        }
    }

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
