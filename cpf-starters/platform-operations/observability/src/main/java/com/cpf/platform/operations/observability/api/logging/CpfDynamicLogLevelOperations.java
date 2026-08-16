package com.cpf.platform.operations.observability.api.logging;

import java.util.List;
import java.util.Optional;

/**
 * 운영·EDU 모듈이 Core의 런타임 저장 구현에 직접 의존하지 않고
 * 거래별 동적 로그 레벨을 관리하는 공개 계약입니다.
 */
public interface CpfDynamicLogLevelOperations {
    DynamicLogLevelRule register(DynamicLogLevelRequest request);

    void upsert(DynamicLogLevelRule rule);

    void replaceAll(List<DynamicLogLevelRule> activeRules);

    Optional<DynamicLogLevelRule> resolve(
            String transactionId,
            String businessTransactionId,
            String moduleId);

    List<DynamicLogLevelRule> findActiveRules();

    boolean remove(String ruleId);

    void clear();
}
