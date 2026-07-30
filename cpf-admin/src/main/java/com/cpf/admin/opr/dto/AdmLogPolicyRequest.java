package com.cpf.admin.opr.dto;

import java.math.BigDecimal;

/** ADM Versioned 로그 정책 등록/수정 요청입니다. */
public record AdmLogPolicyRequest(
        String policyKey,
        String policyName,
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
        Integer retentionDays,
        BigDecimal samplingRate,
        Integer priority,
        String activeYn,
        String description,
        String requestUser,
        String reason) {

    /** 기존 Y/N DTO Consumer의 Source 호환 생성자입니다. */
    public AdmLogPolicyRequest(
            String policyKey, String policyName, String targetType, String targetId, String logLevel,
            String dbLogEnabledYn, String fileLogEnabledYn, String requestBodyLogYn,
            String responseBodyLogYn, String errorStackLogYn, String maskingPolicyKey,
            Integer retentionDays, BigDecimal samplingRate, Integer priority, String activeYn,
            String description, String requestUser, String reason) {
        this(policyKey, policyName, targetType, targetId, logLevel, dbLogEnabledYn, fileLogEnabledYn,
                "NONE", "ALLOWLIST", "ALLOWLIST",
                "Y".equalsIgnoreCase(requestBodyLogYn) ? "MASKED_BODY" : "NONE",
                "Y".equalsIgnoreCase(responseBodyLogYn) ? "MASKED_BODY" : "NONE",
                "Y".equalsIgnoreCase(errorStackLogYn) ? "FULL_MASKED" : "NONE",
                null, "content-type,x-cpf-transaction-id,x-cpf-trace-id", null,
                4096, 8192, 65536, 65536, 32768,
                maskingPolicyKey, retentionDays, samplingRate, priority, activeYn,
                description, requestUser, reason);
    }

    public String requestBodyLogYn() { return captures(requestBodyCaptureMode) ? "Y" : "N"; }
    public String responseBodyLogYn() { return captures(responseBodyCaptureMode) ? "Y" : "N"; }
    public String errorStackLogYn() { return "NONE".equalsIgnoreCase(errorStackCaptureMode) ? "N" : "Y"; }

    private static boolean captures(String value) {
        return value != null && !value.isBlank()
                && !"NONE".equalsIgnoreCase(value) && !"METADATA_ONLY".equalsIgnoreCase(value);
    }
}
