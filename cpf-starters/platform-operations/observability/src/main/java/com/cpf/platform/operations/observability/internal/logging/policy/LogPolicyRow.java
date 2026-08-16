package com.cpf.platform.operations.observability.internal.logging.policy;

/** DB에서 조회한 Versioned 로그 정책 Row입니다. */
record LogPolicyRow(
        Long policyId,
        Long overrideId,
        Integer schemaVersion,
        String targetType,
        String targetId,
        String logLevel,
        String dbLogEnabledYn,
        String fileLogEnabledYn,
        String queryCaptureMode,
        String requestHeaderCaptureMode,
        String responseHeaderCaptureMode,
        String requestBodyCaptureMode,
        String responseBodyCaptureMode,
        String errorStackCaptureMode,
        String queryAllowlist,
        String headerAllowlist,
        String fieldAllowlist,
        Integer maxQueryBytes,
        Integer maxHeaderBytes,
        Integer maxRequestBodyBytes,
        Integer maxResponseBodyBytes,
        Integer maxStackBytes,
        String maskingPolicyKey,
        String policyChecksum,
        String source) {
}
