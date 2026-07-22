package com.cpf.common.tlm.service;

import com.cpf.common.mqe.service.CmnMessageCodec;
import com.cpf.common.tlm.core.CmnTelegramAlign;
import com.cpf.common.tlm.core.CmnTelegramField;
import com.cpf.common.tlm.core.CmnTelegramFieldSpec;
import com.cpf.common.tlm.core.CmnTelegramFieldType;
import com.cpf.common.tlm.core.CmnTelegramParseResult;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfSystemException;
import com.cpf.core.common.exception.CpfValidationException;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 고정길이 전문과 DTO·Map·JSON 사이의 양방향 변환을 제공합니다.
 *
 * <p>스키마 길이, 필드 순서와 이름 중복, 자료형 변환을 검증하고 오류 위치를
 * 필드 단위로 알려 운영 전문 분석과 EDU 샘플에서 같은 규칙을 사용할 수 있게 합니다.</p>
 */
@Service
public class CmnTelegramService extends com.cpf.common.common.base.CmnBaseService {
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final CmnMessageCodec codec;

    public CmnTelegramService(CmnMessageCodec codec) {
        this.codec = codec;
    }

    /** 전문 문자열을 스키마에 따라 원문 필드와 타입 필드 Map으로 분해합니다. */
    public CmnTelegramParseResult parseToMap(String telegram, List<CmnTelegramFieldSpec> schema) {
        List<CmnTelegramFieldSpec> fields = sortSchema(schema);
        int expectedLength = expectedLength(fields);
        int actualLength = telegram == null ? 0 : telegram.length();
        List<String> warnings = new ArrayList<>();

        String normalizedTelegram = telegram == null ? "" : telegram;
        if (actualLength < expectedLength) {
            warnings.add("전문 길이가 스키마보다 짧아 부족한 구간을 공백으로 채웠습니다. actual="
                    + actualLength + ", expected=" + expectedLength);
            normalizedTelegram = normalizedTelegram + " ".repeat(expectedLength - actualLength);
        }
        if (actualLength > expectedLength) {
            warnings.add("전문 길이가 스키마보다 길어 초과 구간을 __remaining에 보관했습니다. actual="
                    + actualLength + ", expected=" + expectedLength);
        }

        int offset = 0;
        Map<String, String> rawFields = new LinkedHashMap<>();
        Map<String, Object> typedFields = new LinkedHashMap<>();
        for (CmnTelegramFieldSpec field : fields) {
            String raw = normalizedTelegram.substring(offset, offset + field.length());
            rawFields.put(field.name(), raw);
            typedFields.put(field.name(), parseValue(raw, field));
            offset += field.length();
        }

        if (actualLength > expectedLength) {
            rawFields.put("__remaining", telegram.substring(expectedLength));
            typedFields.put("__remaining", telegram.substring(expectedLength));
        }

        return new CmnTelegramParseResult(
                telegram,
                expectedLength,
                actualLength,
                rawFields,
                typedFields,
                codec.toJson(typedFields),
                List.copyOf(warnings));
    }

    /** DTO의 필드 선언을 스키마로 사용해 전문 문자열을 DTO로 변환합니다. */
    public <T> T parseToDto(String telegram, Class<T> dtoType) {
        CmnTelegramParseResult result = parseToMap(telegram, schemaFromDto(dtoType));
        return mapToDto(result.typedFields(), dtoType);
    }

    /** Map 값을 스키마 순서와 고정 길이에 맞춘 전문 문자열로 생성합니다. */
    public String writeFromMap(Map<String, ?> values, List<CmnTelegramFieldSpec> schema) {
        StringBuilder builder = new StringBuilder();
        for (CmnTelegramFieldSpec field : sortSchema(schema)) {
            builder.append(formatValue(values == null ? null : values.get(field.name()), field));
        }
        return builder.toString();
    }

    /** DTO 값을 어노테이션 스키마에 맞춘 전문 문자열로 생성합니다. */
    public String writeFromDto(Object dto) {
        if (dto == null) {
            throw new CpfValidationException("전문으로 변환할 DTO는 null일 수 없습니다.");
        }
        return writeFromMap(valuesFromDto(dto), schemaFromDto(dto.getClass()));
    }

    /** record component 또는 일반 필드의 어노테이션에서 전문 스키마를 생성합니다. */
    public List<CmnTelegramFieldSpec> schemaFromDto(Class<?> dtoType) {
        if (dtoType == null) {
            throw new CpfValidationException("전문 스키마를 생성할 DTO 타입은 null일 수 없습니다.");
        }
        List<CmnTelegramFieldSpec> schema = new ArrayList<>();

        if (dtoType.isRecord()) {
            for (RecordComponent component : dtoType.getRecordComponents()) {
                CmnTelegramField annotation = component.getAnnotation(CmnTelegramField.class);
                if (annotation == null) {
                    annotation = annotationFromField(dtoType, component.getName());
                }
                if (annotation != null) {
                    schema.add(toSpec(component.getName(), annotation));
                }
            }
            return sortSchema(schema);
        }

        for (Field field : dtoType.getDeclaredFields()) {
            CmnTelegramField annotation = field.getAnnotation(CmnTelegramField.class);
            if (annotation != null) {
                schema.add(toSpec(field.getName(), annotation));
            }
        }
        return sortSchema(schema);
    }

