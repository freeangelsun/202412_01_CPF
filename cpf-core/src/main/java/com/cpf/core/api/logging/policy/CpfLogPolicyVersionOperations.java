package com.cpf.core.api.logging.policy;

import java.util.List;

/** Public version-aware log-policy query, mutation and recovery contract. */
public interface CpfLogPolicyVersionOperations {
    CpfLogPolicyVersionSnapshot current(LogPolicyTargetType targetType, String targetId);
    List<CpfLogPolicyVersionSnapshot> history(LogPolicyTargetType targetType, String targetId, int limit);
    CpfLogPolicyVersionResult update(CpfLogPolicyVersionUpdateCommand command);
    CpfLogPolicyVersionResult rollback(CpfLogPolicyVersionRollbackCommand command);
    CpfLogPolicyVersionResult reconcile(CpfLogPolicyVersionReconcileCommand command);
    CpfLogPolicyVersionRuntimeStatus runtimeStatus();
}
