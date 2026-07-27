package com.cpf.core.api.security;

import com.cpf.core.api.error.CpfValidationException;

import java.util.Locale;
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
    private static final Pattern PHONE = Pattern.compile("^[+0-9() .\\-xX#]*$");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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
