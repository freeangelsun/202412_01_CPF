package com.cpf.common.message.fixedlength;

/**
 * 고정길이 전문의 필드 정의입니다.
 *
 * @param name 필드명
 * @param start 1부터 시작하는 byte 시작 위치
 * @param length byte 길이
 * @param type 필드 논리 타입
 * @param required 필수 여부
 * @param padding padding 문자
 * @param alignment padding 정렬
 * @param sensitive 민감정보 여부
 */
public record FixedLengthFieldSpec(
        String name,
        int start,
        int length,
        FixedLengthFieldType type,
        boolean required,
        char padding,
        FixedLengthAlignment alignment,
        boolean sensitive) {

    public FixedLengthFieldSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("고정길이 필드명은 필수입니다.");
        }
        if (start < 1) {
            throw new IllegalArgumentException("고정길이 필드 시작 위치는 1 이상이어야 합니다. name=" + name);
        }
        if (length < 1) {
            throw new IllegalArgumentException("고정길이 필드 길이는 1 이상이어야 합니다. name=" + name);
        }
        type = type == null ? FixedLengthFieldType.STRING : type;
        alignment = alignment == null ? defaultAlignment(type) : alignment;
        padding = padding == '\0' ? defaultPadding(type) : padding;
    }

    public static FixedLengthFieldSpec of(String name, int start, int length) {
        return new FixedLengthFieldSpec(name, start, length, FixedLengthFieldType.STRING, false, ' ', FixedLengthAlignment.LEFT, false);
    }

    int zeroBasedStart() {
        return start - 1;
    }

    int zeroBasedEndExclusive() {
        return zeroBasedStart() + length;
    }

    private static FixedLengthAlignment defaultAlignment(FixedLengthFieldType type) {
        return type == FixedLengthFieldType.NUMBER || type == FixedLengthFieldType.DECIMAL
                ? FixedLengthAlignment.RIGHT
                : FixedLengthAlignment.LEFT;
    }

    private static char defaultPadding(FixedLengthFieldType type) {
        return type == FixedLengthFieldType.NUMBER || type == FixedLengthFieldType.DECIMAL ? '0' : ' ';
    }
}
