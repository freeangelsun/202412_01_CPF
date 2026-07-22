package com.cpf.common.message.fixedlength;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * 고정길이 전문 필드 타입별 문자열 변환과 검증을 담당합니다.
 */
public class FixedLengthTypeConverter {
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public String format(FixedLengthFieldSpec field, Object value) {
        String raw = value == null ? "" : String.valueOf(value).trim();
        if (raw.isBlank()) {
            return raw;
        }
        return switch (field.type()) {
            case DATE -> formatDate(raw);
            case NUMBER -> formatNumber(raw, field);
            case DECIMAL -> formatDecimal(raw, field);
            case BOOLEAN -> formatBoolean(raw, field);
            case STRING -> raw;
        };
    }

    public Optional<FixedLengthMessageError> validate(FixedLengthFieldSpec field, String value) {
        String raw = value == null ? "" : value.trim();
        if (raw.isBlank()) {
            return Optional.empty();
        }
        try {
            format(field, raw);
            return Optional.empty();
        } catch (IllegalArgumentException ex) {
            return Optional.of(new FixedLengthMessageError(field.name(), "FIXED_FIELD_TYPE_INVALID", ex.getMessage()));
        }
    }

    private String formatDate(String value) {
        if (!value.matches("\\d{8}")) {
            throw new IllegalArgumentException("DATE 필드는 yyyyMMdd 형식이어야 합니다. value=" + value);
        }
        try {
            LocalDate.parse(value, BASIC_DATE);
            return value;
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("DATE 필드 값이 유효한 날짜가 아닙니다. value=" + value);
        }
    }

    private String formatNumber(String value, FixedLengthFieldSpec field) {
        if (!value.matches("[+-]?\\d+")) {
            throw new IllegalArgumentException("NUMBER 필드는 정수만 허용합니다. name=" + field.name());
        }
        return value;
    }

    private String formatDecimal(String value, FixedLengthFieldSpec field) {
        try {
            return new BigDecimal(value).toPlainString();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("DECIMAL 필드는 숫자만 허용합니다. name=" + field.name());
        }
    }

    private String formatBoolean(String value, FixedLengthFieldSpec field) {
        String normalized = value.toUpperCase();
        if (!normalized.matches("Y|N|TRUE|FALSE|1|0")) {
            throw new IllegalArgumentException("BOOLEAN 필드는 Y/N, true/false, 1/0만 허용합니다. name=" + field.name());
        }
        return normalized;
    }
}
