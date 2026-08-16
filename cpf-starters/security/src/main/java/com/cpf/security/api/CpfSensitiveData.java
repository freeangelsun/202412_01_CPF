package com.cpf.security.api;

import com.cpf.core.api.error.CpfValidationException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
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
    /** Version of the field-classification and masking contract used by audit/evidence consumers. */
    public static final int CURRENT_POLICY_VERSION = 3;

    public enum Classification { PUBLIC, PII, CREDENTIAL, SECRET }
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
    private static final int MAX_AUDIT_COLLECTION_ITEMS = 10_000;
    private static final int MAX_AUDIT_TEXT_LENGTH = 65_536;
    private static final int MAX_AUDIT_FIELD_NAME_LENGTH = 256;
    private static final int MAX_AUDIT_SNAPSHOT_NODES = 20_000;
    private static final int MAX_AUDIT_SNAPSHOT_CHARACTERS = 262_144;
    private static final String TRUNCATED_SUFFIX = "...[TRUNCATED]";
    private static final String TRUNCATED_ITEMS = "[TRUNCATED_ITEMS]";
    private static final String TRUNCATED_BUDGET = "[TRUNCATED_BUDGET]";
    private static final String UNREPRESENTABLE = "[UNREPRESENTABLE]";

    private CpfSensitiveData() {}

    /** blankToNull 작업을 CPF 표준 계약에 따라 수행한다. */
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

    /** normalizeEmail 작업을 CPF 표준 계약에 따라 수행한다. */
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
        return sanitizeAuditSnapshot(
                value, null, 0, new IdentityHashMap<>(), new SnapshotBudget());
    }

    /** Classifies a field name without inspecting or returning its value. */
    /** classifyField 작업을 CPF 표준 계약에 따라 수행한다. */
    public static Classification classifyField(String fieldName) {
        String key = fieldName == null ? "" : normalizeAuditKey(fieldName);
        if (isCredentialAuditKey(key)) return Classification.CREDENTIAL;
        if (isSecretAuditKey(key)) return Classification.SECRET;
        if (isPiiAuditKey(key)) return Classification.PII;
        return Classification.PUBLIC;
    }

    /** 감사 DB의 문자열 Column에 저장할 수 있도록 정제된 Snapshot을 문자열로 변환합니다. */
    public static String sanitizeAuditSnapshotText(Object value) {
        if (value == null) return null;
        Object sanitized = sanitizeAuditSnapshot(value);
        if (sanitized == null) return null;
        return boundText(safeToString(sanitized), MAX_AUDIT_SNAPSHOT_CHARACTERS);
    }

    /** 자유형 오류/직렬화 문자열을 감사 저장 전에 정제합니다. 비어 있는 값은 그대로 허용합니다. */
    public static String sanitizeAuditText(String value) {
        if (value == null) return null;
        String bounded = boundText(value, MAX_AUDIT_TEXT_LENGTH);
        String sanitized = BEARER.matcher(bounded).replaceAll("Bearer [REDACTED]");
        sanitized = AUDIT_SECRET.matcher(sanitized).replaceAll("$1=[REDACTED]");
        sanitized = AUDIT_JSON_SECRET.matcher(sanitized).replaceAll("$1[REDACTED]");
        sanitized = AUDIT_JSON_PII.matcher(sanitized).replaceAll("$1[MASKED]");
        sanitized = KOREAN_RRN.matcher(sanitized).replaceAll("[PII_REDACTED]");
        sanitized = EMAIL_IN_TEXT.matcher(sanitized).replaceAll("[PII_REDACTED]");
        sanitized = PHONE_IN_TEXT.matcher(sanitized).replaceAll("[PII_REDACTED]");
        return sanitized;
    }

    private static Object sanitizeAuditSnapshot(
            Object value, String fieldName, int depth, IdentityHashMap<Object, Boolean> activePath,
            SnapshotBudget budget) {
        if (value == null) return null;
        if (!budget.enterNode()) return TRUNCATED_BUDGET;
        if (depth > MAX_AUDIT_SNAPSHOT_DEPTH) return "[DEPTH_LIMIT]";

        String key = fieldName == null ? "" : normalizeAuditKey(fieldName);
        Classification classification = classifyField(key);
        if (classification == Classification.CREDENTIAL || classification == Classification.SECRET) {
            return "[REDACTED]";
        }
        if (classification == Classification.PII) {
            return maskAuditPii(key, value, budget);
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            if (!budget.consumeCharacters(safeToString(value).length())) return TRUNCATED_BUDGET;
            return value;
        }
        if (value instanceof CharSequence || value instanceof Character) {
            return budget.boundText(sanitizeAuditText(String.valueOf(value)));
        }

        boolean container = value instanceof Map<?, ?> || value instanceof Iterable<?> || value.getClass().isArray();
        if (container && activePath.put(value, Boolean.TRUE) != null) {
            return "[CYCLE]";
        }
        try {
            if (value instanceof Map<?, ?> map) {
                Map<String, Object> copy = new LinkedHashMap<>();
                int count = 0;
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    if (count++ >= MAX_AUDIT_COLLECTION_ITEMS || budget.exhausted()) {
                        copy.put(TRUNCATED_ITEMS, TRUNCATED_BUDGET);
                        break;
                    }
                    String rawName = safeToString(entry.getKey());
                    String outputName = budget.boundText(sanitizeAuditFieldName(rawName));
                    copy.put(outputName, sanitizeAuditSnapshot(
                            entry.getValue(), rawName, depth + 1, activePath, budget));
                }
                return Collections.unmodifiableMap(copy);
            }
            if (value instanceof Iterable<?> iterable) {
                List<Object> copy = new ArrayList<>();
                int count = 0;
                for (Object element : iterable) {
                    if (count++ >= MAX_AUDIT_COLLECTION_ITEMS || budget.exhausted()) {
                        copy.add(TRUNCATED_ITEMS);
                        break;
                    }
                    copy.add(sanitizeAuditSnapshot(element, fieldName, depth + 1, activePath, budget));
                }
                return Collections.unmodifiableList(copy);
            }
            if (value.getClass().isArray()) {
                int length = Array.getLength(value);
                int boundedLength = Math.min(length, MAX_AUDIT_COLLECTION_ITEMS);
                List<Object> copy = new ArrayList<>(boundedLength + (length > boundedLength ? 1 : 0));
                for (int i = 0; i < boundedLength && !budget.exhausted(); i++) {
                    copy.add(sanitizeAuditSnapshot(Array.get(value, i), fieldName, depth + 1, activePath, budget));
                }
                if (length > copy.size() || budget.exhausted()) copy.add(TRUNCATED_ITEMS);
                return Collections.unmodifiableList(copy);
            }
            return budget.boundText(sanitizeAuditText(safeToString(value)));
        } finally {
            if (container) activePath.remove(value);
        }
    }

    private static String sanitizeAuditFieldName(String value) {
        String bounded = boundText(value == null ? "null" : value, MAX_AUDIT_FIELD_NAME_LENGTH);
        return sanitizeAuditText(bounded);
    }

    private static String boundText(String value, int maximumLength) {
        if (value == null) return null;
        if (maximumLength <= 0) return "";
        if (value.length() <= maximumLength) return value;
        if (maximumLength <= TRUNCATED_SUFFIX.length()) {
            return TRUNCATED_SUFFIX.substring(0, maximumLength);
        }
        int prefixLength = maximumLength - TRUNCATED_SUFFIX.length();
        return value.substring(0, prefixLength) + TRUNCATED_SUFFIX;
    }

    private static String safeToString(Object value) {
        try {
            return String.valueOf(value);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (RuntimeException | StackOverflowError failure) {
            return UNREPRESENTABLE;
        }
    }

    private static String normalizeAuditKey(String fieldName) {
        return fieldName.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }

    private static boolean isCredentialAuditKey(String key) {
        return key.contains("password") || key.contains("passwd") || key.equals("pwd")
                || key.contains("token") || key.contains("authorization") || key.contains("apikey")
                || key.contains("credential");
    }

    private static boolean isSecretAuditKey(String key) {
        return key.contains("secret") || key.contains("privatekey") || key.contains("signingkey");
    }

    private static boolean isPiiAuditKey(String key) {
        return key.contains("email") || key.contains("mobile") || key.contains("phone") || key.contains("contact")
                || key.contains("address") || key.contains("resident") || key.equals("rrn") || key.equals("ssn");
    }

    private static String maskAuditPii(String key, Object value, SnapshotBudget budget) {
        if (value instanceof Map<?, ?> || value instanceof Iterable<?> || value.getClass().isArray()) {
            return "[MASKED]";
        }
        String text = blankToNull(boundText(safeToString(value), MAX_AUDIT_TEXT_LENGTH));
        if (text == null) return null;
        String masked;
        if (key.contains("email")) masked = maskEmail(text);
        else if (key.contains("mobile") || key.contains("phone") || key.contains("contact")) masked = maskPhone(text);
        else masked = "[MASKED]";
        return budget.boundText(masked);
    }

    /** SnapshotBudget 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    private static final class SnapshotBudget {
        private int nodes;
        private int characters;

        boolean enterNode() {
            if (nodes >= MAX_AUDIT_SNAPSHOT_NODES) return false;
            nodes++;
            return true;
        }

        boolean consumeCharacters(int count) {
            if (count <= 0) return true;
            if (characters >= MAX_AUDIT_SNAPSHOT_CHARACTERS) return false;
            long next = (long) characters + count;
            characters = (int) Math.min(MAX_AUDIT_SNAPSHOT_CHARACTERS, next);
            return next <= MAX_AUDIT_SNAPSHOT_CHARACTERS;
        }

        String boundText(String value) {
            if (value == null) return null;
            int remaining = MAX_AUDIT_SNAPSHOT_CHARACTERS - characters;
            if (remaining <= 0) return TRUNCATED_BUDGET;
            String bounded = CpfSensitiveData.boundText(value, Math.min(MAX_AUDIT_TEXT_LENGTH, remaining));
            characters += bounded.length();
            return bounded;
        }

        boolean exhausted() {
            return nodes >= MAX_AUDIT_SNAPSHOT_NODES
                    || characters >= MAX_AUDIT_SNAPSHOT_CHARACTERS;
        }
    }

    /** maskPhone 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String maskPhone(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) return null;
        String digits = normalized.replaceAll("\\D", "");
        if (digits.length() <= 4) return "****";
        return "***-****-" + digits.substring(digits.length() - 4);
    }

    /** maskEmail 작업을 CPF 표준 계약에 따라 수행한다. */
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
