package com.cpf.core.api.fixedlength;

/**
 * 고정길이 전문 필드의 byte 위치와 변환 규칙입니다.
 *
 * @param name 필드명
 * @param start 1부터 시작하는 byte 위치
 * @param length byte 길이
 * @param type 논리 자료형
 * @param required 필수 여부
 * @param padding padding 문자. NUL이면 자료형별 기본값을 사용합니다.
 * @param alignment padding 정렬
 * @param sensitive 진단 정보 마스킹 여부
 * @param scale 소수점이 생략된 DECIMAL/AMOUNT 필드의 소수 자릿수
 * @param defaultValue 입력 값이 없을 때 사용할 값
 * @param trim parse 시 padding 제거 여부
 * @param converterId 확장 converter 식별자
 */
public record CpfFixedLengthFieldSpec(
        String name,
        int start,
        int length,
        CpfFixedLengthFieldType type,
        boolean required,
        char padding,
        CpfFixedLengthAlignment alignment,
        boolean sensitive,
        int scale,
        String defaultValue,
        boolean trim,
        String converterId) {

    public CpfFixedLengthFieldSpec(
            String name,
            int start,
            int length,
            CpfFixedLengthFieldType type,
            boolean required,
            char padding,
            CpfFixedLengthAlignment alignment,
            boolean sensitive) {
        this(name, start, length, type, required, padding, alignment, sensitive, 0, "", true, "");
    }

    public CpfFixedLengthFieldSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("고정길이 필드명은 필수입니다.");
        }
        name = name.trim();
        if (start < 1) {
            throw new IllegalArgumentException("고정길이 필드 시작 위치는 1 이상이어야 합니다. name=" + name);
        }
        if (length < 1) {
            throw new IllegalArgumentException("고정길이 필드 길이는 1 이상이어야 합니다. name=" + name);
        }
        if (scale < 0) {
            throw new IllegalArgumentException("고정길이 필드 소수 자릿수는 0 이상이어야 합니다. name=" + name);
        }
        type = type == null ? CpfFixedLengthFieldType.STRING : type;
        alignment = alignment == null || alignment == CpfFixedLengthAlignment.AUTO
                ? defaultAlignment(type)
                : alignment;
        padding = padding == '\0' ? defaultPadding(type) : padding;
        defaultValue = defaultValue == null ? "" : defaultValue;
        converterId = converterId == null ? "" : converterId.trim();
        if (type == CpfFixedLengthFieldType.CUSTOM && converterId.isBlank()) {
            throw new IllegalArgumentException("CUSTOM 필드는 converterId가 필요합니다. name=" + name);
        }
    }

    public static CpfFixedLengthFieldSpec of(String name, int start, int length) {
        return new CpfFixedLengthFieldSpec(
                name,
                start,
                length,
                CpfFixedLengthFieldType.STRING,
                false,
                ' ',
                CpfFixedLengthAlignment.LEFT,
                false);
    }

    public int zeroBasedStart() {
        return start - 1;
    }

    public int zeroBasedEndExclusive() {
        return zeroBasedStart() + length;
    }

    private static CpfFixedLengthAlignment defaultAlignment(CpfFixedLengthFieldType type) {
        return switch (type) {
            case STRING, DATE, TIME -> CpfFixedLengthAlignment.LEFT;
            case NUMBER, DECIMAL, AMOUNT, BOOLEAN, CUSTOM -> CpfFixedLengthAlignment.RIGHT;
        };
    }

    private static char defaultPadding(CpfFixedLengthFieldType type) {
        return switch (type) {
            case NUMBER, DECIMAL, AMOUNT -> '0';
            case STRING, DATE, TIME, BOOLEAN, CUSTOM -> ' ';
        };
    }
}
