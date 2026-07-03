package cpf.cmn.message.fixedlength;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * byte 길이 기준 고정길이 전문 parser입니다.
 */
public class FixedLengthMessageParser {
    private final FixedLengthMaskingRule maskingRule;
    private final FixedLengthTypeConverter typeConverter;

    public FixedLengthMessageParser() {
        this(new FixedLengthMaskingRule(), new FixedLengthTypeConverter());
    }

    public FixedLengthMessageParser(FixedLengthMaskingRule maskingRule) {
        this(maskingRule, new FixedLengthTypeConverter());
    }

    public FixedLengthMessageParser(FixedLengthMaskingRule maskingRule, FixedLengthTypeConverter typeConverter) {
        this.maskingRule = maskingRule;
        this.typeConverter = typeConverter;
    }

    public FixedLengthParseResult parse(String message, FixedLengthLayoutSpec layout) {
        if (message == null) {
            throw new IllegalArgumentException("고정길이 전문 원문은 필수입니다.");
        }
        byte[] bytes = message.getBytes(layout.charset());
        List<String> errors = new ArrayList<>();
        List<FixedLengthMessageError> fieldErrors = new ArrayList<>();
        if (bytes.length != layout.totalLength()) {
            addError(errors, fieldErrors, "_message", "FIXED_MESSAGE_LENGTH_MISMATCH",
                    "전문 byte 길이가 레이아웃과 다릅니다. expected=" + layout.totalLength() + ", actual=" + bytes.length);
        }

        Map<String, String> fields = new LinkedHashMap<>();
        Map<String, String> maskedFields = new LinkedHashMap<>();
        for (FixedLengthFieldSpec field : layout.fields()) {
            if (field.zeroBasedEndExclusive() > bytes.length) {
                addError(errors, fieldErrors, field.name(), "FIXED_FIELD_RANGE_OVERFLOW",
                        "필드가 원문 길이를 초과합니다. name=" + field.name());
                continue;
            }
            byte[] fieldBytes = Arrays.copyOfRange(bytes, field.zeroBasedStart(), field.zeroBasedEndExclusive());
            String raw = new String(fieldBytes, layout.charset());
            String value = stripPadding(raw, field);
            if (field.required() && value.isBlank()) {
                addError(errors, fieldErrors, field.name(), "FIXED_FIELD_REQUIRED",
                        "필수 필드 값이 비어 있습니다. name=" + field.name());
            }
            typeConverter.validate(field, value)
                    .ifPresent(error -> addError(errors, fieldErrors, error));
            fields.put(field.name(), value);
            maskedFields.put(field.name(), maskingRule.mask(field, value));
        }

        Map<String, List<Map<String, String>>> groups = parseGroups(bytes, layout, fields, errors, fieldErrors, false);
        Map<String, List<Map<String, String>>> maskedGroups = parseGroups(
                bytes, layout, fields, new ArrayList<>(), new ArrayList<>(), true);
        return new FixedLengthParseResult(
                message,
                Map.copyOf(fields),
                Map.copyOf(maskedFields),
                List.copyOf(errors),
                copyNested(groups),
                copyNested(maskedGroups),
                List.copyOf(fieldErrors));
    }

