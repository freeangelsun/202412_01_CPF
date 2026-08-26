package com.cpf.admin.opr.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * ADM 운영 API가 로그 원문을 그대로 외부에 노출하지 않도록 하는 Backend 최종 마스킹 계층입니다.
 *
 * <p>Frontend 마스킹에 의존하지 않으며 Header/Cookie/JWT/개인 식별값을 Key와 Value 양쪽에서 방어합니다.</p>
 */
final class AdmLogSanitizer {
    private static final Pattern SECRET_PAIR = Pattern.compile(
            "(?i)(password|passwd|pwd|secret|token|authorization|proxy-authorization|cookie|set-cookie|api[-_]?key|member[-_]?no|customer[-_]?no|account[-_]?no|mobile[-_]?no|phone|email)"
                    + "(\\\"?\\s*[:=]\\s*\\\"?)[^,\\\"}\\r\\n]+");
    private static final Pattern BEARER = Pattern.compile("(?i)Bearer\\s+[A-Za-z0-9._~+/-]+=*");
    private static final Pattern JWT = Pattern.compile(
            "(?<![A-Za-z0-9_-])[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}\\.[A-Za-z0-9_-]{8,}(?![A-Za-z0-9_-])");
    private static final Pattern PHONE = Pattern.compile("(?<!\\d)(\\d{3})-?(\\d{3,4})-?(\\d{4})(?!\\d)");
    private static final Pattern EMAIL = Pattern.compile(
            "([A-Za-z0-9._%+-]{2})[A-Za-z0-9._%+-]*(@[A-Za-z0-9.-]+)");

    private AdmLogSanitizer() {
    }

    static Map<String, Object> sanitizeMap(Map<String, Object> source) {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) sanitizeStructure(source, null);
        return result;
    }

    static Object sanitizeStructure(Object value, String key) {
        if (value == null) return null;
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> result = new LinkedHashMap<>();
            map.forEach((entryKey, entryValue) -> {
                String childKey = String.valueOf(entryKey);
                result.put(childKey, sanitizeStructure(entryValue, childKey));
            });
            return result;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> result = new ArrayList<>();
            for (Object item : iterable) result.add(sanitizeStructure(item, key));
            return result;
        }
        if (value.getClass().isArray()) {
            return sanitizeText(String.valueOf(value));
        }
        if (isAlwaysSecretKey(key)) return "****";
        String text = String.valueOf(value);
        if (isIdentityKey(key)) return maskIdentifier(text);
        return sanitizeText(text);
    }

    static String sanitizeJson(ObjectMapper objectMapper, String value) {
        if (value == null || value.isBlank()) return "";
        try {
            Object parsed = objectMapper.readValue(value, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(sanitizeStructure(parsed, null));
        } catch (JsonProcessingException ex) {
            return sanitizeText(value);
        }
    }

    static String sanitizeText(String value) {
        if (value == null) return "";
        String masked = SECRET_PAIR.matcher(value).replaceAll("$1$2****");
        masked = BEARER.matcher(masked).replaceAll("Bearer ****");
        masked = JWT.matcher(masked).replaceAll("****.****.****");
        masked = PHONE.matcher(masked).replaceAll("$1-****-$3");
        masked = EMAIL.matcher(masked).replaceAll("$1****$2");
        return masked;
    }

    private static boolean isAlwaysSecretKey(String key) {
        if (key == null) return false;
        String normalized = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("authorization")
                || normalized.contains("cookie")
                || normalized.contains("apikey");
    }

    private static boolean isIdentityKey(String key) {
        if (key == null) return false;
        String normalized = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return normalized.contains("memberno")
                || normalized.contains("customerno")
                || normalized.contains("accountno")
                || normalized.contains("mobile")
                || normalized.contains("phone")
                || normalized.contains("email");
    }

    private static String maskIdentifier(String value) {
        if (value == null || value.isBlank()) return "";
        String text = sanitizeText(value);
        if (text.contains("@")) return EMAIL.matcher(text).replaceAll("$1****$2");
        if (text.length() <= 4) return "****";
        int visible = Math.min(4, text.length());
        return "*".repeat(Math.max(4, text.length() - visible)) + text.substring(text.length() - visible);
    }
}
