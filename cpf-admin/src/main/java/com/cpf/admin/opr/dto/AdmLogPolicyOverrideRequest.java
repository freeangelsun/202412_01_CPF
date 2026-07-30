package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;

/** ADM 로그 정책 임시 Versioned override 요청입니다. */
public record AdmLogPolicyOverrideRequest(
        Long policyId,
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
        LocalDateTime effectiveStartAt,
        LocalDateTime effectiveEndAt,
        String approvedBy,
        String requestUser,
        String reason) {

    public AdmLogPolicyOverrideRequest(
            Long policyId, String targetType, String targetId, String logLevel,
            String dbLogEnabledYn, String fileLogEnabledYn, String requestBodyLogYn,
            String responseBodyLogYn, String errorStackLogYn, String maskingPolicyKey,
            LocalDateTime effectiveStartAt, LocalDateTime effectiveEndAt,
            String approvedBy, String requestUser, String reason) {
        this(policyId, targetType, targetId, logLevel, dbLogEnabledYn, fileLogEnabledYn,
                null, null, null,
                ynMode(requestBodyLogYn, "MASKED_BODY"),
                ynMode(responseBodyLogYn, "MASKED_BODY"),
                ynMode(errorStackLogYn, "FULL_MASKED"),
                null, null, null, null, null, null, null, null,
                maskingPolicyKey, effectiveStartAt, effectiveEndAt, approvedBy, requestUser, reason);
    }

    public String requestBodyLogYn() { return captureYn(requestBodyCaptureMode); }
    public String responseBodyLogYn() { return captureYn(responseBodyCaptureMode); }
    public String errorStackLogYn() { return "NONE".equalsIgnoreCase(errorStackCaptureMode) ? "N" : "Y"; }

    private static String ynMode(String yn, String enabled) {
        if (yn == null || yn.isBlank()) return null;
        return "Y".equalsIgnoreCase(yn) ? enabled : "NONE";
    }
    private static String captureYn(String value) {
        if (value == null || value.isBlank()) return null;
        return (!"NONE".equalsIgnoreCase(value) && !"METADATA_ONLY".equalsIgnoreCase(value)) ? "Y" : "N";
    }
}
