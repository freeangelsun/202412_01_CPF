package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthAlignment;
import com.cpf.core.api.fixedlength.CpfFixedLengthError;
import com.cpf.core.api.fixedlength.CpfFixedLengthException;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldType;
import com.cpf.core.api.fixedlength.CpfFixedLengthGroupSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import com.cpf.core.api.fixedlength.CpfFixedLengthParseResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthParser;
import com.cpf.core.spi.fixedlength.CpfFixedLengthConverter;
import com.cpf.core.spi.fixedlength.CpfFixedLengthEncoding;
import com.cpf.core.spi.fixedlength.CpfFixedLengthMasker;
import com.cpf.core.spi.fixedlength.CpfFixedLengthValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * byte offset과 charset 경계를 엄격하게 지키는 CPF 기본 parser입니다.
 */
@Component
public final class DefaultCpfFixedLengthParser implements CpfFixedLengthParser {
    private final List<CpfFixedLengthConverter> converters;
    private final List<CpfFixedLengthValidator> validators;
    private final CpfFixedLengthMasker masker;
    private final CpfFixedLengthEncoding encoding;

    public DefaultCpfFixedLengthParser() {
        this(
                List.of(new DefaultCpfFixedLengthConverter()),
                List.of(new DefaultCpfFixedLengthValidator()),
                new DefaultCpfFixedLengthMasker(),
                new DefaultCpfFixedLengthEncoding());
    }

    @Autowired
    public DefaultCpfFixedLengthParser(
            List<CpfFixedLengthConverter> converters,
            List<CpfFixedLengthValidator> validators,
            CpfFixedLengthMasker masker,
            CpfFixedLengthEncoding encoding) {
        this.converters = requireConverters(converters);
        this.validators = validators == null ? List.of() : List.copyOf(validators);
        this.masker = masker == null ? new DefaultCpfFixedLengthMasker() : masker;
        this.encoding = encoding == null ? new DefaultCpfFixedLengthEncoding() : encoding;
    }

    @Override
    public CpfFixedLengthParseResult parse(String message, CpfFixedLengthLayout layout) {
        requireLayout(layout);
        if (message == null) {
            throw new CpfFixedLengthException(
                    "고정길이 전문 원문이 없습니다.",
                    List.of(new CpfFixedLengthError(
                            "_message",
                            "CPF_FIXED_MESSAGE_REQUIRED",
                            "고정길이 전문 원문은 필수입니다.")));
        }
        try {
            return parseBytes(encoding.encode(message, layout.charset()), layout, message);
        } catch (CharacterCodingException exception) {
            throw encodingException("CPF_FIXED_MESSAGE_ENCODING_INVALID", exception);
        }
    }

    @Override
    public CpfFixedLengthParseResult parse(byte[] message, CpfFixedLengthLayout layout) {
        requireLayout(layout);
        if (message == null) {
            throw new CpfFixedLengthException(
                    "고정길이 전문 byte 원문이 없습니다.",
                    List.of(new CpfFixedLengthError(
                            "_message",
                            "CPF_FIXED_MESSAGE_REQUIRED",
                            "고정길이 전문 byte 원문은 필수입니다.")));
        }
        return parseBytes(message.clone(), layout, null);
    }

    private CpfFixedLengthParseResult parseBytes(
            byte[] bytes,
            CpfFixedLengthLayout layout,
            String providedRawMessage) {
        List<CpfFixedLengthError> errors = new ArrayList<>();
        String rawMessage = providedRawMessage;
        if (rawMessage == null) {
            try {
                rawMessage = encoding.decode(bytes, layout.charset());
            } catch (CharacterCodingException exception) {
                rawMessage = "";
                errors.add(new CpfFixedLengthError(
                        "_message",
                        "CPF_FIXED_MESSAGE_ENCODING_INVALID",
                        "전문 원문이 지정 charset의 유효한 byte 시퀀스가 아닙니다.",
                        0,
                        1,
                        "<invalid-bytes>"));
            }
        }
        if (bytes.length != layout.totalLength()) {
            int mismatchOffset = Math.min(bytes.length, layout.totalLength());
            errors.add(new CpfFixedLengthError(
                    "_message",
                    "CPF_FIXED_MESSAGE_LENGTH_MISMATCH",
                    "전문 byte 길이가 layout의 전체 길이와 다릅니다. expected="
                            + layout.totalLength() + ", actual=" + bytes.length,
                    mismatchOffset,
                    mismatchOffset + 1,
                    ""));
        }

        Map<String, String> fields = new LinkedHashMap<>();
        Map<String, Object> typedFields = new LinkedHashMap<>();
        Map<String, String> maskedFields = new LinkedHashMap<>();
        for (CpfFixedLengthFieldSpec field : layout.fields()) {
            ParsedField parsed = parseField(
                    bytes,
                    layout,
                    field,
                    field.name(),
                    field.zeroBasedStart(),
                    errors);
            addField(parsed, field.name(), fields, typedFields, maskedFields);
        }

        Map<String, List<Map<String, String>>> groups = new LinkedHashMap<>();
        Map<String, List<Map<String, Object>>> typedGroups = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> maskedGroups = new LinkedHashMap<>();
        parseGroups(
                bytes,
                layout,
                fields,
                groups,
                typedGroups,
                maskedGroups,
                errors);

        return new CpfFixedLengthParseResult(
                rawMessage,
                bytes.length,
                fields,
                typedFields,
                maskedFields,
                groups,
                typedGroups,
                maskedGroups,
                errors);
    }

