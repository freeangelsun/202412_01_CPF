package com.cpf.common.parameter.api;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * CPF 운영 기능, Batch, Gateway와 Generator가 공유하는 버전형 Parameter Schema입니다.
 *
 * <p>Backend Validation, OpenAPI/Generator Metadata와 ADM 동적 Form이 같은 필드 정의를
 * 사용하도록 자료형·참조형·표시 Metadata·민감도·Override·종속 조건을 하나의 계약으로 제공합니다.</p>
 */
public record CpfParameterSchema(
        String schemaId,
        long schemaVersion,
        String title,
        String description,
        List<ParameterDefinition> parameters) {

    public enum ValueType {
        STRING, INTEGER, LONG, DECIMAL, BOOLEAN, DATE, DATETIME, ENUM, JSON_OBJECT,
        SECRET_REFERENCE, PATH_ALIAS, SERVICE_REFERENCE, FILE_REFERENCE, CODE_REFERENCE
    }

    /** ValueSource 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum ValueSource {
        SAFE_DEFAULT(10), INSTALL_PROPERTY(20), PLATFORM_POLICY(30), ENVIRONMENT_POLICY(40),
        FEATURE_POLICY(50), OPERATION_OVERRIDE(60);
        private final int priority;
        ValueSource(int priority) { this.priority = priority; }
        public int priority() { return priority; }
    }

    public CpfParameterSchema {
        schemaId = requiredToken(schemaId, "schemaId");
        if (schemaVersion <= 0) throw new IllegalArgumentException("schemaVersion must be positive");
        title = requiredText(title, "title");
        description = clean(description);
        parameters = parameters == null ? List.of() : List.copyOf(parameters);
        Set<String> names = new HashSet<>();
        for (ParameterDefinition definition : parameters) {
            if (!names.add(definition.name())) throw new IllegalArgumentException("duplicate parameter: " + definition.name());
        }
    }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
    public ValidationReport validate(Map<String, ?> values) {
        return validateInternal(values, true);
    }

    /**
     * 운영 화면과 API Validation 응답에는 민감 값을 노출하지 않고, Runtime Resolve에서는
     * 실제 Secret 값이 아니라 승인된 Reference Alias를 유지합니다.
     */
    private ValidationReport validateInternal(Map<String, ?> values, boolean redactSensitive) {
        Map<String, ?> supplied = values == null ? Map.of() : values;
        List<FieldError> errors = new ArrayList<>();
        Map<String, Object> normalized = new LinkedHashMap<>();
        for (ParameterDefinition definition : parameters) {
            Object raw = supplied.containsKey(definition.name()) ? supplied.get(definition.name()) : definition.defaultValue();
            if (!definition.visibleWhen().matches(supplied)) continue;
            FieldValidation validation = definition.validate(raw, redactSensitive);
            if (!validation.valid()) errors.add(new FieldError(definition.name(), validation.code(), validation.message()));
            else if (validation.normalizedValue() != null) normalized.put(definition.name(), validation.normalizedValue());
        }
        return new ValidationReport(errors.isEmpty(), List.copyOf(errors), Map.copyOf(normalized));
    }

    /** 우선순위가 높은 Layer가 낮은 Layer를 덮어쓰며 허용되지 않은 실행 Override는 실패시킵니다. */
    public ResolvedSnapshot resolve(List<ValueLayer> layers) {
        return resolve(layers, Clock.systemUTC());
    }

    /** Runtime/테스트가 명시 Clock으로 동일한 resolvedAt을 재현하도록 합니다. */
    public ResolvedSnapshot resolve(List<ValueLayer> layers, Clock clock) {
        Objects.requireNonNull(clock, "clock");
        List<ValueLayer> ordered = layers == null ? List.of() : layers.stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(layer -> layer.source().priority()))
                .toList();
        Map<String, Object> values = new LinkedHashMap<>();
        Map<String, ValueSource> sources = new LinkedHashMap<>();
        for (ParameterDefinition parameter : parameters) {
            if (parameter.defaultValue() != null) {
                values.put(parameter.name(), parameter.defaultValue());
                sources.put(parameter.name(), ValueSource.SAFE_DEFAULT);
            }
        }
        Map<String, ParameterDefinition> definitions = new HashMap<>();
        for (ParameterDefinition parameter : parameters) definitions.put(parameter.name(), parameter);
        for (ValueLayer layer : ordered) {
            for (Map.Entry<String, ?> entry : layer.values().entrySet()) {
                ParameterDefinition definition = definitions.get(entry.getKey());
                if (definition == null) throw new IllegalArgumentException("unknown parameter: " + entry.getKey());
                if (layer.source() == ValueSource.OPERATION_OVERRIDE && !definition.runtimeOverrideAllowed()) {
                    throw new IllegalArgumentException("runtime override is not allowed: " + entry.getKey());
                }
                values.put(entry.getKey(), entry.getValue());
                sources.put(entry.getKey(), layer.source());
            }
        }
        ValidationReport validation = validateInternal(values, false);
        if (!validation.valid()) throw new ParameterValidationException(validation.errors());
        String hash = sha256(canonical(validation.normalizedValues(), sources));
        return new ResolvedSnapshot(schemaId, schemaVersion, validation.normalizedValues(), Map.copyOf(sources), hash, OffsetDateTime.now(clock));
    }

    /** ParameterDefinition 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record ParameterDefinition(
            String name,
            ValueType type,
            String label,
            String description,
            boolean required,
            boolean sensitive,
            boolean identifying,
            boolean runtimeOverrideAllowed,
            Object defaultValue,
            List<String> allowedValues,
            String validationPattern,
            BigDecimal minValue,
            BigDecimal maxValue,
            Integer minLength,
            Integer maxLength,
            String referenceCatalog,
            String placeholder,
            int displayOrder,
            VisibilityCondition visibleWhen) {
        public ParameterDefinition {
            name = requiredToken(name, "name");
            type = Objects.requireNonNull(type, "type");
            label = requiredText(label, "label");
            description = clean(description);
            allowedValues = allowedValues == null ? List.of() : allowedValues.stream().filter(Objects::nonNull).map(String::trim).filter(v -> !v.isEmpty()).distinct().toList();
            validationPattern = clean(validationPattern);
            referenceCatalog = clean(referenceCatalog);
            placeholder = clean(placeholder);
            visibleWhen = visibleWhen == null ? VisibilityCondition.always() : visibleWhen;
            if (!validationPattern.isBlank()) Pattern.compile(validationPattern);
            if (minLength != null && minLength < 0 || maxLength != null && maxLength < 0) throw new IllegalArgumentException("length must be non-negative");
            if (minLength != null && maxLength != null && minLength > maxLength) throw new IllegalArgumentException("minLength > maxLength");
            if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) throw new IllegalArgumentException("minValue > maxValue");
            if (type == ValueType.ENUM && allowedValues.isEmpty()) throw new IllegalArgumentException("ENUM requires allowedValues");
            if (isReference(type) && referenceCatalog.isBlank()) referenceCatalog = type.name();
        }

        FieldValidation validate(Object raw, boolean redactSensitive) {
            if (raw == null || String.valueOf(raw).isBlank()) {
                return required ? FieldValidation.invalid("REQUIRED", label + " 값이 필요합니다.") : FieldValidation.valid(null);
            }
            String text = String.valueOf(raw).trim();
            if (minLength != null && text.length() < minLength) return FieldValidation.invalid("MIN_LENGTH", label + " 최소 길이는 " + minLength + "입니다.");
            if (maxLength != null && text.length() > maxLength) return FieldValidation.invalid("MAX_LENGTH", label + " 최대 길이는 " + maxLength + "입니다.");
            if (!validationPattern.isBlank() && !Pattern.matches(validationPattern, text)) return FieldValidation.invalid("PATTERN", label + " 형식이 올바르지 않습니다.");
            if (!allowedValues.isEmpty() && !allowedValues.contains(text)) return FieldValidation.invalid("NOT_ALLOWED", label + " 허용 값이 아닙니다.");
            try {
                Object normalized = switch (type) {
                    case INTEGER -> Integer.valueOf(text);
                    case LONG -> Long.valueOf(text);
                    case DECIMAL -> new BigDecimal(text);
                    case BOOLEAN -> parseBoolean(text);
                    case DATE -> LocalDate.parse(text);
                    case DATETIME -> OffsetDateTime.parse(text);
                    case JSON_OBJECT -> validateJsonObjectShape(text);
                    default -> text;
                };
                if (normalized instanceof Number number) {
                    BigDecimal decimal = new BigDecimal(number.toString());
                    if (minValue != null && decimal.compareTo(minValue) < 0) return FieldValidation.invalid("MIN_VALUE", label + " 최소값은 " + minValue + "입니다.");
                    if (maxValue != null && decimal.compareTo(maxValue) > 0) return FieldValidation.invalid("MAX_VALUE", label + " 최대값은 " + maxValue + "입니다.");
                }
                if (isReference(type) && !text.matches("[A-Za-z0-9._:/-]{1,300}")) return FieldValidation.invalid("INVALID_REFERENCE", label + " 참조 형식이 올바르지 않습니다.");
                return FieldValidation.valid(sensitive && redactSensitive ? "***" : normalized);
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            } catch (RuntimeException failure) {
                return FieldValidation.invalid("TYPE_MISMATCH", label + " 자료형이 " + type + "과 일치하지 않습니다.");
            }
        }
    }

    public record VisibilityCondition(String parameterName, String operator, List<String> values) {
        public VisibilityCondition {
            parameterName = clean(parameterName);
            operator = clean(operator).isBlank() ? "ALWAYS" : operator.trim().toUpperCase(Locale.ROOT);
            values = values == null ? List.of() : List.copyOf(values);
        }
        /** always 작업을 CPF 표준 계약에 따라 수행한다. */
        public static VisibilityCondition always() { return new VisibilityCondition("", "ALWAYS", List.of()); }
        boolean matches(Map<String, ?> supplied) {
            if ("ALWAYS".equals(operator)) return true;
            String actual = Objects.toString(supplied.get(parameterName), "");
            return switch (operator) {
                case "EQUALS" -> values.contains(actual);
                case "NOT_EQUALS" -> !values.contains(actual);
                case "IN" -> values.contains(actual);
                case "PRESENT" -> !actual.isBlank();
                default -> throw new IllegalArgumentException("unsupported visibility operator: " + operator);
            };
        }
    }

    /** ValueLayer 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record ValueLayer(ValueSource source, Map<String, ?> values) {
        public ValueLayer { source = Objects.requireNonNull(source, "source"); values = values == null ? Map.of() : Map.copyOf(values); }
    }
    public record FieldError(String field, String code, String message) {}
    public record ValidationReport(boolean valid, List<FieldError> errors, Map<String, Object> normalizedValues) {}
    public record ResolvedSnapshot(String schemaId, long schemaVersion, Map<String, Object> values, Map<String, ValueSource> sources, String valueHash, OffsetDateTime resolvedAt) {}
    private record FieldValidation(boolean valid, String code, String message, Object normalizedValue) {
        static FieldValidation valid(Object value) { return new FieldValidation(true, "OK", "", value); }
        static FieldValidation invalid(String code, String message) { return new FieldValidation(false, code, message, null); }
    }
    /** ParameterValidationException 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public static final class ParameterValidationException extends IllegalArgumentException {
        private final List<FieldError> errors;
        public ParameterValidationException(List<FieldError> errors) { super("parameter validation failed: " + errors); this.errors = List.copyOf(errors); }
        public List<FieldError> errors() { return errors; }
    }

    private static boolean isReference(ValueType type) { return switch (type) { case SECRET_REFERENCE, PATH_ALIAS, SERVICE_REFERENCE, FILE_REFERENCE, CODE_REFERENCE -> true; default -> false; }; }
    private static Boolean parseBoolean(String value) {
        if ("Y".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value)) return true;
        if ("N".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) return false;
        throw new IllegalArgumentException("boolean");
    }
    private static String validateJsonObjectShape(String value) {
        String text = value.trim();
        if (!text.startsWith("{") || !text.endsWith("}")) throw new IllegalArgumentException("json object");
        return text;
    }
    private static String canonical(Map<String, Object> values, Map<String, ValueSource> sources) {
        StringBuilder text = new StringBuilder();
        values.keySet().stream().sorted().forEach(key -> text.append(key).append('=').append(values.get(key)).append('@').append(sources.get(key)).append('\n'));
        return text.toString();
    }
    private static String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
    }
    private static String requiredToken(String value, String field) { String text = requiredText(value, field); if (!text.matches("[A-Za-z][A-Za-z0-9._-]{0,119}")) throw new IllegalArgumentException(field + " format invalid"); return text; }
    private static String requiredText(String value, String field) { if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " required"); return value.trim(); }
    private static String clean(String value) { return value == null ? "" : value.trim(); }
}
