package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthDtoMapper;
import com.cpf.core.api.fixedlength.CpfFixedLengthError;
import com.cpf.core.api.fixedlength.CpfFixedLengthException;
import com.cpf.core.api.fixedlength.CpfFixedLengthField;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import com.cpf.core.api.fixedlength.CpfFixedLengthParseResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthParser;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriteResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 어노테이션 metadata를 ClassValue에 안전하게 캐시하는 CPF 기본 DTO mapper입니다.
 */
@Component
public final class DefaultCpfFixedLengthDtoMapper implements CpfFixedLengthDtoMapper {
    private final CpfFixedLengthParser parser;
    private final CpfFixedLengthWriter writer;
    private final ClassValue<DtoMetadata> metadata = new ClassValue<>() {
        @Override
        protected DtoMetadata computeValue(Class<?> type) {
            return inspect(type);
        }
    };

    public DefaultCpfFixedLengthDtoMapper() {
        this(new DefaultCpfFixedLengthParser(), new DefaultCpfFixedLengthWriter());
    }

    @Autowired
    public DefaultCpfFixedLengthDtoMapper(
            CpfFixedLengthParser parser,
            CpfFixedLengthWriter writer) {
        this.parser = parser;
        this.writer = writer;
    }

    @Override
    public CpfFixedLengthLayout layoutFromDto(Class<?> dtoType) {
        return layoutFromDto(dtoType, StandardCharsets.UTF_8);
    }

    @Override
    public CpfFixedLengthLayout layoutFromDto(Class<?> dtoType, Charset charset) {
        requireDtoType(dtoType);
        Charset effectiveCharset = charset == null ? StandardCharsets.UTF_8 : charset;
        return metadata.get(dtoType).layout(effectiveCharset);
    }

    @Override
    public CpfFixedLengthWriteResult writeFromDto(Object dto) {
        return writeFromDto(dto, StandardCharsets.UTF_8);
    }

    @Override
    public CpfFixedLengthWriteResult writeFromDto(Object dto, Charset charset) {
        if (dto == null) {
            throw new CpfFixedLengthException(
                    "고정길이 전문으로 변환할 DTO가 없습니다.",
                    List.of(new CpfFixedLengthError(
                            "_dto",
                            "CPF_FIXED_DTO_REQUIRED",
                            "고정길이 전문으로 변환할 DTO는 필수입니다.")));
        }
        DtoMetadata dtoMetadata = metadata.get(dto.getClass());
        return writer.write(
                valuesFromDto(dto, dtoMetadata),
                dtoMetadata.layout(charset == null ? StandardCharsets.UTF_8 : charset));
    }

    @Override
    public CpfFixedLengthParseResult parseToMap(String message, Class<?> dtoType) {
        return parseToMap(message, dtoType, StandardCharsets.UTF_8);
    }

    @Override
    public CpfFixedLengthParseResult parseToMap(
            String message,
            Class<?> dtoType,
            Charset charset) {
        return parser.parse(message, layoutFromDto(dtoType, charset));
    }

    @Override
    public <T> T parseToDto(String message, Class<T> dtoType) {
        return parseToDto(message, dtoType, StandardCharsets.UTF_8);
    }

    @Override
    public <T> T parseToDto(String message, Class<T> dtoType, Charset charset) {
        CpfFixedLengthParseResult result = parseToMap(message, dtoType, charset);
        if (!result.valid()) {
            throw new CpfFixedLengthException("고정길이 전문을 DTO로 변환하지 못했습니다.", result.errors());
        }
        return createDto(result.typedFields(), dtoType, metadata.get(dtoType));
    }

    private DtoMetadata inspect(Class<?> dtoType) {
        requireDtoType(dtoType);
        List<FieldBinding> bindings = dtoType.isRecord()
                ? inspectRecord(dtoType)
                : inspectClass(dtoType);
        if (bindings.isEmpty()) {
            throw new IllegalArgumentException(
                    "DTO에 @CpfFixedLengthField가 하나 이상 필요합니다. type=" + dtoType.getName());
        }
        List<FieldBinding> sorted = bindings.stream()
                .sorted(Comparator.comparingInt(FieldBinding::order))
                .toList();
        Set<Integer> orders = new HashSet<>();
        Set<String> wireNames = new HashSet<>();
        for (FieldBinding binding : sorted) {
            if (!orders.add(binding.order())) {
                throw new IllegalArgumentException(
                        "고정길이 DTO 필드 순서가 중복되었습니다. order=" + binding.order());
            }
            if (!wireNames.add(binding.wireName())) {
                throw new IllegalArgumentException(
                        "고정길이 DTO wire 필드명이 중복되었습니다. field=" + binding.wireName());
            }
        }
        return new DtoMetadata(dtoType, sorted);
    }

