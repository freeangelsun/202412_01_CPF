package com.cpf.common.tlm.core;

import com.cpf.core.common.exception.CpfValidationException;

/** 고정길이 전문 필드 하나의 위치·길이·자료형·채움 규칙을 표현합니다. */
public record CmnTelegramFieldSpec(
        String name,
        int order,
        int length,
        CmnTelegramFieldType type,
        CmnTelegramAlign align,
        char padding,
        String defaultValue,
        int scale,
        boolean trim) {

    public CmnTelegramFieldSpec {
        if (name == null || name.isBlank()) {
            throw new CpfValidationException("전문 필드명은 비어 있을 수 없습니다.");
        }
        if (order <= 0) {
            throw new CpfValidationException("전문 필드 순서는 1 이상이어야 합니다. field=" + name);
        }
        if (length <= 0) {
            throw new CpfValidationException("전문 필드 길이는 1 이상이어야 합니다. field=" + name);
        }
        if (scale < 0) {
            throw new CpfValidationException("전문 필드 소수 자릿수는 0 이상이어야 합니다. field=" + name);
        }
        type = type == null ? CmnTelegramFieldType.STRING : type;
        align = align == null ? CmnTelegramAlign.AUTO : align;
        defaultValue = defaultValue == null ? "" : defaultValue;
    }

    public static CmnTelegramFieldSpec of(String name, int order, int length, CmnTelegramFieldType type) {
        return new CmnTelegramFieldSpec(name, order, length, type, CmnTelegramAlign.AUTO, '\0', "", 0, true);
    }

    public CmnTelegramAlign resolvedAlign() {
        if (align != CmnTelegramAlign.AUTO) {
            return align;
        }
        return type == CmnTelegramFieldType.STRING || type == CmnTelegramFieldType.DATE
                ? CmnTelegramAlign.LEFT
                : CmnTelegramAlign.RIGHT;
    }

    public char resolvedPadding() {
        if (padding != '\0') {
            return padding;
        }
        return type == CmnTelegramFieldType.STRING || type == CmnTelegramFieldType.DATE ? ' ' : '0';
    }
}

