
package com.cpf.data.api.quality;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Topology-independent data-quality operations.
 *
 * <p>Public callers can validate, inspect, replay and reconcile. Correction mutation is intentionally
 * absent from this public contract and is owned by the ADM approval-engine command boundary.</p>
 */
/** CpfDataQualityOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfDataQualityOperations {
    CpfDataQualityRule register(CpfDataQualityRule rule, String actorId, String reason);

    CpfDataQualityDecision validate(String recordId, Map<String, Object> record);

    Optional<QuarantineItem> quarantine(String quarantineId);


    /** CAS/idempotent replay. Validation is performed without creating a second quarantine row. */
    CpfDataQualityDecision replay(ReplayCommand command);

    /** Compatibility read path; production consumers must use the versioned command. */
    @Deprecated(forRemoval = true)
    default CpfDataQualityDecision replay(String quarantineId, String actorId, String reason) {
        QuarantineItem item = quarantine(quarantineId)
                .orElseThrow(() -> new IllegalArgumentException("quarantine not found"));
        return replay(new ReplayCommand(quarantineId, item.version(),
                "legacy-" + java.util.UUID.randomUUID(), actorId, reason));
    }

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
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

    /** ReplayCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ReplayCommand(String quarantineId, long expectedVersion, String operationId,
                         String actorId, String reason) {
        public ReplayCommand {
            if (quarantineId == null || quarantineId.isBlank()) throw new IllegalArgumentException("quarantineId is required");
            if (expectedVersion < 1) throw new IllegalArgumentException("expectedVersion must be positive");
            if (operationId == null || operationId.isBlank()) throw new IllegalArgumentException("operationId is required");
            if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId is required");
            if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason is required");
        }
    }

    /** ReconcileResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record ReconcileResult(int inspected, int replayed, int remaining) {
    }
}
