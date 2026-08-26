package com.cpf.batch.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Batch Job과 Executor가 함께 사용하는 버전 독립형 Parameter Schema입니다.
 *
 * <p>기존 6개 필드 생성자는 호환을 위해 유지하면서, 운영 화면·OpenAPI·Agent가
 * 동일한 Schema로 Enum, 범위, Pattern, 참조형, Secret, Override 정책을 처리할 수 있게 합니다.</p>
 */
public record BatchParameterDefinition(
        String name,
        String type,
        boolean required,
        String defaultValue,
        boolean identifying,
        boolean sensitive,
        String label,
        String description,
        List<String> allowedValues,
        String pattern,
        String minValue,
        String maxValue,
        Integer minLength,
        Integer maxLength,
        String referenceType,
        String alias,
        boolean overrideAllowed) {

    /** 기존 생성 코드와 Generator 산출물의 Source Compatibility를 보존합니다. */
    public BatchParameterDefinition(
            String name,
            String type,
            boolean required,
            String defaultValue,
            boolean identifying,
            boolean sensitive) {
        this(name, type, required, defaultValue, identifying, sensitive,
                name, "", List.of(), "", "", "", null, null, "", "", true);
    }

    public BatchParameterDefinition {
        name = requireToken(name, "name");
        type = normalizeType(type);
        label = blankTo(label, name);
        description = Objects.requireNonNullElse(description, "").trim();
        allowedValues = allowedValues == null ? List.of() : allowedValues.stream()
                .filter(Objects::nonNull)
                .map(value -> value.trim())
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
        pattern = Objects.requireNonNullElse(pattern, "").trim();
        minValue = Objects.requireNonNullElse(minValue, "").trim();
        maxValue = Objects.requireNonNullElse(maxValue, "").trim();
        referenceType = Objects.requireNonNullElse(referenceType, "").trim().toUpperCase(Locale.ROOT);
        alias = Objects.requireNonNullElse(alias, "").trim();

        if (minLength != null && minLength < 0) {
            throw new IllegalArgumentException("minLength must be >= 0");
        }
        if (maxLength != null && maxLength < 0) {
            throw new IllegalArgumentException("maxLength must be >= 0");
        }
        if (minLength != null && maxLength != null && minLength > maxLength) {
            throw new IllegalArgumentException("minLength must be <= maxLength");
        }
        if (!pattern.isEmpty()) {
            Pattern.compile(pattern);
        }
        if ("ENUM".equals(type) && allowedValues.isEmpty()) {
            throw new IllegalArgumentException("ENUM parameter requires allowedValues");
        }
        if (isReferenceType(type) && referenceType.isEmpty()) {
            referenceType = type;
        }
        validateNumericBounds(type, minValue, maxValue);
    }

    /**
     * 전달 전 값을 검증합니다. Secret 원문은 반환 메시지에 포함하지 않습니다.
     */
    public ValidationResult validate(String rawValue) {
        String value = rawValue == null ? defaultValue : rawValue;
        if (value == null || value.isBlank()) {
            return required
                    ? ValidationResult.invalid(name, "REQUIRED", label + " 값이 필요합니다.")
                    : ValidationResult.valid(name, value);
        }

        if (minLength != null && value.length() < minLength) {
            return ValidationResult.invalid(name, "MIN_LENGTH", label + " 최소 길이는 " + minLength + "입니다.");
        }
        if (maxLength != null && value.length() > maxLength) {
            return ValidationResult.invalid(name, "MAX_LENGTH", label + " 최대 길이는 " + maxLength + "입니다.");
        }
        if (!pattern.isEmpty() && !Pattern.matches(pattern, value)) {
            return ValidationResult.invalid(name, "PATTERN", label + " 형식이 올바르지 않습니다.");
        }
        if (!allowedValues.isEmpty() && allowedValues.stream().noneMatch(value::equals)) {
            return ValidationResult.invalid(name, "NOT_ALLOWED", label + " 허용 값이 아닙니다.");
        }

        try {
            switch (type) {
                case "INTEGER" -> Integer.parseInt(value);
                case "LONG" -> Long.parseLong(value);
                case "DECIMAL" -> validateRange(new BigDecimal(value));
                case "BOOLEAN" -> {
                    if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)
                            && !"Y".equalsIgnoreCase(value) && !"N".equalsIgnoreCase(value)) {
                        throw new IllegalArgumentException("boolean");
                    }
                }
                case "DATE" -> LocalDate.parse(value);
                case "DATETIME" -> OffsetDateTime.parse(value);
                case "SECRET_REFERENCE" -> {
                    if (!value.matches("[A-Za-z0-9._:/-]{3,200}")) {
                        return ValidationResult.invalid(name, "INVALID_SECRET_REFERENCE", "Secret Reference 형식이 올바르지 않습니다.");
                    }
                }
                case "PATH_ALIAS", "SERVICE_REFERENCE", "FILE_REFERENCE" -> {
                    if (!value.matches("[A-Za-z0-9._:/-]{1,300}")) {
                        return ValidationResult.invalid(name, "INVALID_REFERENCE", label + " 참조 형식이 올바르지 않습니다.");
                    }
                }
                default -> {
                    // STRING, ENUM, JSON_OBJECT와 확장형은 공통 제약으로 검증합니다.
                }
            }
            if ("INTEGER".equals(type) || "LONG".equals(type)) {
                validateRange(new BigDecimal(value));
            }
        } catch (NumberFormatException | DateTimeParseException failure) {
            return ValidationResult.invalid(name, "TYPE_MISMATCH", label + " 자료형이 " + type + "과 일치하지 않습니다.");
        } catch (IllegalArgumentException failure) {
            return ValidationResult.invalid(name, "TYPE_MISMATCH", label + " 자료형이 " + type + "과 일치하지 않습니다.");
        }
        return ValidationResult.valid(name, sensitive ? "***" : value);
    }

    public String effectiveValue(String suppliedValue) {
        return suppliedValue == null || suppliedValue.isBlank() ? defaultValue : suppliedValue;
    }

    public boolean referenceParameter() {
        return isReferenceType(type);
    }

    private void validateRange(BigDecimal value) {
        if (!minValue.isEmpty() && value.compareTo(new BigDecimal(minValue)) < 0) {
            throw new IllegalArgumentException("below min");
        }
        if (!maxValue.isEmpty() && value.compareTo(new BigDecimal(maxValue)) > 0) {
            throw new IllegalArgumentException("above max");
        }
    }

    private static void validateNumericBounds(String type, String minValue, String maxValue) {
        if (!("INTEGER".equals(type) || "LONG".equals(type) || "DECIMAL".equals(type))) {
            return;
        }
        BigDecimal min = minValue.isEmpty() ? null : new BigDecimal(minValue);
        BigDecimal max = maxValue.isEmpty() ? null : new BigDecimal(maxValue);
        if (min != null && max != null && min.compareTo(max) > 0) {
            throw new IllegalArgumentException("minValue must be <= maxValue");
        }
    }

    private static String normalizeType(String type) {
        String normalized = requireToken(type, "type").toUpperCase(Locale.ROOT);
        String canonical = switch (normalized) {
            case "INT" -> "INTEGER";
            case "NUMBER", "BIGDECIMAL" -> "DECIMAL";
            case "SECRET" -> "SECRET_REFERENCE";
            default -> normalized;
        };
        if (!Set.of("STRING", "INTEGER", "LONG", "DECIMAL", "BOOLEAN", "DATE", "DATETIME", "ENUM",
                "JSON_OBJECT", "SECRET_REFERENCE", "PATH_ALIAS", "SERVICE_REFERENCE", "FILE_REFERENCE",
                "CODE_REFERENCE").contains(canonical)) {
            throw new IllegalArgumentException("unsupported parameter type: " + canonical);
        }
        return canonical;
    }

    private static boolean isReferenceType(String type) {
        return "SECRET_REFERENCE".equals(type)
                || "PATH_ALIAS".equals(type)
                || "SERVICE_REFERENCE".equals(type)
                || "FILE_REFERENCE".equals(type)
                || "CODE_REFERENCE".equals(type);
    }

    private static String requireToken(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        String normalized = value.trim();
        if (!normalized.matches("[A-Za-z][A-Za-z0-9._-]{0,99}")) {
            throw new IllegalArgumentException(field + " has invalid format");
        }
        return normalized;
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    /** Batch Parameter 값 검증의 성공 여부와 오류 사유를 반환하는 결과 계약입니다. */
    public record ValidationResult(String name, boolean valid, String code, String message, String normalizedValue) {
        public static ValidationResult valid(String name, String normalizedValue) {
            return new ValidationResult(name, true, "OK", "", normalizedValue);
        }

        public static ValidationResult invalid(String name, String code, String message) {
            return new ValidationResult(name, false, code, message, null);
        }
    }
}
