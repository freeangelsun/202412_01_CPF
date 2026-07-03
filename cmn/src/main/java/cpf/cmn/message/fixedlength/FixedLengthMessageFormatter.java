package cpf.cmn.message.fixedlength;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * byte 길이 기준 고정길이 전문 formatter skeleton입니다.
 */
public class FixedLengthMessageFormatter {
    private final FixedLengthMaskingRule maskingRule;

    public FixedLengthMessageFormatter() {
        this(new FixedLengthMaskingRule());
    }

    public FixedLengthMessageFormatter(FixedLengthMaskingRule maskingRule) {
        this.maskingRule = maskingRule;
    }

    public FixedLengthFormatResult format(Map<String, ?> values, FixedLengthLayoutSpec layout) {
        if (values == null) {
            throw new IllegalArgumentException("고정길이 전문 생성 값은 필수입니다.");
        }
        byte[] messageBytes = new byte[layout.totalLength()];
        Arrays.fill(messageBytes, (byte) ' ');
        Map<String, String> maskedFields = new LinkedHashMap<>();

        for (FixedLengthFieldSpec field : layout.fields()) {
            String value = stringValue(values.get(field.name()));
            if (field.required() && value.isBlank()) {
                throw new IllegalArgumentException("필수 필드 값이 비어 있습니다. name=" + field.name());
            }
            writeField(messageBytes, value, field, layout);
            maskedFields.put(field.name(), maskingRule.mask(field, value));
        }

        return new FixedLengthFormatResult(
                new String(messageBytes, layout.charset()),
                messageBytes.length,
                Map.copyOf(maskedFields));
    }

    private void writeField(byte[] messageBytes, String value, FixedLengthFieldSpec field, FixedLengthLayoutSpec layout) {
        byte[] paddingBytes = String.valueOf(field.padding()).getBytes(layout.charset());
        if (paddingBytes.length != 1) {
            throw new IllegalArgumentException("padding 문자는 1 byte 문자만 사용할 수 있습니다. name=" + field.name());
        }
        byte[] fieldBytes = value.getBytes(layout.charset());
        if (fieldBytes.length > field.length()) {
            throw new IllegalArgumentException("필드 byte 길이가 레이아웃보다 깁니다. name=" + field.name()
                    + ", expected=" + field.length() + ", actual=" + fieldBytes.length);
        }

        int offset = field.zeroBasedStart();
        Arrays.fill(messageBytes, offset, field.zeroBasedEndExclusive(), paddingBytes[0]);
        int copyStart = field.alignment() == FixedLengthAlignment.RIGHT
                ? field.zeroBasedEndExclusive() - fieldBytes.length
                : offset;
        System.arraycopy(fieldBytes, 0, messageBytes, copyStart, fieldBytes.length);
    }

    private String stringValue(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
