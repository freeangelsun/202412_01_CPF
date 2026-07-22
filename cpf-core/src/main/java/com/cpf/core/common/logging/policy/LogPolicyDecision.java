package cpf.pfw.common.logging.policy;

import java.util.Locale;

/**
 * 로그 정책 우선순위 평가가 끝난 뒤 런타임에서 사용하는 불변 결과입니다.
 */
public record LogPolicyDecision(
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

    public static LogPolicyDecision cpfDefault(LogPolicyTargetType targetType, String targetId) {
        return new LogPolicyDecision(
                targetType.code(),
                normalizeTargetId(targetId),
                "INFO",
                true,
                "INFO",
                false,
                false,
                true,
                "DEFAULT",
                "CPF_DEFAULT",
                null,
                null);
    }

    public LogPolicyDecision withSource(String source) {
        return new LogPolicyDecision(
                targetType,
                targetId,
                fileLogLevel,
                dbLogEnabled,
                dbLogLevel,
                requestBodySave,
                responseBodySave,
                errorStackSave,
                maskingPolicyKey,
                source,
                overrideId,
                policyId);
    }

    public String requestBodySaveYn() {
        return yn(requestBodySave);
    }

    public String responseBodySaveYn() {
        return yn(responseBodySave);
    }

    public String errorStackSaveYn() {
        return yn(errorStackSave);
    }

    public String dbLogEnabledYn() {
        return yn(dbLogEnabled);
    }

    public static String normalizeLevel(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback == null || fallback.isBlank() ? "INFO" : fallback.trim().toUpperCase(Locale.ROOT);
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizeTargetId(String targetId) {
        return targetId == null || targetId.isBlank() ? "*" : targetId.trim();
    }

    private static String yn(boolean value) {
        return value ? "Y" : "N";
    }
}
