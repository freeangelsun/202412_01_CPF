package com.cpf.platform.operations.observability.spi.logging;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionRuntimeStatus;
import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import java.util.List;
import java.util.Optional;

/** Replaceable durable store for target-scoped versions and command idempotency. */
/** CpfLogPolicyVersionStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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

    /** WriteResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record WriteResult(Status status, CpfLogPolicyVersionSnapshot snapshot) {
        public WriteResult { if (status == null) throw new IllegalArgumentException("status is required"); }
    }
    record StatusResult(boolean updated, CpfLogPolicyVersionSnapshot snapshot) { }
    enum Status { APPLIED, IDEMPOTENT_REPLAY, VERSION_CONFLICT, COMMAND_CONFLICT, RESOURCE_EXHAUSTED, UNKNOWN }
}
