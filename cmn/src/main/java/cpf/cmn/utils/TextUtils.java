package cpf.cmn.utils;

import java.util.Locale;

/**
 * 臾몄옄??泥섎━ 怨듯넻 ?좏떥由ы떚?낅땲??
 *
 * <p>?낅Т 媛쒕컻?먭? 諛섎났?곸쑝濡??묒꽦?섎뒗 null/blank 寃?? 湲곕낯媛?泥섎━, 肄붾뱶 ?뺢퇋??濡쒖쭅?? * ??怨녹뿉???ъ슜?섎룄濡??쒓났?⑸땲?? ?낅Т ?섎?媛 ?ㅼ뼱媛?蹂?섏? ???대옒?ㅼ뿉 ?ｌ? ?딄퀬
 * 媛??낅Т ?꾨찓???쒕퉬?ㅼ뿉 ?〓땲??</p>
 */
public final class TextUtils {

    private TextUtils() {
    }

    /**
     * 臾몄옄?댁씠 null???꾨땲怨?怨듬갚留뚯쑝濡?援ъ꽦?섏? ?딆븯?붿? ?뺤씤?⑸땲??
     *
     * @param value 寃?ы븷 臾몄옄??     * @return 媛믪씠 ?덉쑝硫?true
     */
    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 媛믪씠 ?놁쑝硫?湲곕낯媛믪쓣 諛섑솚?⑸땲??
     *
     * @param value        ?먮낯 臾몄옄??     * @param defaultValue 湲곕낯 臾몄옄??     * @return ?먮낯??媛믪씠 ?덉쑝硫??먮낯, ?놁쑝硫?湲곕낯媛?     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    /**
     * ?꾩닔 臾몄옄?댁쓣 寃利앺븯怨??욌뮘 怨듬갚???쒓굅??諛섑솚?⑸땲??
     *
     * @param value     ?먮낯 臾몄옄??     * @param fieldName ?ㅻ쪟 硫붿떆吏???ъ슜???꾨뱶紐?     * @return trim 泥섎━??臾몄옄??     * @throws IllegalArgumentException 媛믪씠 ?놁쓣 ??諛쒖깮
     */
    public static String requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + " 媛믪? ?꾩닔?낅땲??");
        }
        return value.trim();
    }

    /**
     * 肄붾뱶媛?鍮꾧탳瑜??꾪빐 怨듬갚 ?쒓굅 ???臾몄옄濡?蹂?섑빀?덈떎.
     *
     * @param value ?먮낯 肄붾뱶媛?     * @return ?뺢퇋?붾맂 肄붾뱶媛?     */
    public static String normalizeCode(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }
}

