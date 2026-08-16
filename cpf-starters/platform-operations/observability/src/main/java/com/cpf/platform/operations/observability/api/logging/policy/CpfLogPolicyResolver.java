package com.cpf.platform.operations.observability.api.logging.policy;

/**
 * 운영 모듈과 CPF Runtime이 로그 정책 구현·cache에 직접 결합하지 않고
 * 정책을 조회·갱신하는 공개 계약입니다.
 */
public interface CpfLogPolicyResolver {
    LogPolicyDecision resolveOnlineTransaction(String transactionId);

    LogPolicyDecision resolveBatchJob(String jobId);

    LogPolicyDecision resolveBatchStep(String jobId, String stepName);

    LogPolicyDecision refresh(String targetType, String targetId);

    void evict(String targetType, String targetId);

    void clear();

    int cachedSize();
}
