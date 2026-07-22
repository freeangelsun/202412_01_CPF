package com.cpf.core.common.logging.policy;

/**
 * cpf_log_policy 또는 cpf_log_policy_override 조회 결과입니다.
 */
public record LogPolicyRow(
        Long policyId,
        Long overrideId,
        String targetType,
        String targetId,
        String logLevel,
        String dbLogEnabledYn,
        String fileLogEnabledYn,
        String requestBodyLogYn,
        String responseBodyLogYn,
        String errorStackLogYn,
        String maskingPolicyKey,
        String source) {
}