    private Object parseValue(String raw, CmnTelegramFieldSpec field) {
        String value = field.trim() ? trimPadding(raw, field.resolvedPadding()) : raw;
        if (!TextUtils.hasText(value)) {
            value = defaultLiteral(field);
        }

        try {
            return switch (field.type()) {
                case STRING -> value;
                case NUMBER -> Long.parseLong(TextUtils.defaultIfBlank(value, "0"));
                case DECIMAL -> parseDecimal(value, field.scale());
                case BOOLEAN -> parseBoolean(value);
                case DATE -> TextUtils.hasText(value) ? LocalDate.parse(value.replace("-", ""), BASIC_DATE) : null;
            };
        } catch (RuntimeException ex) {
            throw new CpfValidationException("전문 필드 값을 지정한 자료형으로 변환하지 못했습니다. field="
                    + field.name() + ", value=" + raw + ", type=" + field.type());
        }
    }

    private String formatValue(Object value, CmnTelegramFieldSpec field) {
        String raw = switch (field.type()) {
            case STRING -> value == null ? defaultLiteral(field) : value.toString();
            case NUMBER -> numberLiteral(value, field);
            case DECIMAL -> decimalLiteral(value, field);
            case BOOLEAN -> booleanLiteral(value, field);
            case DATE -> dateLiteral(value, field);
        };

        if (raw.length() > field.length()) {
            throw new CpfValidationException("전문 필드 값이 고정 길이를 초과했습니다. field=" + field.name()
                    + ", length=" + field.length() + ", valueLength=" + raw.length());
        }

        int padCount = field.length() - raw.length();
        String padding = String.valueOf(field.resolvedPadding()).repeat(padCount);
        return field.resolvedAlign() == CmnTelegramAlign.RIGHT ? padding + raw : raw + padding;
    }

    private String trimPadding(String raw, char padding) {
        if (raw == null) {
            return "";
        }
        String value = raw.strip();
        if (padding == ' ') {
            return value;
        }
        int start = 0;
        int end = value.length();
        while (start < end && value.charAt(start) == padding) {
            start++;
        }
        while (end > start && value.charAt(end - 1) == padding) {
            end--;
        }
        return value.substring(start, end);
    }

    private BigDecimal parseDecimal(String value, int scale) {
        if (!TextUtils.hasText(value)) {
            return BigDecimal.ZERO;
        }
        if (value.contains(".")) {
            return new BigDecimal(value);
        }
        BigDecimal decimal = new BigDecimal(value);
        return scale > 0 ? decimal.movePointLeft(scale) : decimal;
    }

    private Boolean parseBoolean(String value) {
        String normalized = TextUtils.normalizeCode(value);
        return "Y".equals(normalized) || "1".equals(normalized) || "TRUE".equals(normalized);
    }

    private String numberLiteral(Object value, CmnTelegramFieldSpec field) {
        if (value == null || !TextUtils.hasText(value.toString())) {
            return defaultLiteral(field);
        }
        if (value instanceof Number number) {
            return String.valueOf(number.longValue());
        }
        return value.toString().trim();
    }

    private String decimalLiteral(Object value, CmnTelegramFieldSpec field) {
        if (value == null || !TextUtils.hasText(value.toString())) {
            return defaultLiteral(field);
        }
        BigDecimal decimal = value instanceof BigDecimal bigDecimal
                ? bigDecimal
                : new BigDecimal(value.toString());
        return field.scale() > 0
                ? decimal.movePointRight(field.scale()).setScale(0).toPlainString()
                : decimal.toPlainString();
    }

    private String booleanLiteral(Object value, CmnTelegramFieldSpec field) {
        if (value == null || !TextUtils.hasText(value.toString())) {
            return defaultLiteral(field);
        }
        if (value instanceof Boolean bool) {
            return bool ? "Y" : "N";
        }
        return parseBoolean(value.toString()) ? "Y" : "N";
    }

    private String dateLiteral(Object value, CmnTelegramFieldSpec field) {
        if (value == null || !TextUtils.hasText(value.toString())) {
            return defaultLiteral(field);
        }
        if (value instanceof LocalDate date) {
            return BASIC_DATE.format(date);
        }
        return value.toString().replace("-", "");
    }

