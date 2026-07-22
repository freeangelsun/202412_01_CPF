package cpf.cmn.tlm.core;

import cpf.pfw.common.exception.CpfValidationException;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
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
            throw new CpfValidationException("CPF 처리 기준입니다.");
        }
        if (order <= 0) {
            throw new CpfValidationException("CPF 처리 기준입니다." + name);
        }
        if (length <= 0) {
            throw new CpfValidationException("CPF 처리 기준입니다." + name);
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