    private List<FieldBinding> inspectRecord(Class<?> dtoType) {
        List<FieldBinding> bindings = new ArrayList<>();
        for (RecordComponent component : dtoType.getRecordComponents()) {
            CpfFixedLengthField annotation = component.getAnnotation(CpfFixedLengthField.class);
            Field backingField = findField(dtoType, component.getName());
            if (annotation == null && backingField != null) {
                annotation = backingField.getAnnotation(CpfFixedLengthField.class);
            }
            if (annotation == null) {
                throw new IllegalArgumentException(
                        "record의 모든 component에 @CpfFixedLengthField가 필요합니다. type="
                                + dtoType.getName() + ", component=" + component.getName());
            }
            Method accessor = component.getAccessor();
            accessor.setAccessible(true);
            bindings.add(toBinding(
                    component.getName(),
                    component.getType(),
                    annotation,
                    accessor,
                    backingField));
        }
        return bindings;
    }

    private List<FieldBinding> inspectClass(Class<?> dtoType) {
        List<FieldBinding> bindings = new ArrayList<>();
        for (Field field : dtoType.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            CpfFixedLengthField annotation = field.getAnnotation(CpfFixedLengthField.class);
            if (annotation != null) {
                field.setAccessible(true);
                bindings.add(toBinding(field.getName(), field.getType(), annotation, null, field));
            }
        }
        return bindings;
    }

    private FieldBinding toBinding(
            String javaName,
            Class<?> javaType,
            CpfFixedLengthField annotation,
            Method accessor,
            Field field) {
        if (annotation.order() < 1) {
            throw new IllegalArgumentException(
                    "고정길이 DTO 필드 순서는 1 이상이어야 합니다. field=" + javaName);
        }
        if (annotation.length() < 1) {
            throw new IllegalArgumentException(
                    "고정길이 DTO 필드 byte 길이는 1 이상이어야 합니다. field=" + javaName);
        }
        String wireName = annotation.name().isBlank() ? javaName : annotation.name().trim();
        return new FieldBinding(
                javaName,
                wireName,
                javaType,
                annotation.order(),
                annotation.length(),
                annotation,
                accessor,
                field);
    }

    private Map<String, Object> valuesFromDto(Object dto, DtoMetadata dtoMetadata) {
        Map<String, Object> values = new LinkedHashMap<>();
        try {
            for (FieldBinding binding : dtoMetadata.bindings()) {
                Object value = binding.accessor() != null
                        ? binding.accessor().invoke(dto)
                        : binding.field().get(dto);
                values.put(binding.wireName(), value);
            }
            return values;
        } catch (ReflectiveOperationException exception) {
            throw new CpfFixedLengthException(
                    "고정길이 DTO 필드 값을 읽지 못했습니다.",
                    exception,
                    List.of(new CpfFixedLengthError(
                            "_dto",
                            "CPF_FIXED_DTO_READ_FAILED",
                            "DTO 필드 접근에 실패했습니다.")));
        }
    }

