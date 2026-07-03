package cpf.cmn.message.fixedlength;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * byte 길이 기준 고정길이 전문 parser skeleton입니다.
 */
public class FixedLengthMessageParser {
    private final FixedLengthMaskingRule maskingRule;

    public FixedLengthMessageParser() {
        this(new FixedLengthMaskingRule());
    }

    public FixedLengthMessageParser(FixedLengthMaskingRule maskingRule) {
        this.maskingRule = maskingRule;
    }

    public FixedLengthParseResult parse(String message, FixedLengthLayoutSpec layout) {
        if (message == null) {
            throw new IllegalArgumentException("고정길이 전문 원문은 필수입니다.");
        }
        byte[] bytes = message.getBytes(layout.charset());
        List<String> errors = new ArrayList<>();
        if (bytes.length != layout.totalLength()) {
            errors.add("전문 byte 길이가 레이아웃과 다릅니다. expected=" + layout.totalLength() + ", actual=" + bytes.length);
        }

        Map<String, String> fields = new LinkedHashMap<>();
        Map<String, String> maskedFields = new LinkedHashMap<>();
        for (FixedLengthFieldSpec field : layout.fields()) {
            if (field.zeroBasedEndExclusive() > bytes.length) {
                errors.add("필드가 원문 길이를 초과합니다. name=" + field.name());
                continue;
            }
            byte[] fieldBytes = Arrays.copyOfRange(bytes, field.zeroBasedStart(), field.zeroBasedEndExclusive());
            String raw = new String(fieldBytes, layout.charset());
            String value = stripPadding(raw, field);
            if (field.required() && value.isBlank()) {
                errors.add("필수 필드 값이 비어 있습니다. name=" + field.name());
            }
            fields.put(field.name(), value);
            maskedFields.put(field.name(), maskingRule.mask(field, value));
        }
        return new FixedLengthParseResult(message, Map.copyOf(fields), Map.copyOf(maskedFields), List.copyOf(errors));
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
