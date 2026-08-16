package com.cpf.platform.operations.observability.api.logging;

import java.util.List;

/** Version-aware command contract for operational dynamic log-level changes. */
/** CpfDynamicLogLevelCommandOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
