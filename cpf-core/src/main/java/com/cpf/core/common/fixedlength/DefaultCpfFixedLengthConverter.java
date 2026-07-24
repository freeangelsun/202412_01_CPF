package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldType;
import com.cpf.core.spi.fixedlength.CpfFixedLengthConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * CPF 표준 문자열·숫자·금액·날짜·시간·불리언 converter입니다.
 */
@Component
public final class DefaultCpfFixedLengthConverter implements CpfFixedLengthConverter {
    public static final String ID = "cpf-default";
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter BASIC_TIME = DateTimeFormatter.ofPattern("HHmmss");

    @Override
    public String id() {
        return ID;
    }

    @Override
    public boolean supports(CpfFixedLengthFieldSpec field) {
        return field.type() != CpfFixedLengthFieldType.CUSTOM
                && (field.converterId().isBlank() || ID.equals(field.converterId()));
    }

    @Override
    public String format(CpfFixedLengthFieldSpec field, Object value) {
        Object effectiveValue = value;
        if (isEmpty(effectiveValue) && !field.defaultValue().isEmpty()) {
            effectiveValue = field.defaultValue();
        }
        if (effectiveValue == null) {
            return "";
        }
        return switch (field.type()) {
            case STRING -> String.valueOf(effectiveValue);
            case NUMBER -> formatNumber(effectiveValue);
            case DECIMAL, AMOUNT -> formatDecimal(effectiveValue, field.scale());
            case DATE -> formatDate(effectiveValue);
            case TIME -> formatTime(effectiveValue);
            case BOOLEAN -> formatBoolean(effectiveValue);
            case CUSTOM -> throw new IllegalArgumentException("CUSTOM 필드는 확장 converter가 필요합니다.");
        };
    }

    @Override
    public Object parse(CpfFixedLengthFieldSpec field, String value) {
        String effectiveValue = value == null ? "" : value;
        if (effectiveValue.isBlank()) {
            if (!field.defaultValue().isEmpty()) {
                effectiveValue = field.defaultValue();
            } else {
                return field.type() == CpfFixedLengthFieldType.STRING ? "" : null;
            }
        }
        String normalized = field.type() == CpfFixedLengthFieldType.STRING
                ? effectiveValue
                : effectiveValue.trim();
        return switch (field.type()) {
            case STRING -> normalized;
            case NUMBER -> new BigInteger(normalized);
            case DECIMAL, AMOUNT -> parseDecimal(normalized, field.scale());
            case DATE -> parseDate(normalized);
            case TIME -> parseTime(normalized);
            case BOOLEAN -> parseBoolean(normalized);
            case CUSTOM -> throw new IllegalArgumentException("CUSTOM 필드는 확장 converter가 필요합니다.");
        };
    }

    private String formatNumber(Object value) {
        if (value instanceof BigInteger bigInteger) {
            return bigInteger.toString();
        }
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) {
            return String.valueOf(value);
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toBigIntegerExact().toString();
        }
        String literal = String.valueOf(value).trim();
        return new BigInteger(literal).toString();
    }

    private String formatDecimal(Object value, int scale) {
        BigDecimal decimal = value instanceof BigDecimal bigDecimal
                ? bigDecimal
                : new BigDecimal(String.valueOf(value).trim());
        if (scale == 0) {
            return decimal.toPlainString();
        }
        return decimal.movePointRight(scale)
                .setScale(0, RoundingMode.UNNECESSARY)
                .toPlainString();
    }

    private BigDecimal parseDecimal(String value, int scale) {
        BigDecimal decimal = new BigDecimal(value);
        return scale > 0 && !value.contains(".") ? decimal.movePointLeft(scale) : decimal;
    }

    private String formatDate(Object value) {
        if (value instanceof LocalDate date) {
            return BASIC_DATE.format(date);
        }
        return BASIC_DATE.format(parseDate(String.valueOf(value).replace("-", "").trim()));
    }

    private LocalDate parseDate(String value) {
        if (!value.matches("\\d{8}")) {
            throw new IllegalArgumentException("DATE 필드는 yyyyMMdd 형식이어야 합니다.");
        }
        try {
            return LocalDate.parse(value, BASIC_DATE);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("DATE 필드 값이 유효한 날짜가 아닙니다.", exception);
        }
    }

    private String formatTime(Object value) {
        if (value instanceof LocalTime time) {
            return BASIC_TIME.format(time);
        }
        return BASIC_TIME.format(parseTime(String.valueOf(value).replace(":", "").trim()));
    }

    private LocalTime parseTime(String value) {
        if (!value.matches("\\d{6}")) {
            throw new IllegalArgumentException("TIME 필드는 HHmmss 형식이어야 합니다.");
        }
        try {
            return LocalTime.parse(value, BASIC_TIME);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("TIME 필드 값이 유효한 시간이 아닙니다.", exception);
        }
    }

    private String formatBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool ? "Y" : "N";
        }
        return parseBoolean(String.valueOf(value)) ? "Y" : "N";
    }

    private Boolean parseBoolean(String value) {
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "Y", "TRUE", "1" -> true;
            case "N", "FALSE", "0" -> false;
            default -> throw new IllegalArgumentException(
                    "BOOLEAN 필드는 Y/N, true/false 또는 1/0만 허용합니다.");
        };
    }

    private boolean isEmpty(Object value) {
        return value == null || value instanceof CharSequence sequence && sequence.toString().isBlank();
    }
}
