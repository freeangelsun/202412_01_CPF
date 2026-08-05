package com.cpf.core.api.logging;

import java.util.List;

/** Version-aware command contract for operational dynamic log-level changes. */
public interface CpfDynamicLogLevelCommandOperations {
    DynamicLogLevelRule register(DynamicLogLevelRequest request, long expectedVersion);

    void upsert(DynamicLogLevelRule rule, long expectedVersion);

    void replaceAll(
            List<DynamicLogLevelRule> activeRules,
            long expectedVersion,
            String actor,
            String reason);

    boolean remove(String ruleId, long expectedVersion, String actor, String reason);

    void clear(long expectedVersion, String actor, String reason);
}
