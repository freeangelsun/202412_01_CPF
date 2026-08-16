package com.cpf.platform.operations.observability.api.logging.policy;

import java.util.List;

/** Public version-aware log-policy query, mutation and recovery contract. */
/** CpfLogPolicyVersionOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfLogPolicyVersionOperations {
    CpfLogPolicyVersionSnapshot current(LogPolicyTargetType targetType, String targetId);
    List<CpfLogPolicyVersionSnapshot> history(LogPolicyTargetType targetType, String targetId, int limit);
    CpfLogPolicyVersionResult update(CpfLogPolicyVersionUpdateCommand command);
    CpfLogPolicyVersionResult rollback(CpfLogPolicyVersionRollbackCommand command);
    CpfLogPolicyVersionResult reconcile(CpfLogPolicyVersionReconcileCommand command);
    CpfLogPolicyVersionRuntimeStatus runtimeStatus();
}
