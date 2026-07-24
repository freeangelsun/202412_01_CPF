package com.cpf.core.common.fixedlength;

import com.cpf.core.api.fixedlength.CpfFixedLengthAlignment;
import com.cpf.core.api.fixedlength.CpfFixedLengthError;
import com.cpf.core.api.fixedlength.CpfFixedLengthException;
import com.cpf.core.api.fixedlength.CpfFixedLengthFieldSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthGroupSpec;
import com.cpf.core.api.fixedlength.CpfFixedLengthLayout;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriteResult;
import com.cpf.core.api.fixedlength.CpfFixedLengthWriter;
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
 * 값이 필드 경계에서 잘리지 않도록 byte 단위로 생성하는 CPF 기본 writer입니다.
 */
@Component
public final class DefaultCpfFixedLengthWriter implements CpfFixedLengthWriter {
    private final List<CpfFixedLengthConverter> converters;
    private final List<CpfFixedLengthValidator> validators;
    private final CpfFixedLengthMasker masker;
    private final CpfFixedLengthEncoding encoding;

    public DefaultCpfFixedLengthWriter() {
        this(
                List.of(new DefaultCpfFixedLengthConverter()),
                List.of(new DefaultCpfFixedLengthValidator()),
                new DefaultCpfFixedLengthMasker(),
                new DefaultCpfFixedLengthEncoding());
    }