    private void parseGroups(
            byte[] bytes,
            CpfFixedLengthLayout layout,
            Map<String, String> fields,
            Map<String, List<Map<String, String>>> groups,
            Map<String, List<Map<String, Object>>> typedGroups,
            Map<String, List<Map<String, String>>> maskedGroups,
            List<CpfFixedLengthError> errors) {
        int fallbackStart = layout.fields().stream()
                .mapToInt(CpfFixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);

        for (CpfFixedLengthGroupSpec group : layout.groups()) {
            int itemCount = resolveGroupCount(group, fields, errors);
            int groupStart = group.zeroBasedStart(fallbackStart);
            int itemLength = group.itemLength();
            List<Map<String, String>> rows = new ArrayList<>();
            List<Map<String, Object>> typedRows = new ArrayList<>();
            List<Map<String, String>> maskedRows = new ArrayList<>();

            for (int index = 0; index < itemCount; index++) {
                Map<String, String> row = new LinkedHashMap<>();
                Map<String, Object> typedRow = new LinkedHashMap<>();
                Map<String, String> maskedRow = new LinkedHashMap<>();
                int itemStart = groupStart + (index * itemLength);
                for (CpfFixedLengthFieldSpec field : group.fields()) {
                    String fieldPath = group.name() + "[" + index + "]." + field.name();
                    ParsedField parsed = parseField(
                            bytes,
                            layout,
                            field,
                            fieldPath,
                            itemStart + field.zeroBasedStart(),
                            errors);
                    addField(parsed, field.name(), row, typedRow, maskedRow);
                }
                rows.add(Map.copyOf(row));
                typedRows.add(Map.copyOf(typedRow));
                maskedRows.add(Map.copyOf(maskedRow));
            }
            groups.put(group.name(), List.copyOf(rows));
            typedGroups.put(group.name(), List.copyOf(typedRows));
            maskedGroups.put(group.name(), List.copyOf(maskedRows));
            fallbackStart = groupStart + (itemLength * group.maxCount());
        }
    }

    private ParsedField parseField(
            byte[] bytes,
            CpfFixedLengthLayout layout,
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            int byteOffset,
            List<CpfFixedLengthError> errors) {
        int endExclusive = byteOffset + field.length();
        if (byteOffset < 0 || endExclusive > bytes.length) {
            errors.add(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_FIELD_RANGE_OVERFLOW",
                    "필드 byte 범위가 전문 원문을 초과합니다.",
                    Math.max(byteOffset, 0),
                    Math.max(byteOffset, 0) + 1,
                    ""));
            return null;
        }

