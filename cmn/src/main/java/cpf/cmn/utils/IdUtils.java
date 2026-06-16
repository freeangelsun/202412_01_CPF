package cpf.cmn.utils;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * ?낅Т?먯꽌 ?먯＜ ?곕뒗 ?꾩떆 ?앸퀎???앹꽦 ?좏떥由ы떚?낅땲??
 *
 * <p>湲濡쒕쾶 嫄곕옒ID??PFW??{@code TransactionIdGenerator}瑜??ъ슜?댁빞 ?⑸땲??
 * ???대옒?ㅻ뒗 ?낅Т ?꾩떆踰덊샇, ?붾㈃ ?붿껌踰덊샇, ?섑뵆 ?곗씠???ㅼ쿂??嫄곕옒ID媛 ?꾨땶 蹂댁“ ?앸퀎?먯뿉留??ъ슜?⑸땲??</p>
 */
public final class IdUtils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private IdUtils() {
    }

    /**
     * ?섏씠???녿뒗 UUID 臾몄옄?댁쓣 ?앹꽦?⑸땲??
     *
     * @return 32?먮━ UUID 臾몄옄??     */
    public static String uuid32() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 吏?뺥븳 ?묐몢?댁? ?좎쭨/?쒖닔瑜?議고빀???낅Т???꾩떆 ID瑜??앹꽦?⑸땲??
     *
     * @param prefix ?낅Т ?묐몢??     * @return ?? TMP20260612095615123456
     */
    public static String temporaryId(String prefix) {
        String safePrefix = TextUtils.defaultIfBlank(prefix, "TMP").trim().toUpperCase();
        int random = SECURE_RANDOM.nextInt(1_000_000);
        return safePrefix + DateTimeUtils.nowDateTimeMillis() + String.format("%06d", random);
    }
}