    @Autowired
    public DefaultCpfFixedLengthWriter(
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
    public CpfFixedLengthWriteResult write(Map<String, ?> values, CpfFixedLengthLayout layout) {
        if (layout == null) {
            throw new IllegalArgumentException("고정길이 layout은 필수입니다.");
        }
        if (values == null) {
            throw new CpfFixedLengthException(
                    "고정길이 전문 생성 값이 없습니다.",
                    List.of(new CpfFixedLengthError(
                            "_message",
                            "CPF_FIXED_VALUES_REQUIRED",
                            "고정길이 전문 생성 값은 필수입니다.")));
        }

        List<CpfFixedLengthError> errors = new ArrayList<>();
        byte[] messageBytes = initializedMessage(layout, errors);
        Map<String, Object> normalizedValues = normalizeGroupCounts(values, layout, errors);
        Map<String, String> maskedFields = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> maskedGroups = new LinkedHashMap<>();

        for (CpfFixedLengthFieldSpec field : layout.fields()) {
            String formatted = formatAndWrite(
                    messageBytes,
                    normalizedValues.get(field.name()),
                    field,
                    field.name(),
                    field.zeroBasedStart(),
                    layout,
                    errors);
            if (formatted != null) {
                maskedFields.put(field.name(), masker.mask(field, formatted));
            }
        }
        writeGroups(messageBytes, values, layout, maskedGroups, errors);

        if (!errors.isEmpty()) {
            throw new CpfFixedLengthException("고정길이 전문 생성에 실패했습니다.", errors);
        }
        try {
            return new CpfFixedLengthWriteResult(
                    encoding.decode(messageBytes, layout.charset()),
                    messageBytes,
                    maskedFields,
                    maskedGroups);
        } catch (CharacterCodingException exception) {
            throw new CpfFixedLengthException(
                    "생성된 고정길이 전문을 decode하지 못했습니다.",
                    exception,
                    List.of(new CpfFixedLengthError(
                            "_message",
                            "CPF_FIXED_MESSAGE_ENCODING_INVALID",
                            "생성된 전문이 layout charset의 유효한 byte 시퀀스가 아닙니다.",
                            0,
                            1,
                            "<invalid-bytes>")));
        }
    }

    private byte[] initializedMessage(
            CpfFixedLengthLayout layout,
            List<CpfFixedLengthError> errors) {
        byte[] messageBytes = new byte[layout.totalLength()];
        try {
            byte[] space = encoding.encode(" ", layout.charset());
            if (space.length != 1) {
                errors.add(new CpfFixedLengthError(
                        "_message",
                        "CPF_FIXED_LAYOUT_CHARSET_UNSUPPORTED",
                        "layout charset에서 공백 문자가 정확히 1 byte가 아닙니다."));
                return messageBytes;
            }
            Arrays.fill(messageBytes, space[0]);
            return messageBytes;
        } catch (CharacterCodingException exception) {
            errors.add(new CpfFixedLengthError(
                    "_message",
                    "CPF_FIXED_LAYOUT_CHARSET_UNSUPPORTED",
                    "layout charset으로 공백 문자를 인코딩할 수 없습니다."));
            return messageBytes;
        }
    }

    private Map<String, Object> normalizeGroupCounts(
            Map<String, ?> values,
            CpfFixedLengthLayout layout,
            List<CpfFixedLengthError> errors) {
        Map<String, Object> normalized = new LinkedHashMap<>(values);
        for (CpfFixedLengthGroupSpec group : layout.groups()) {
            Object groupValue = values.get(group.name());
            List<?> rows;
            if (groupValue == null) {
                rows = List.of();
            } else if (groupValue instanceof List<?> list) {
                rows = list;
            } else {
                rows = List.of();
                errors.add(new CpfFixedLengthError(
                        group.name(),
                        "CPF_FIXED_GROUP_VALUE_INVALID",
                        "반복부 값은 Map 항목의 List여야 합니다."));
            }
            Object declaredCount = values.get(group.countFieldName());
            if (declaredCount != null && !String.valueOf(rows.size()).equals(normalizedCount(declaredCount))) {
                errors.add(new CpfFixedLengthError(
                        group.countFieldName(),
                        "CPF_FIXED_GROUP_COUNT_MISMATCH",
                        "반복부 건수 필드와 실제 항목 수가 다릅니다. group=" + group.name()));
            }
            normalized.put(group.countFieldName(), rows.size());
        }
        return normalized;
    }

    private String normalizedCount(Object value) {
        try {
            return new java.math.BigInteger(String.valueOf(value).trim()).toString();
        } catch (NumberFormatException exception) {
            return String.valueOf(value);
        }
    }

    private void writeGroups(
            byte[] messageBytes,
            Map<String, ?> values,
            CpfFixedLengthLayout layout,
            Map<String, List<Map<String, String>>> maskedGroups,
            List<CpfFixedLengthError> errors) {
        int fallbackStart = layout.fields().stream()
                .mapToInt(CpfFixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);

        for (CpfFixedLengthGroupSpec group : layout.groups()) {
            Object groupValue = values.get(group.name());
            List<?> rows = groupValue instanceof List<?> list ? list : List.of();
            if (rows.size() > group.maxCount()) {
                errors.add(new CpfFixedLengthError(
                        group.name(),
                        "CPF_FIXED_GROUP_COUNT_OVER_MAX",
                        "반복부 건수가 최대 허용 건수를 초과합니다. group=" + group.name()));
            }
            int groupStart = group.zeroBasedStart(fallbackStart);
            int itemLength = group.itemLength();
            List<Map<String, String>> maskedRows = new ArrayList<>();
            int writableCount = Math.min(rows.size(), group.maxCount());
            for (int index = 0; index < writableCount; index++) {
                Object rowValue = rows.get(index);
                Map<?, ?> row;
                if (rowValue instanceof Map<?, ?> map) {
                    row = map;
                } else {
                    row = Map.of();
                    errors.add(new CpfFixedLengthError(
                            group.name() + "[" + index + "]",
                            "CPF_FIXED_GROUP_ROW_INVALID",
                            "반복부 항목은 필드 Map이어야 합니다."));
                }
                Map<String, String> maskedRow = new LinkedHashMap<>();
                int itemStart = groupStart + (index * itemLength);
                for (CpfFixedLengthFieldSpec field : group.fields()) {
                    String fieldPath = group.name() + "[" + index + "]." + field.name();
                    String formatted = formatAndWrite(
                            messageBytes,
                            row.get(field.name()),
                            field,
                            fieldPath,
                            itemStart + field.zeroBasedStart(),
                            layout,
                            errors);
                    if (formatted != null) {
                        maskedRow.put(field.name(), masker.mask(field, formatted));
                    }
                }
                maskedRows.add(Map.copyOf(maskedRow));
            }
            maskedGroups.put(group.name(), List.copyOf(maskedRows));
            fallbackStart = groupStart + (itemLength * group.maxCount());
        }
    }

    private String formatAndWrite(
            byte[] messageBytes,
            Object value,
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            int byteOffset,
            CpfFixedLengthLayout layout,
            List<CpfFixedLengthError> errors) {
        String formatted;
        try {
            formatted = converterFor(field).format(field, value);
        } catch (RuntimeException exception) {
            errors.add(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_FIELD_TYPE_INVALID",
                    "필드 값을 지정한 논리 자료형의 wire 값으로 변환하지 못했습니다. type=" + field.type(),
                    byteOffset,
                    byteOffset + 1,
                    safeRejectedValue(field, value)));
            return null;
        }

        boolean validationFailed = false;
        for (CpfFixedLengthValidator validator : validators) {
            if (!validator.supports(field)) {
                continue;
            }
            List<CpfFixedLengthError> violations = validator.validate(field, fieldPath, formatted, byteOffset);
            if (violations != null && !violations.isEmpty()) {
                validationFailed = true;
                violations.forEach(error -> errors.add(
                        normalizeError(error, field, fieldPath, formatted, byteOffset)));
            }
        }
        if (!validationFailed) {
            writeField(messageBytes, formatted, field, fieldPath, byteOffset, layout, errors);
        }
        return formatted;
    }