    private Map<String, List<Map<String, String>>> parseGroups(byte[] bytes,
                                                               FixedLengthLayoutSpec layout,
                                                               Map<String, String> fields,
                                                               List<String> errors,
                                                               List<FixedLengthMessageError> fieldErrors,
                                                               boolean masked) {
        Map<String, List<Map<String, String>>> result = new LinkedHashMap<>();
        int fallbackStart = layout.fields().stream()
                .mapToInt(FixedLengthFieldSpec::zeroBasedEndExclusive)
                .max()
                .orElse(0);

        for (FixedLengthGroupSpec group : layout.groups()) {
            int itemCount = groupItemCount(group, fields, errors, fieldErrors);
            int groupStart = group.zeroBasedStart(fallbackStart);
            int itemLength = group.itemLength();
            List<Map<String, String>> items = new ArrayList<>();

            for (int index = 0; index < itemCount; index++) {
                int itemStart = groupStart + (index * itemLength);
                Map<String, String> item = new LinkedHashMap<>();
                for (FixedLengthFieldSpec field : group.fields()) {
                    int start = itemStart + field.zeroBasedStart();
                    int end = itemStart + field.zeroBasedEndExclusive();
                    String fieldName = group.name() + "[" + index + "]." + field.name();
                    if (end > bytes.length) {
                        addError(errors, fieldErrors, fieldName, "FIXED_GROUP_FIELD_RANGE_OVERFLOW",
                                "반복부 필드가 원문 길이를 초과합니다. name=" + fieldName);
                        continue;
                    }
                    String raw = new String(Arrays.copyOfRange(bytes, start, end), layout.charset());
                    String value = stripPadding(raw, field);
                    if (field.required() && value.isBlank()) {
                        addError(errors, fieldErrors, fieldName, "FIXED_FIELD_REQUIRED",
                                "반복부 필수 필드 값이 비어 있습니다. name=" + fieldName);
                    }
                    typeConverter.validate(field, value)
                            .ifPresent(error -> addError(errors, fieldErrors,
                                    new FixedLengthMessageError(fieldName, error.errorCode(), error.message())));
                    item.put(field.name(), masked ? maskingRule.mask(field, value) : value);
                }
                items.add(Map.copyOf(item));
            }
            result.put(group.name(), List.copyOf(items));
            fallbackStart = groupStart + (itemLength * group.maxCount());
        }
        return result;
    }

    private int groupItemCount(FixedLengthGroupSpec group,
                               Map<String, String> fields,
                               List<String> errors,
                               List<FixedLengthMessageError> fieldErrors) {
        String countValue = fields.get(group.countFieldName());
        if (countValue == null || countValue.isBlank()) {
            addError(errors, fieldErrors, group.countFieldName(), "FIXED_GROUP_COUNT_REQUIRED",
                    "반복부 건수 필드 값이 필요합니다. group=" + group.name());
            return 0;
        }
        try {
            int count = Integer.parseInt(countValue);
            if (count > group.maxCount()) {
                addError(errors, fieldErrors, group.countFieldName(), "FIXED_GROUP_COUNT_OVER_MAX",
                        "반복부 건수가 최대 허용 건수를 초과합니다. group=" + group.name());
                return group.maxCount();
            }
            return Math.max(count, 0);
        } catch (NumberFormatException ex) {
            addError(errors, fieldErrors, group.countFieldName(), "FIXED_GROUP_COUNT_INVALID",
                    "반복부 건수 필드는 숫자여야 합니다. group=" + group.name());
            return 0;
        }
    }

    private Map<String, List<Map<String, String>>> copyNested(Map<String, List<Map<String, String>>> source) {
        Map<String, List<Map<String, String>>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }

    private void addError(List<String> errors, List<FixedLengthMessageError> fieldErrors, FixedLengthMessageError error) {
        addError(errors, fieldErrors, error.fieldName(), error.errorCode(), error.message());
    }

    private void addError(List<String> errors,
                          List<FixedLengthMessageError> fieldErrors,
                          String fieldName,
                          String errorCode,
                          String message) {
        errors.add(fieldName + ":" + errorCode + ":" + message);
        fieldErrors.add(new FixedLengthMessageError(fieldName, errorCode, message));
    }

    private String stripPadding(String value, FixedLengthFieldSpec field) {
        if (field.alignment() == FixedLengthAlignment.RIGHT) {
            int index = 0;
            while (index < value.length() && value.charAt(index) == field.padding()) {
                index++;
            }
            return value.substring(index).trim();
        }
        int index = value.length();
        while (index > 0 && value.charAt(index - 1) == field.padding()) {
            index--;
        }
        return value.substring(0, index).trim();
    }
}
