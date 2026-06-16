package cpf.cmn.utils;

import cpf.pfw.common.logging.SensitiveDataMasker;

/**
 * 媛쒖씤?뺣낫? 誘쇨컧媛?留덉뒪??怨듯넻 ?좏떥由ы떚?낅땲??
 *
 * <p>?ㅼ젣 留덉뒪??洹쒖튃? PFW??{@link SensitiveDataMasker}瑜??ъ슜?⑸땲??
 * ?낅Т 媛쒕컻?먮뒗 濡쒓렇???섑뵆 ?묐떟?먯꽌 誘쇨컧媛믪쓣 吏곸젒 ?ㅻ（湲곕낫?????좏떥???듯빐
 * ?꾨젅?꾩썙??湲곗?怨?媛숈? 洹쒖튃???ъ슜?⑸땲??</p>
 */
public final class MaskingUtils {

    private MaskingUtils() {
    }

    /**
     * PFW 誘쇨컧?뺣낫 留덉뒪??洹쒖튃?쇰줈 臾몄옄?댁쓣 留덉뒪?뱁빀?덈떎.
     *
     * @param value ?먮낯 臾몄옄??     * @return 留덉뒪?밸맂 臾몄옄??     */
    public static String maskSensitive(String value) {
        return SensitiveDataMasker.mask(value);
    }

    /**
     * ?대쫫??媛?대뜲 ?곸뿭??留덉뒪?뱁빀?덈떎.
     *
     * @param name ?대쫫
     * @return 留덉뒪?밸맂 ?대쫫
     */
    public static String maskName(String name) {
        if (!TextUtils.hasText(name)) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.length() <= 1) {
            return "*";
        }
        if (trimmed.length() == 2) {
            return trimmed.charAt(0) + "*";
        }
        return trimmed.charAt(0) + "*".repeat(trimmed.length() - 2) + trimmed.charAt(trimmed.length() - 1);
    }
}