    private void writeField(
            byte[] messageBytes,
            String value,
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            int byteOffset,
            CpfFixedLengthLayout layout,
            List<CpfFixedLengthError> errors) {
        byte[] paddingBytes;
        byte[] fieldBytes;
        try {
            paddingBytes = encoding.encode(String.valueOf(field.padding()), layout.charset());
            fieldBytes = encoding.encode(value, layout.charset());
        } catch (CharacterCodingException exception) {
            errors.add(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_FIELD_ENCODING_INVALID",
                    "필드 값을 layout charset으로 인코딩할 수 없습니다.",
                    byteOffset,
                    byteOffset + 1,
                    masker.mask(field, value)));
            return;
        }
        if (paddingBytes.length != 1) {
            errors.add(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_PADDING_ENCODING_INVALID",
                    "padding 문자는 layout charset에서 정확히 1 byte여야 합니다.",
                    byteOffset,
                    byteOffset + 1,
                    ""));
            return;
        }
        if (fieldBytes.length > field.length()) {
            errors.add(new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_FIELD_LENGTH_OVERFLOW",
                    "필드 byte 길이가 layout을 초과합니다. expected="
                            + field.length() + ", actual=" + fieldBytes.length,
                    byteOffset,
                    byteOffset + 1,
                    masker.mask(field, value)));
            return;
        }
        int endExclusive = byteOffset + field.length();
        Arrays.fill(messageBytes, byteOffset, endExclusive, paddingBytes[0]);
        int copyStart = field.alignment() == CpfFixedLengthAlignment.RIGHT
                ? endExclusive - fieldBytes.length
                : byteOffset;
        System.arraycopy(fieldBytes, 0, messageBytes, copyStart, fieldBytes.length);
    }

    private CpfFixedLengthError normalizeError(
            CpfFixedLengthError error,
            CpfFixedLengthFieldSpec field,
            String fieldPath,
            String value,
            int byteOffset) {
        String maskedValue = masker.mask(field, value);
        if (error == null) {
            return new CpfFixedLengthError(
                    fieldPath,
                    "CPF_FIXED_VALIDATION_ERROR",
                    "확장 validator가 빈 오류를 반환했습니다.",
                    byteOffset,
                    byteOffset + 1,
                    maskedValue);
        }
        String safeMessage = error.message();
        if (field.sensitive() && value != null && !value.isEmpty()) {
            safeMessage = safeMessage.replace(value, maskedValue);
        }
        int effectiveOffset = error.byteOffset() < 0 ? byteOffset : error.byteOffset();
        int effectivePosition = error.originalPosition() < 0 ? effectiveOffset + 1 : error.originalPosition();
        return new CpfFixedLengthError(
                error.fieldName(),
                error.errorCode(),
                safeMessage,
                effectiveOffset,
                effectivePosition,
                maskedValue);
    }

    private String safeRejectedValue(CpfFixedLengthFieldSpec field, Object value) {
        if (value == null) {
            return "";
        }
        return masker.mask(field, String.valueOf(value));
    }

    private CpfFixedLengthConverter converterFor(CpfFixedLengthFieldSpec field) {
        return converters.stream()
                .filter(converter -> field.converterId().isBlank() || field.converterId().equals(converter.id()))
                .filter(converter -> converter.supports(field))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "고정길이 필드 converter를 찾을 수 없습니다. field=" + field.name()));
    }

    private static List<CpfFixedLengthConverter> requireConverters(
            List<CpfFixedLengthConverter> converters) {
        if (converters == null || converters.isEmpty()) {
            throw new IllegalArgumentException("하나 이상의 고정길이 converter가 필요합니다.");
        }
        return List.copyOf(converters);
    }
}
