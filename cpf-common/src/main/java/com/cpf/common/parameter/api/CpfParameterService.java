package com.cpf.common.parameter.api;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

/** Customer Application이 직접 소비하는 Common Parameter Product API입니다. */
public interface CpfParameterService {
    /** Parameter metadata와 해석된 값을 함께 조회합니다. */
    Optional<CpfParameter> find(String key);

    /** 값이 반드시 존재해야 하는 Parameter를 문자열로 반환합니다. */
    String requiredValue(String key);

    /**
     * 문자열 cast boilerplate 없이 CPF가 지원하는 표준 Type으로 Parameter 값을 조회합니다.
     * 지원 Type은 String, Integer, Long, Boolean, Double, BigDecimal, Duration, LocalDate, LocalDateTime입니다.
     */
    default <T> Optional<T> findValue(String key, Class<T> type) {
        return find(key).map(parameter -> convert(parameter.key(), parameter.value(), type));
    }

    /** Parameter가 없거나 Type 변환에 실패하면 명확한 예외로 실패합니다. */
    default <T> T requiredValue(String key, Class<T> type) {
        return findValue(key, type).orElseThrow(() ->
                new java.util.NoSuchElementException("CPF Common parameter not found: " + key));
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(String key, String raw, Class<T> type) {
        if (type == null) throw new IllegalArgumentException("parameter target type is required");
        if (!isSupportedTargetType(type)) {
            throw new IllegalArgumentException("Unsupported CPF Common parameter target type: " + type.getName());
        }
        if (type == String.class) return (T) raw;
        try {
            Object converted;
            if (type == Integer.class || type == int.class) converted = Integer.valueOf(raw);
            else if (type == Long.class || type == long.class) converted = Long.valueOf(raw);
            else if (type == Boolean.class || type == boolean.class) {
                String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
                if (!normalized.equals("true") && !normalized.equals("false")) {
                    throw new IllegalArgumentException("boolean value must be true or false");
                }
                converted = Boolean.valueOf(normalized);
            }
            else if (type == Double.class || type == double.class) converted = Double.valueOf(raw);
            else if (type == BigDecimal.class) converted = new BigDecimal(raw);
            else if (type == Duration.class) converted = Duration.parse(raw);
            else if (type == LocalDate.class) converted = LocalDate.parse(raw);
            else if (type == LocalDateTime.class) converted = LocalDateTime.parse(raw);
            else throw new IllegalStateException("validated parameter target type has no converter: " + type.getName());
            return (T) converted;
        // 하위 구현 실패를 정상값으로 숨기지 않고 호출자가 실패 의미를 구분할 수 있도록 보존합니다.
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "CPF Common parameter type conversion failed: key=" + key + ", type=" + type.getSimpleName(), ex);
        }
    }

    private static boolean isSupportedTargetType(Class<?> type) {
        return type == String.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class
                || type == Double.class || type == double.class
                || type == BigDecimal.class
                || type == Duration.class
                || type == LocalDate.class
                || type == LocalDateTime.class;
    }
}
