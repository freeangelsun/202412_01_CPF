package com.cpf.core.common.logging;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * 로그/감사/운영 출력에 사용되는 민감정보 마스커입니다.
 *
 * <p>정책은 immutable snapshot으로 교체되며 진행 중인 호출은 기존 snapshot을,
 * 이후 호출은 새 snapshot을 사용합니다. 빈 key 정책이나 과도한 출력 길이는
 * fail-closed 기본값으로 보정합니다.</p>
 */
public final class SensitiveDataMasker {

    private static final int DEFAULT_MAX_LENGTH = 4000;
    private static final int MIN_MAX_LENGTH = 256;
    private static final int ABSOLUTE_MAX_LENGTH = 65536;
    private static final Set<String> DEFAULT_SENSITIVE_KEYS = Set.of(
            "password", "passwd", "pwd", "token", "authorization", "auth", "secret",
            "ssn", "rrn", "resident", "residentno", "accountno", "accountnumber",
            "cardno", "cardnumber", "pin", "otp", "apikey", "api_key", "clientsecret"
    );
    private static final AtomicReference<MaskingPolicy> POLICY =
            new AtomicReference<>(MaskingPolicy.create(DEFAULT_SENSITIVE_KEYS, DEFAULT_MAX_LENGTH, true));

    private SensitiveDataMasker() {
    }

    public static String mask(String value) {
        MaskingPolicy policy = POLICY.get();
        return mask(value, policy.maxLength(), policy);
    }

    public static String mask(String value, int maxLength) {
        return mask(value, maxLength, POLICY.get());
    }

    /** Runtime Control Plane에서 검증된 전체 정책 snapshot을 원자 교체합니다. */
    public static MaskingPolicy replacePolicy(Set<String> sensitiveKeys, int maxLength, boolean maskBearerToken) {
        MaskingPolicy next = MaskingPolicy.create(sensitiveKeys, maxLength, maskBearerToken);
        POLICY.set(next);
        return next;
    }

    public static MaskingPolicy currentPolicy() {
        return POLICY.get();
    }

    private static String mask(String value, int maxLength, MaskingPolicy policy) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String masked = value;
        for (Pattern pattern : policy.jsonPatterns()) {
            masked = pattern.matcher(masked).replaceAll("$1***$2");
        }
        for (Pattern pattern : policy.keyValuePatterns()) {
            masked = pattern.matcher(masked).replaceAll("$1***");
        }
        if (policy.maskBearerToken()) {
            masked = policy.bearerPattern().matcher(masked).replaceAll("$1***");
        }
        return truncate(masked, normalizeMaxLength(maxLength));
    }

    public static String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...(truncated)";
    }

    private static int normalizeMaxLength(int value) {
        if (value < MIN_MAX_LENGTH) return MIN_MAX_LENGTH;
        return Math.min(value, ABSOLUTE_MAX_LENGTH);
    }

    public record MaskingPolicy(
            Set<String> sensitiveKeys,
            int maxLength,
            boolean maskBearerToken,
            List<Pattern> jsonPatterns,
            List<Pattern> keyValuePatterns,
            Pattern bearerPattern) {

        private static MaskingPolicy create(Set<String> sensitiveKeys, int maxLength, boolean maskBearerToken) {
            LinkedHashSet<String> normalized = new LinkedHashSet<>(DEFAULT_SENSITIVE_KEYS);
            if (sensitiveKeys != null) {
                sensitiveKeys.stream()
                        .filter(key -> key != null && !key.isBlank())
                        .map(key -> key.trim().toLowerCase(Locale.ROOT))
                        .filter(key -> key.matches("[a-z0-9_.-]{2,64}"))
                        .forEach(normalized::add);
            }
            List<Pattern> json = normalized.stream()
                    .map(Pattern::quote)
                    .map(key -> Pattern.compile("(?i)(\\\"" + key + "\\\"\\s*:\\s*\\\")[^\\\"]*(\\\")"))
                    .toList();
            List<Pattern> keyValue = normalized.stream()
                    .map(Pattern::quote)
                    .map(key -> Pattern.compile("(?i)(\\b" + key + "\\b\\s*[=:]\\s*)[^,\\]\\}&\\s]+"))
                    .toList();
            return new MaskingPolicy(
                    Set.copyOf(normalized),
                    normalizeMaxLength(maxLength),
                    maskBearerToken,
                    List.copyOf(json),
                    List.copyOf(keyValue),
                    Pattern.compile("(?i)(Bearer\\s+)[A-Za-z0-9._~+/=-]+"));
        }
    }
}
