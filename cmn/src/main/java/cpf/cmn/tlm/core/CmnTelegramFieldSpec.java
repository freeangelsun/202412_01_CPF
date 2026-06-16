package cpf.cmn.tlm.core;

import cpf.pfw.common.exception.FpsValidationException;

/**
 * 怨좎젙湲몄씠 ?꾨Ц ?꾨뱶 ?뺤쓽?낅땲??
 *
 * @param name         ?꾨뱶紐? * @param order        ?꾨뱶 ?쒖꽌
 * @param length       怨좎젙 湲몄씠
 * @param type         ?먮즺?? * @param align        ?뺣젹 諛⑹떇
 * @param padding      ?⑤뵫 臾몄옄. '\0'?대㈃ ?먮즺??湲곕낯 ?⑤뵫 ?ъ슜
 * @param defaultValue 湲곕낯媛? * @param scale        DECIMAL ?뚯닔 ?먮━?? * @param trim         ?뚯떛 ??怨듬갚/?⑤뵫 ?쒓굅 ?щ?
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
            throw new FpsValidationException("?꾨Ц ?꾨뱶紐낆? ?꾩닔?낅땲??");
        }
        if (order <= 0) {
            throw new FpsValidationException("?꾨Ц ?꾨뱶 ?쒖꽌??1 ?댁긽?댁뼱???⑸땲?? field=" + name);
        }
        if (length <= 0) {
            throw new FpsValidationException("?꾨Ц ?꾨뱶 湲몄씠??1 ?댁긽?댁뼱???⑸땲?? field=" + name);
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

