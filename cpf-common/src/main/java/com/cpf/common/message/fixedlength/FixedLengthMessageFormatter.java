package com.cpf.common.message.fixedlength;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * byte 길이 기준 고정길이 전문 formatter입니다.
 */
public class FixedLengthMessageFormatter {
    private final FixedLengthMaskingRule maskingRule;
    private final FixedLengthTypeConverter typeConverter;

    public FixedLengthMessageFormatter() {
        this(new FixedLengthMaskingRule(), new FixedLengthTypeConverter());
    }

    public FixedLengthMessageFormatter(FixedLengthMaskingRule maskingRule) {
        this(maskingRule, new FixedLengthTypeConverter());
    }

    public FixedLengthMessageFormatter(FixedLengthMaskingRule maskingRule, FixedLengthTypeConverter typeConverter) {
        this.maskingRule = maskingRule;
        this.typeConverter = typeConverter;
    }

    public FixedLengthFormatResult format(Map<String, ?> values, FixedLengthLayoutSpec layout) {
        if (values == null) {
            throw new IllegalArgumentException("고정길이 전문 생성 값은 필수입니다.");
        }
        Map<String, Object> normalizedValues = normalizeGroupCounts(values, layout);
        byte[] messageBytes = new byte[layout.totalLength()];
        Arrays.fill(messageBytes, (byte) ' ');
        Map<String, String> maskedFields = new LinkedHashMap<>();
        Map<String, List<Map<String, String>>> maskedGroups = new LinkedHashMap<>();

        for (FixedLengthFieldSpec field : layout.fields()) {
            String value = typeConverter.format(field, normalizedValues.get(field.name()));
            if (field.required() && value.isBlank()) {
                throw new IllegalArgumentException("필수 필드 값이 비어 있습니다. name=" + field.name());
            }
            writeField(messageBytes, value, field, layout);
            maskedFields.put(field.name(), maskingRule.mask(field, value));
        }
        writeGroups(messageBytes, values, layout, maskedGroups);

        return new FixedLengthFormatResult(
                new String(messageBytes, layout.charset()),
                messageBytes.length,
                Map.copyOf(maskedFields),
                copyNested(maskedGroups));
    }

    private Map<String, Object> normalizeGroupCounts(Map<String, ?> values, FixedLengthLayoutSpec layout) {
        Map<String, Object> normalized = new HashMap<>(values);
        for (FixedLengthGroupSpec group : layout.groups()) {
            Object groupValue = values.get(group.name());
            if (!normalized.containsKey(group.countFieldName()) && groupValue instanceof List<?> list) {
                normalized.put(group.countFieldName(), list.size());
            }
        }
        return normalized;
    }

    private void writeGroups(byte[] messageBytes,
                             Map<String, ?> values,
                             FixedLengthLayoutSpec layout,
                             Map<String, List<Map<String, String>>> maskedGroups) {
        int fallbackStart = layout.fields().stream()
                .mapToInt(FixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);
        for (FixedLengthGroupSpec group : layout.groups()) {
            Object groupValue = values.get(group.name());
            List<?> rows = groupValue instanceof List<?> list ? list : List.of();
            if (rows.size() > group.maxCount()) {
                throw new IllegalArgumentException("반복부 건수가 최대 허용 건수를 초과합니다. name=" + group.name());
            }

            int groupStart = group.zeroBasedStart(fallbackStart);
            int itemLength = group.itemLength();
            List<Map<String, String>> maskedItems = new ArrayList<>();
            for (int index = 0; index < rows.size(); index++) {
                Map<?, ?> row = rows.get(index) instanceof Map<?, ?> map ? map : Map.of();
                Map<String, String> maskedItem = new LinkedHashMap<>();
                for (FixedLengthFieldSpec field : group.fields()) {
                    String value = typeConverter.format(field, row.get(field.name()));
                    if (field.required() && value.isBlank()) {
                        throw new IllegalArgumentException("반복부 필수 필드 값이 비어 있습니다. name="
                                + group.name() + "[" + index + "]." + field.name());
                    }
                    writeField(messageBytes, value, field, layout,
                            groupStart + (index * itemLength) + field.zeroBasedStart());
                    maskedItem.put(field.name(), maskingRule.mask(field, value));
                }
                maskedItems.add(Map.copyOf(maskedItem));
            }
            maskedGroups.put(group.name(), List.copyOf(maskedItems));
            fallbackStart = groupStart + (itemLength * group.maxCount());
        }
    }

    private void writeField(byte[] messageBytes, String value, FixedLengthFieldSpec field, FixedLengthLayoutSpec layout) {
        writeField(messageBytes, value, field, layout, field.zeroBasedStart());
    }

    private void writeField(byte[] messageBytes, String value, FixedLengthFieldSpec field, FixedLengthLayoutSpec layout, int offset) {
        byte[] paddingBytes = String.valueOf(field.padding()).getBytes(layout.charset());
        if (paddingBytes.length != 1) {
            throw new IllegalArgumentException("padding 문자는 1 byte 문자만 사용할 수 있습니다. name=" + field.name());
        }
        byte[] fieldBytes = value.getBytes(layout.charset());
        if (fieldBytes.length > field.length()) {
            throw new IllegalArgumentException("필드 byte 길이가 레이아웃보다 깁니다. name=" + field.name()
                    + ", expected=" + field.length() + ", actual=" + fieldBytes.length);
        }

        int endExclusive = offset + field.length();
        Arrays.fill(messageBytes, offset, endExclusive, paddingBytes[0]);
        int copyStart = field.alignment() == FixedLengthAlignment.RIGHT
                ? endExclusive - fieldBytes.length
                : offset;
        System.arraycopy(fieldBytes, 0, messageBytes, copyStart, fieldBytes.length);
    }

    private Map<String, List<Map<String, String>>> copyNested(Map<String, List<Map<String, String>>> source) {
        Map<String, List<Map<String, String>>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }
}
