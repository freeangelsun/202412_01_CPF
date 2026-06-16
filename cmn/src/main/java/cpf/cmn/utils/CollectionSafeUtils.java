package cpf.cmn.utils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 而щ젆??null ?덉쟾 泥섎━ ?좏떥由ы떚?낅땲??
 *
 * <p>Spring??CollectionUtils? ?대쫫??寃뱀튂吏 ?딅룄濡?{@code CollectionSafeUtils}?쇰뒗
 * 紐낆묶???ъ슜?⑸땲?? ?낅Т 肄붾뱶?먯꽌??null 而щ젆?섏쓣 吏곸젒 ?쒗쉶?섏? 留먭퀬 ???좏떥???듯빐
 * 鍮?而щ젆?섏쑝濡?移섑솚?섍굅??議댁옱 ?щ?瑜??뺤씤?⑸땲??</p>
 */
public final class CollectionSafeUtils {

    private CollectionSafeUtils() {
    }

    /**
     * 而щ젆?섏씠 null?닿굅??鍮꾩뼱 ?덈뒗吏 ?뺤씤?⑸땲??
     *
     * @param source 寃?????而щ젆??     * @return null ?먮뒗 empty?대㈃ true
     */
    public static boolean isEmpty(Collection<?> source) {
        return source == null || source.isEmpty();
    }

    /**
     * Map??null?닿굅??鍮꾩뼱 ?덈뒗吏 ?뺤씤?⑸땲??
     *
     * @param source 寃?????Map
     * @return null ?먮뒗 empty?대㈃ true
     */
    public static boolean isEmpty(Map<?, ?> source) {
        return source == null || source.isEmpty();
    }

    /**
     * null List瑜?鍮?List濡?蹂?섑빀?덈떎.
     *
     * @param source ?먮낯 List
     * @param <T>    ??ぉ ???     * @return ?먮낯??null?대㈃ 鍮?List
     */
    public static <T> List<T> emptyIfNull(List<T> source) {
        return source == null ? List.of() : source;
    }
}