        String raw;
        try {
            raw = encoding.decode(Arrays.copyOfRange(bytes, byteOffset, endExclusive), layout.charset());
        } catch (CharacterCodingException exception) {
            errors.add(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_FIELD_ENCODING_INVALID",
                    "필드 byte 구간이 지정 charset의 문자 경계와 일치하지 않습니다.",
                    byteOffset,
                    byteOffset + 1,
                    "<invalid-bytes>"));
            return null;
        }

        String value = stripPadding(raw, field);
        String maskedValue = masker.mask(field, value);
        for (CpfFixedLengthValidator validator : validators) {
            if (!validator.supports(field)) {
                continue;
            }
            List<CpfFixedLengthError> violations = validator.validate(field, fieldPath, value, byteOffset);
            if (violations != null) {
                violations.forEach(error -> errors.add(
                        normalizeError(error, field, fieldPath, value, byteOffset)));
            }
        }

        Object typedValue = null;
        if (!value.isBlank() || !field.defaultValue().isEmpty()) {
            try {
                typedValue = converterFor(field).parse(field, value);
            } catch (RuntimeException exception) {
                errors.add(new CpfFixedLengthError(
                        fieldPath,
                        "CPF_FIXED_FIELD_TYPE_INVALID",
                        "필드 값을 지정한 논리 자료형으로 변환하지 못했습니다. type=" + field.type(),
                        byteOffset,
                        byteOffset + 1,
                        maskedValue));
            }
        } else if (field.type() == CpfFixedLengthFieldType.STRING) {
            typedValue = "";
        }
        return new ParsedField(value, typedValue, maskedValue);
    }

    private int resolveGroupCount(
            CpfFixedLengthGroupSpec group,
            Map<String, String> fields,
            List<CpfFixedLengthError> errors) {
        String countValue = fields.get(group.countFieldName());
        if (countValue == null || countValue.isBlank()) {
            errors.add(new CpfFixedLengthError(
                    group.countFieldName(),
                    "CPF_FIXED_GROUP_COUNT_REQUIRED",
                    "반복부 건수 필드 값이 필요합니다. group=" + group.name()));
            return 0;
        }
        try {
            int count = Integer.parseInt(countValue);
            if (count < 0) {
                errors.add(new CpfFixedLengthError(
                        group.countFieldName(),
                        "CPF_FIXED_GROUP_COUNT_NEGATIVE",
                        "반복부 건수는 0 이상이어야 합니다. group=" + group.name()));
                return 0;
            }
            if (count > group.maxCount()) {
                errors.add(new CpfFixedLengthError(
                        group.countFieldName(),
                        "CPF_FIXED_GROUP_COUNT_OVER_MAX",
                        "반복부 건수가 최대 허용 건수를 초과합니다. group=" + group.name()));
                return group.maxCount();
            }
            return count;
        } catch (NumberFormatException exception) {
            errors.add(new CpfFixedLengthError(
                    group.countFieldName(),
                    "CPF_FIXED_GROUP_COUNT_INVALID",
                    "반복부 건수 필드는 정수여야 합니다. group=" + group.name()));
            return 0;
        }
    }

    private void addField(
            ParsedField parsed,
            String name,
            Map<String, String> values,
            Map<String, Object> typedValues,
            Map<String, String> maskedValues) {
        if (parsed == null) {
            return;
        }
        values.put(name, parsed.value());
        if (parsed.typedValue() != null) {
            typedValues.put(name, parsed.typedValue());
        }
        maskedValues.put(name, parsed.maskedValue());
    }

    private CpfFixedLengthError normalizeError(
            CpfFixedLengthError error,
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            String value,
            int byteOffset) {
        if (error == null) {
            return new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_VALIDATION_ERROR",
                    "확장 validator가 빈 오류를 반환했습니다.",
                    byteOffset,
                    byteOffset + 1,
                    masker.mask(field, value));
        }
        String maskedValue = masker.mask(field, value);
        String safeMessage = error.message();
        if (field.sensitive() && value != null && !value.isEmpty()) {
            safeMessage = safeMessage.replace(value, maskedValue);
        }
        int effectiveOffset = error.byteOffset() < 0 ? byteOffset : error.byteOffset();
        int effectivePosition = error.originalPosition() < 0 ? effectiveOffset + 1 : error.originalPosition();
        return new CpfFixedLengthError(
                error.fieldName() == null || error.fieldName().isBlank() ? fieldPath : error.fieldName(),
                error.errorCode(),
                safeMessage,
                effectiveOffset,
                effectivePosition,
                maskedValue);
    }

    private CpfFixedLengthConverter converterFor(CpfFixedLengthFieldSpec field) {
        return converters.stream()
                .filter(converter -> field.converterId().isBlank() || field.converterId().equals(converter.id()))
                .filter(converter -> converter.supports(field))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "고정길이 필드 converter를 찾을 수 없습니다. field=" + field.name()));
    }

    private String stripPadding(String raw, CpfFixedLengthFieldSpec field) {
        if (!field.trim() || raw.isEmpty()) {
            return raw;
        }
        int start = 0;
        int end = raw.length();
        if (field.alignment() == CpfFixedLengthAlignment.RIGHT) {
            while (start < end && raw.charAt(start) == field.padding()) {
                start++;
            }
        } else {
            while (end > start && raw.charAt(end - 1) == field.padding()) {
                end--;
            }
        }
        if (start == end
                && field.padding() == '0'
                && (field.type() == CpfFixedLengthFieldType.NUMBER
                || field.type() == CpfFixedLengthFieldType.DECIMAL
                || field.type() == CpfFixedLengthFieldType.AMOUNT)) {
            return "0";
        }
        return raw.substring(start, end);
    }

    private CpfFixedLengthException encodingException(String errorCode, CharacterCodingException cause) {
        return new CpfFixedLengthException(
                "고정길이 전문 encoding에 실패했습니다.",
                cause,
                List.of(new CpfFixedLengthError(
                        "_message",
                        errorCode,
                        "전문 문자열을 layout charset으로 안전하게 인코딩할 수 없습니다.",
                        0,
                        1,
                        "<unencodable>")));
    }

    private void requireLayout(CpfFixedLengthLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("고정길이 layout은 필수입니다.");
        }
    }

    private static List<CpfFixedLengthConverter> requireConverters(
            List<CpfFixedLengthConverter> converters) {
        if (converters == null || converters.isEmpty()) {
            throw new IllegalArgumentException("하나 이상의 고정길이 converter가 필요합니다.");
        }
        return List.copyOf(converters);
    }

    private record ParsedField(String value, Object typedValue, String maskedValue) {
    }
}
