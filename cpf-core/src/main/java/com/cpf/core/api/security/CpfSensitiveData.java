package com.cpf.core.api.security;

import com.cpf.core.api.error.CpfValidationException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CPF 공통 민감정보 정규화·표시 마스킹 유틸리티입니다.
 *
 * <p>원문 값을 로그나 예외 메시지에 포함하지 않으며, 전화번호는 국제번호(+), 내선(x/#),
 * 구분자 문자를 허용하되 숫자형으로 변환하지 않습니다.</p>
 */
public final class CpfSensitiveData {
    private static final int MAX_PHONE_LENGTH = 50;
    private static final int MAX_EMAIL_LENGTH = 200;
    private static final int MAX_AUDIT_REASON_LENGTH = 500;
    private static final Pattern PHONE = Pattern.compile("^[+0-9() .\\-xX#]*$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern AUDIT_SECRET = Pattern.compile(
            "(?i)\\b(password|passwd|pwd|token|access[_-]?token|refresh[_-]?token|authorization|api[_-]?key|secret|credential)\\s*[:=]\\s*([^\\s,;]+)");
    private static final Pattern BEARER = Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._~+/=-]+");
    private static final Pattern KOREAN_RRN = Pattern.compile("(?<!\\d)\\d{6}[- ]?\\d{7}(?!\\d)");
    private static final Pattern EMAIL_IN_TEXT = Pattern.compile("(?i)(?<![A-Z0-9._%+-])[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}(?![A-Z0-9._%+-])");
    private static final Pattern PHONE_IN_TEXT = Pattern.compile("(?<!\\d)(?:01[016789]|0[2-6][1-5]?)[ .-]?\\d{3,4}[ .-]?\\d{4}(?!\\d)");
    private static final Pattern AUDIT_JSON_SECRET = Pattern.compile(
            "(?i)([\"']?(?:password|passwd|pwd|token|access[_-]?token|refresh[_-]?token|authorization|api[_-]?key|secret|credential|private[_-]?key)[\"']?\\s*[:=]\\s*[\"']?)([^\"',}\\s]+)");
    private static final Pattern AUDIT_JSON_PII = Pattern.compile(
            "(?i)([\"']?(?:email|mobile|mobileNo|phone|officePhone|contact|address|resident[_-]?no|rrn|ssn)[\"']?\\s*[:=]\\s*[\"']?)([^\"',}\\s]+)");
    private static final int MAX_AUDIT_SNAPSHOT_DEPTH = 32;

    private CpfSensitiveData() {}

    public static String blankToNull(String value) {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    public static String normalizePhone(String value, String fieldName) {
        String normalized = blankToNull(value);
        if (normalized == null) return null;
        rejectControlCharacters(normalized, fieldName);
        if (normalized.length() > MAX_PHONE_LENGTH || !PHONE.matcher(normalized).matches()) {
            throw new CpfValidationException(fieldName + " 형식이 올바르지 않습니다.");
        }
        return normalized;
    }

    public static String normalizeEmail(String value, String fieldName) {
        String normalized = blankToNull(value);
        if (normalized == null) return null;
        rejectControlCharacters(normalized, fieldName);
        if (normalized.length() > MAX_EMAIL_LENGTH || !EMAIL.matcher(normalized).matches()) {
            throw new CpfValidationException(fieldName + " 형식이 올바르지 않습니다.");
        }
        int at = normalized.lastIndexOf('@');
        return normalized.substring(0, at) + "@" + normalized.substring(at + 1).toLowerCase(Locale.ROOT);
    }


    /**
     * 감사 사유를 저장하기 전에 제어문자와 과도한 길이를 거부하고 Secret/PII 패턴을 제거합니다.
     *
     * <p>사유는 운영자가 장애 원인을 설명하는 자유문이지만 Password, Bearer Token, 주민번호,
     * 이메일, 전화번호 같은 원문을 Evidence나 감사 DB에 남기는 통로가 되어서는 안 됩니다.</p>
     */
    public static String sanitizeAuditReason(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) {
            throw new CpfValidationException("감사 사유는 필수입니다.");
        }
        rejectControlCharacters(normalized, "reason");
        if (normalized.length() > MAX_AUDIT_REASON_LENGTH) {
            throw new CpfValidationException("감사 사유는 " + MAX_AUDIT_REASON_LENGTH + "자 이하로 입력해야 합니다.");
        }
        String sanitized = BEARER.matcher(normalized).replaceAll("Bearer [REDACTED]");
        sanitized = AUDIT_SECRET.matcher(sanitized).replaceAll("$1=[REDACTED]");
        sanitized = KOREAN_RRN.matcher(sanitized).replaceAll("[PII_REDACTED]");
        sanitized = EMAIL_IN_TEXT.matcher(sanitized).replaceAll("[PII_REDACTED]");
        sanitized = PHONE_IN_TEXT.matcher(sanitized).replaceAll("[PII_REDACTED]");
        return sanitized;
    }

    /**
     * 감사 Before/After/Diff/Error Snapshot을 field-aware 방식으로 재귀 정제합니다.
     *
     * <p>Map/Iterable/배열은 key를 기준으로 Secret은 완전 제거하고 연락처·주소·식별 PII는 마스킹합니다.
     * 단순 문자열은 JSON/로그 형태를 포함해 Secret, 주민번호, Email, 전화번호 패턴을 제거합니다.
     * 입력 객체는 수정하지 않으며 반환 객체는 감사 저장 전용 복사본입니다.</p>
     */
    public static Object sanitizeAuditSnapshot(Object value) {
        return sanitizeAuditSnapshot(value, null, 0);
    }

    /** 감사 DB의 문자열 Column에 저장할 수 있도록 정제된 Snapshot을 문자열로 변환합니다. */
    public static String sanitizeAuditSnapshotText(Object value) {
        if (value == null) return null;
        Object sanitized = sanitizeAuditSnapshot(value);
        return sanitized == null ? null : String.valueOf(sanitized);
    }

    /** 자유형 오류/직렬화 문자열을 감사 저장 전에 정제합니다. 비어 있는 값은 그대로 허용합니다. */
    public static String sanitizeAuditText(String value) {
        if (value == null) return null;
        String sanitized = BEARER.matcher(value).replaceAll("Bearer [REDACTED]");
        sanitized = AUDIT_SECRET.matcher(sanitized).replaceAll("$1=[REDACTED]");
        sanitized = AUDIT_JSON_SECRET.matcher(sanitized).replaceAll("$1[REDACTED]");
        sanitized = AUDIT_JSON_PII.matcher(sanitized).replaceAll("$1[MASKED]");
        sanitized = KOREAN_RRN.matcher(sanitized).replaceAll("[PII_REDACTED]");
        sanitized = EMAIL_IN_TEXT.matcher(sanitized).replaceAll("[PII_REDACTED]");
        sanitized = PHONE_IN_TEXT.matcher(sanitized).replaceAll("[PII_REDACTED]");
        return sanitized;
    }

    private static Object sanitizeAuditSnapshot(Object value, String fieldName, int depth) {
        if (value == null) return null;
        if (depth > MAX_AUDIT_SNAPSHOT_DEPTH) return "[DEPTH_LIMIT]";
        String key = fieldName == null ? "" : normalizeAuditKey(fieldName);
        if (isSecretAuditKey(key)) return "[REDACTED]";
        if (isPiiAuditKey(key)) return maskAuditPii(key, value);
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new LinkedHashMap<>();
            map.forEach((k, v) -> {
                String name = String.valueOf(k);
                copy.put(name, sanitizeAuditSnapshot(v, name, depth + 1));
            });
            return copy;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> copy = new ArrayList<>();
            for (Object element : iterable) copy.add(sanitizeAuditSnapshot(element, fieldName, depth + 1));
            return copy;
        }
        if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            List<Object> copy = new ArrayList<>(length);
            for (int i = 0; i < length; i++) copy.add(sanitizeAuditSnapshot(Array.get(value, i), fieldName, depth + 1));
            return copy;
        }
        if (value instanceof CharSequence || value instanceof Character) return sanitizeAuditText(String.valueOf(value));
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) return value;
        return sanitizeAuditText(String.valueOf(value));
    }

    private static String normalizeAuditKey(String fieldName) {
        return fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private static boolean isSecretAuditKey(String key) {
        return key.contains("password") || key.contains("passwd") || key.equals("pwd")
                || key.contains("token") || key.contains("authorization") || key.contains("apikey")
                || key.contains("secret") || key.contains("credential") || key.contains("privatekey");
    }

    private static boolean isPiiAuditKey(String key) {
        return key.contains("email") || key.contains("mobile") || key.contains("phone") || key.contains("contact")
                || key.contains("address") || key.contains("resident") || key.equals("rrn") || key.equals("ssn");
    }

    private static String maskAuditPii(String key, Object value) {
        String text = blankToNull(String.valueOf(value));
        if (text == null) return null;
        if (key.contains("email")) return maskEmail(text);
        if (key.contains("mobile") || key.contains("phone") || key.contains("contact")) return maskPhone(text);
        return "[MASKED]";
    }

    public static String maskPhone(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) return null;
        String digits = normalized.replaceAll("\\D", "");
        if (digits.length() <= 4) return "****";
        return "***-****-" + digits.substring(digits.length() - 4);
    }

    public static String maskEmail(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) return null;
        int at = normalized.indexOf('@');
        if (at <= 0) return "***";
        return normalized.substring(0, 1) + "***" + normalized.substring(at);
    }

    private static void rejectControlCharacters(String value, String fieldName) {
        if (value.chars().anyMatch(ch -> Character.isISOControl(ch))) {
            throw new CpfValidationException(fieldName + "에는 제어문자를 사용할 수 없습니다.");
        }
    }
}
