package com.cpf.core.spi.logging;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionRuntimeStatus;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import java.util.List;
import java.util.Optional;

/** Replaceable durable store for target-scoped versions and command idempotency. */
public interface CpfLogPolicyVersionStore {
    CpfLogPolicyVersionSnapshot ensureBaseline(CpfLogPolicyVersionSnapshot baseline);
    Optional<CpfLogPolicyVersionSnapshot> current(LogPolicyTargetType targetType, String targetId);
    Optional<CpfLogPolicyVersionSnapshot> findVersion(LogPolicyTargetType targetType, String targetId, long version);
    List<CpfLogPolicyVersionSnapshot> history(LogPolicyTargetType targetType, String targetId, int limit);
    WriteResult compareAndSet(long expectedVersion, String commandId, String commandHash,
            CpfLogPolicyVersionSnapshot next);
    StatusResult updateStatus(LogPolicyTargetType targetType, String targetId, long expectedVersion,
            CpfLogPolicyVersionSnapshot.Status expectedStatus, CpfLogPolicyVersionSnapshot.Status nextStatus,
            String actor, String reason);
    CpfLogPolicyVersionRuntimeStatus runtimeStatus();

    record WriteResult(Status status, CpfLogPolicyVersionSnapshot snapshot) {
        public WriteResult { if (status == null) throw new IllegalArgumentException("status is required"); }
    }
    record StatusResult(boolean updated, CpfLogPolicyVersionSnapshot snapshot) { }
    enum Status { APPLIED, IDEMPOTENT_REPLAY, VERSION_CONFLICT, COMMAND_CONFLICT, RESOURCE_EXHAUSTED, UNKNOWN }
}
