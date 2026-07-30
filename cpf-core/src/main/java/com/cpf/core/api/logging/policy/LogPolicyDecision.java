package com.cpf.core.api.logging.policy;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

import static com.cpf.core.api.logging.policy.LogCaptureMode.CaptureArea;

/**
 * 로그 정책 우선순위 평가가 끝난 뒤 런타임에서 사용하는 Versioned 불변 결과입니다.
 *
 * <p>Query/Header/Request Body/Response Body/Error Stack의 수집 범위와 최대 크기를
 * 독립적으로 보존합니다. 민감정보가 원문으로 저장되지 않도록 허용 목록과 마스킹
 * 정책을 함께 전달하며, 정책 Checksum은 다중 인스턴스 Drift 판정에 사용합니다.</p>
 */
public record LogPolicyDecision(
        int schemaVersion,
        String targetType,
        String targetId,
        String fileLogLevel,
        boolean dbLogEnabled,
        String dbLogLevel,
        LogCaptureMode queryCaptureMode,
        LogCaptureMode requestHeaderCaptureMode,
        LogCaptureMode responseHeaderCaptureMode,
        LogCaptureMode requestBodyCaptureMode,
        LogCaptureMode responseBodyCaptureMode,
        LogCaptureMode errorStackCaptureMode,
        List<String> queryAllowlist,
        List<String> headerAllowlist,
        List<String> fieldAllowlist,
        int maxQueryBytes,
        int maxHeaderBytes,
        int maxRequestBodyBytes,
        int maxResponseBodyBytes,
        int maxStackBytes,
        String maskingPolicyKey,
        String policyChecksum,
        String resolvedSource,
        Long overrideId,
        Long policyId) {

    public static final int CURRENT_SCHEMA_VERSION = 2;

    public LogPolicyDecision {
        if (schemaVersion < CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException("지원하지 않는 로그 정책 Schema Version입니다: " + schemaVersion);
        }
        targetType = required(targetType, "targetType");
        targetId = normalizeTargetId(targetId);
        fileLogLevel = normalizeLevel(fileLogLevel, "INFO");
        dbLogLevel = normalizeLevel(dbLogLevel, "INFO");
        queryCaptureMode = mode(queryCaptureMode, LogCaptureMode.NONE, CaptureArea.QUERY);
        requestHeaderCaptureMode = mode(requestHeaderCaptureMode, LogCaptureMode.NONE, CaptureArea.HEADER);
        responseHeaderCaptureMode = mode(responseHeaderCaptureMode, LogCaptureMode.NONE, CaptureArea.HEADER);
        requestBodyCaptureMode = mode(requestBodyCaptureMode, LogCaptureMode.NONE, CaptureArea.BODY);
        responseBodyCaptureMode = mode(responseBodyCaptureMode, LogCaptureMode.NONE, CaptureArea.BODY);
        errorStackCaptureMode = mode(errorStackCaptureMode, LogCaptureMode.SUMMARY, CaptureArea.STACK);
        queryAllowlist = normalizeList(queryAllowlist);
        headerAllowlist = normalizeList(headerAllowlist);
        fieldAllowlist = normalizeList(fieldAllowlist);
        maxQueryBytes = positive(maxQueryBytes, "maxQueryBytes");
        maxHeaderBytes = positive(maxHeaderBytes, "maxHeaderBytes");
        maxRequestBodyBytes = positive(maxRequestBodyBytes, "maxRequestBodyBytes");
        maxResponseBodyBytes = positive(maxResponseBodyBytes, "maxResponseBodyBytes");
        maxStackBytes = positive(maxStackBytes, "maxStackBytes");
        maskingPolicyKey = required(maskingPolicyKey, "maskingPolicyKey");
        resolvedSource = required(resolvedSource, "resolvedSource");
        String canonical = canonical(schemaVersion, targetType, targetId, fileLogLevel, dbLogEnabled, dbLogLevel,
                queryCaptureMode, requestHeaderCaptureMode, responseHeaderCaptureMode,
                requestBodyCaptureMode, responseBodyCaptureMode, errorStackCaptureMode,
                queryAllowlist, headerAllowlist, fieldAllowlist, maxQueryBytes, maxHeaderBytes,
                maxRequestBodyBytes, maxResponseBodyBytes, maxStackBytes, maskingPolicyKey);
        String calculated = sha256(canonical);
        if (policyChecksum == null || policyChecksum.isBlank()) {
            policyChecksum = calculated;
        } else if (!calculated.equalsIgnoreCase(policyChecksum.trim())) {
            throw new IllegalArgumentException("로그 정책 Checksum이 내용과 일치하지 않습니다.");
        } else {
            policyChecksum = policyChecksum.trim().toLowerCase(Locale.ROOT);
        }
    }

    /** 기존 Y/N 계약 Consumer의 Source 호환을 위한 생성자입니다. */
    public LogPolicyDecision(
            String targetType,
            String targetId,
            String fileLogLevel,
            boolean dbLogEnabled,
            String dbLogLevel,
            boolean requestBodySave,
            boolean responseBodySave,
            boolean errorStackSave,
            String maskingPolicyKey,
            String resolvedSource,
            Long overrideId,
            Long policyId) {
        this(CURRENT_SCHEMA_VERSION, targetType, targetId, fileLogLevel, dbLogEnabled, dbLogLevel,
                LogCaptureMode.NONE, LogCaptureMode.NONE, LogCaptureMode.NONE,
                requestBodySave ? LogCaptureMode.MASKED_BODY : LogCaptureMode.NONE,
                responseBodySave ? LogCaptureMode.MASKED_BODY : LogCaptureMode.NONE,
                errorStackSave ? LogCaptureMode.FULL_MASKED : LogCaptureMode.NONE,
                List.of(), List.of(), List.of(), 4096, 8192, 65536, 65536, 32768,
                maskingPolicyKey, null, resolvedSource, overrideId, policyId);
    }

    public static LogPolicyDecision cpfDefault(LogPolicyTargetType targetType, String targetId) {
        return new LogPolicyDecision(
                CURRENT_SCHEMA_VERSION, targetType.code(), normalizeTargetId(targetId), "INFO", true, "INFO",
                LogCaptureMode.NONE, LogCaptureMode.ALLOWLIST, LogCaptureMode.ALLOWLIST,
                LogCaptureMode.NONE, LogCaptureMode.NONE, LogCaptureMode.SUMMARY,
                List.of(), List.of("content-type", "x-cpf-transaction-id", "x-cpf-trace-id"), List.of(),
                4096, 8192, 65536, 65536, 32768,
                "DEFAULT", null, "CPF_DEFAULT", null, null);
    }

    public LogPolicyDecision withSource(String source) {
        return new LogPolicyDecision(schemaVersion, targetType, targetId, fileLogLevel, dbLogEnabled, dbLogLevel,
                queryCaptureMode, requestHeaderCaptureMode, responseHeaderCaptureMode,
                requestBodyCaptureMode, responseBodyCaptureMode, errorStackCaptureMode,
                queryAllowlist, headerAllowlist, fieldAllowlist, maxQueryBytes, maxHeaderBytes,
                maxRequestBodyBytes, maxResponseBodyBytes, maxStackBytes, maskingPolicyKey,
                null, source, overrideId, policyId);
    }

    public boolean requestBodySave() { return requestBodyCaptureMode.capturesPayload(); }
    public boolean responseBodySave() { return responseBodyCaptureMode.capturesPayload(); }
    public boolean errorStackSave() { return errorStackCaptureMode != LogCaptureMode.NONE; }
    public String requestBodySaveYn() { return yn(requestBodySave()); }
    public String responseBodySaveYn() { return yn(responseBodySave()); }
    public String errorStackSaveYn() { return yn(errorStackSave()); }
    public String dbLogEnabledYn() { return yn(dbLogEnabled); }

    public static String normalizeLevel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback == null || fallback.isBlank() ? "INFO" : fallback.trim().toUpperCase(Locale.ROOT);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizeTargetId(String targetId) {
        return targetId == null || targetId.isBlank() ? "*" : targetId.trim();
    }

    public static List<String> parseCsv(String value) {
        if (value == null || value.isBlank()) return List.of();
        return normalizeList(List.of(value.split(",")));
    }

    public static String toCsv(List<String> values) {
        return String.join(",", normalizeList(values));
    }

    private static LogCaptureMode mode(LogCaptureMode value, LogCaptureMode fallback, CaptureArea area) {
        return (value == null ? fallback : value).validateFor(area);
    }

    private static int positive(int value, String field) {
        if (value < 0) throw new IllegalArgumentException(field + " 값은 0 이상이어야 합니다.");
        return value;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " 값은 필수입니다.");
        return value.trim();
    }

    private static List<String> normalizeList(List<String> values) {
        if (values == null) return List.of();
        return values.stream().filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toLowerCase(Locale.ROOT)).distinct().sorted().toList();
    }

    private static String canonical(Object... values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            if (!builder.isEmpty()) builder.append('|');
            if (value instanceof List<?> list) builder.append(String.join(",", list.stream().map(String::valueOf).toList()));
            else builder.append(String.valueOf(value));
        }
        return builder.toString();
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }

    private static String yn(boolean value) { return value ? "Y" : "N"; }
}
