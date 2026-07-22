package com.cpf.core.common.logging.policy;

/**
 * 온라인 거래와 배치 런타임이 공통으로 사용하는 로그 정책 조회 진입점입니다.
 */
public class LogPolicyResolver {

    private final LogPolicyCache cache;

    public LogPolicyResolver(LogPolicyCache cache) {
        this.cache = cache;
    }

    public LogPolicyDecision resolveOnlineTransaction(String transactionId) {
        return cache.resolve(LogPolicyTargetType.ONLINE_TRANSACTION, transactionId);
    }

    public LogPolicyDecision resolveBatchJob(String jobId) {
        return cache.resolve(LogPolicyTargetType.BATCH_JOB, jobId);
    }

    public LogPolicyDecision resolveBatchStep(String jobId, String stepName) {
        return cache.resolve(LogPolicyTargetType.BATCH_STEP, batchStepTargetId(jobId, stepName));
    }

    public LogPolicyDecision refresh(String targetType, String targetId) {
        return cache.refresh(LogPolicyTargetType.fromCode(targetType), targetId);
    }

    public void evict(String targetType, String targetId) {
        cache.evict(LogPolicyTargetType.fromCode(targetType), targetId);
    }

    public void clear() {
        cache.clear();
    }

    public int cachedSize() {
        return cache.size();
    }

    private String batchStepTargetId(String jobId, String stepName) {
        String normalizedJobId = LogPolicyDecision.normalizeTargetId(jobId);
        String normalizedStepName = LogPolicyDecision.normalizeTargetId(stepName);
        return normalizedJobId + ":" + normalizedStepName;
    }
}