    private String defaultLiteral(CmnTelegramFieldSpec field) {
        if (TextUtils.hasText(field.defaultValue())) {
            return field.defaultValue();
        }
        return switch (field.type()) {
            case STRING, DATE -> "";
            case NUMBER, DECIMAL -> "0";
            case BOOLEAN -> "N";
        };
    }

    private List<CmnTelegramFieldSpec> sortSchema(List<CmnTelegramFieldSpec> schema) {
        if (schema == null || schema.isEmpty()) {
            throw new CpfValidationException("전문 스키마에는 하나 이상의 필드가 필요합니다.");
        }
        if (schema.stream().anyMatch(field -> field == null)) {
            throw new CpfValidationException("전문 스키마에 null 필드를 포함할 수 없습니다.");
        }
        List<CmnTelegramFieldSpec> sorted = schema.stream()
                .sorted(Comparator.comparingInt(CmnTelegramFieldSpec::order))
                .toList();
        Set<Integer> orders = new HashSet<>();
        Set<String> names = new HashSet<>();
        for (CmnTelegramFieldSpec field : sorted) {
            if (!orders.add(field.order())) {
                throw new CpfValidationException("전문 필드 순서가 중복되었습니다. order=" + field.order());
            }
            if (!names.add(field.name())) {
                throw new CpfValidationException("전문 필드명이 중복되었습니다. field=" + field.name());
            }
        }
        return sorted;
    }

    private int expectedLength(List<CmnTelegramFieldSpec> fields) {
        return fields.stream().mapToInt(CmnTelegramFieldSpec::length).sum();
    }

    private CmnTelegramFieldSpec toSpec(String javaFieldName, CmnTelegramField annotation) {
        String name = TextUtils.hasText(annotation.name()) ? annotation.name() : javaFieldName;
        return new CmnTelegramFieldSpec(
                name,
                annotation.order(),
                annotation.length(),
                annotation.type(),
                annotation.align(),
                annotation.padding(),
                annotation.defaultValue(),
                annotation.scale(),
                annotation.trim());
    }

    private CmnTelegramField annotationFromField(Class<?> dtoType, String fieldName) {
        try {
            Field field = dtoType.getDeclaredField(fieldName);
            return field.getAnnotation(CmnTelegramField.class);
        } catch (NoSuchFieldException ex) {
            return null;
        }
    }

    private Map<String, Object> valuesFromDto(Object dto) {
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            if (dto.getClass().isRecord()) {
                for (RecordComponent component : dto.getClass().getRecordComponents()) {
                    if (component.getAnnotation(CmnTelegramField.class) != null
                            || annotationFromField(dto.getClass(), component.getName()) != null) {
                        values.put(component.getName(), component.getAccessor().invoke(dto));
                    }
                }
                return values;
            }

            for (Field field : dto.getClass().getDeclaredFields()) {
                if (field.getAnnotation(CmnTelegramField.class) != null) {
                    field.setAccessible(true);
                    values.put(field.getName(), field.get(dto));
                }
            }
            return values;
        } catch (ReflectiveOperationException ex) {
            throw new CpfSystemException("전문 DTO의 필드 값을 읽지 못했습니다. type="
                    + dto.getClass().getName(), ex);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T mapToDto(Map<String, Object> values, Class<T> dtoType) {
        try {
            if (dtoType.isRecord()) {
                RecordComponent[] components = dtoType.getRecordComponents();
                Class<?>[] parameterTypes = Arrays.stream(components)
                        .map(RecordComponent::getType)
                        .toArray(Class<?>[]::new);
                Object[] arguments = Arrays.stream(components)
                        .map(component -> convertToTarget(values.get(component.getName()), component.getType()))
                        .toArray();
                Constructor<T> constructor = dtoType.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
                return constructor.newInstance(arguments);
            }

            T instance = dtoType.getDeclaredConstructor().newInstance();
            for (Field field : dtoType.getDeclaredFields()) {
                CmnTelegramField annotation = field.getAnnotation(CmnTelegramField.class);
                if (annotation != null) {
                    field.setAccessible(true);
                    field.set(instance, convertToTarget(values.get(field.getName()), field.getType()));
                }
            }
            return instance;
        } catch (ReflectiveOperationException ex) {
            throw new CpfSystemException("전문 필드 값을 DTO로 변환하지 못했습니다. type="
                    + dtoType.getName(), ex);
        }
    }

    private Object convertToTarget(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }
        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }
        if (targetType == String.class) {
            return value.toString();
        }
        if (targetType == Long.class || targetType == long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == Integer.class || targetType == int.class) {
            return ((Number) value).intValue();
        }
        if (targetType == BigDecimal.class) {
            return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return value instanceof Boolean bool ? bool : parseBoolean(value.toString());
        }
        if (targetType == LocalDate.class) {
            return value instanceof LocalDate date ? date : LocalDate.parse(value.toString().replace("-", ""), BASIC_DATE);
        }
        return value;
    }
}