    private <T> T createDto(
            Map<String, Object> values,
            Class<T> dtoType,
            DtoMetadata dtoMetadata) {
        try {
            if (dtoType.isRecord()) {
                RecordComponent[] components = dtoType.getRecordComponents();
                Class<?>[] parameterTypes = new Class<?>[components.length];
                Object[] arguments = new Object[components.length];
                Map<String, FieldBinding> bindingByJavaName = dtoMetadata.bindingByJavaName();
                for (int index = 0; index < components.length; index++) {
                    RecordComponent component = components[index];
                    FieldBinding binding = bindingByJavaName.get(component.getName());
                    parameterTypes[index] = component.getType();
                    arguments[index] = convertToTarget(
                            values.get(binding.wireName()),
                            component.getType());
                }
                Constructor<T> constructor = dtoType.getDeclaredConstructor(parameterTypes);
                constructor.setAccessible(true);
                return constructor.newInstance(arguments);
            }

            Constructor<T> constructor = dtoType.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            for (FieldBinding binding : dtoMetadata.bindings()) {
                if (Modifier.isFinal(binding.field().getModifiers())) {
                    throw new IllegalArgumentException(
                            "일반 DTO의 고정길이 대상 필드는 final일 수 없습니다. field=" + binding.javaName());
                }
                binding.field().set(
                        instance,
                        convertToTarget(values.get(binding.wireName()), binding.javaType()));
            }
            return instance;
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            throw new CpfFixedLengthException(
                    "고정길이 필드 값을 DTO로 변환하지 못했습니다.",
                    exception,
                    List.of(new CpfFixedLengthError(
                            "_dto",
                            "CPF_FIXED_DTO_CREATE_FAILED",
                            "DTO 생성 또는 필드 대입에 실패했습니다.")));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object convertToTarget(Object value, Class<?> targetType) {
        if (value == null) {
            return primitiveDefault(targetType);
        }
        if (targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class) {
            return String.valueOf(value);
        }
        if (targetType == BigInteger.class) {
            return value instanceof BigDecimal decimal
                    ? decimal.toBigIntegerExact()
                    : new BigInteger(String.valueOf(value));
        }
        if (targetType == BigDecimal.class) {
            return value instanceof BigInteger integer
                    ? new BigDecimal(integer)
                    : new BigDecimal(String.valueOf(value));
        }
        if (targetType == Long.class || targetType == long.class) {
            return number(value).longValueExact();
        }
        if (targetType == Integer.class || targetType == int.class) {
            return number(value).intValueExact();
        }
        if (targetType == Short.class || targetType == short.class) {
            return number(value).shortValueExact();
        }
        if (targetType == Byte.class || targetType == byte.class) {
            return number(value).byteValueExact();
        }
        if (targetType == Double.class || targetType == double.class) {
            return Double.valueOf(String.valueOf(value));
        }
        if (targetType == Float.class || targetType == float.class) {
            return Float.valueOf(String.valueOf(value));
        }
        if (targetType == Boolean.class || targetType == boolean.class) {
            return value instanceof Boolean bool ? bool : Boolean.valueOf(String.valueOf(value));
        }
        if (targetType == LocalDate.class && value instanceof LocalDate date) {
            return date;
        }
        if (targetType == LocalTime.class && value instanceof LocalTime time) {
            return time;
        }
        if (targetType.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>) targetType, String.valueOf(value));
        }
        return value;
    }

    private BigInteger number(Object value) {
        if (value instanceof BigInteger integer) {
            return integer;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.toBigIntegerExact();
        }
        return new BigInteger(String.valueOf(value));
    }

    private Object primitiveDefault(Class<?> targetType) {
        if (!targetType.isPrimitive()) {
            return null;
        }
        if (targetType == boolean.class) {
            return false;
        }
        if (targetType == char.class) {
            return '\0';
        }
        if (targetType == byte.class) {
            return (byte) 0;
        }
        if (targetType == short.class) {
            return (short) 0;
        }
        if (targetType == int.class) {
            return 0;
        }
        if (targetType == long.class) {
            return 0L;
        }
        if (targetType == float.class) {
            return 0.0f;
        }
        return 0.0d;
    }

    private Field findField(Class<?> dtoType, String name) {
        try {
            Field field = dtoType.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }

    private void requireDtoType(Class<?> dtoType) {
        if (dtoType == null) {
            throw new IllegalArgumentException("고정길이 DTO 타입은 필수입니다.");
        }
    }

    private record FieldBinding(
            String javaName,
            String wireName,
            Class<?> javaType,
            int order,
            int length,
            CpfFixedLengthField annotation,
            Method accessor,
            Field field) {
    }

    private record DtoMetadata(Class<?> dtoType, List<FieldBinding> bindings) {
        private DtoMetadata {
            bindings = List.copyOf(bindings);
        }

        private CpfFixedLengthLayout layout(Charset charset) {
            int start = 1;
            List<CpfFixedLengthFieldSpec> fields = new ArrayList<>();
            for (FieldBinding binding : bindings) {
                CpfFixedLengthField annotation = binding.annotation();
                fields.add(new CpfFixedLengthFieldSpec(
                        binding.wireName(),
                        start,
                        binding.length(),
                        annotation.type(),
                        annotation.required(),
                        annotation.padding(),
                        annotation.alignment(),
                        annotation.sensitive(),
                        annotation.scale(),
                        annotation.defaultValue(),
                        annotation.trim(),
                        annotation.converterId()));
                start += binding.length();
            }
            return new CpfFixedLengthLayout(
                    dtoType.getName(),
                    "1",
                    charset,
                    start - 1,
                    fields,
                    List.of());
        }

        private Map<String, FieldBinding> bindingByJavaName() {
            Map<String, FieldBinding> result = new LinkedHashMap<>();
            bindings.forEach(binding -> result.put(binding.javaName(), binding));
            return result;
        }
    }
}
