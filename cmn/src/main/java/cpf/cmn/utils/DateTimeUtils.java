package cpf.cmn.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ?좎쭨/?쒓컙 泥섎━ 怨듯넻 ?좏떥由ы떚?낅땲??
 *
 * <p>湲덉쑖 ?낅Т?먯꽌 ?먯＜ ?ъ슜?섎뒗 ?곸뾽??臾몄옄?? 諛由ъ큹 ?ы븿 ?쇱떆 臾몄옄??
 * ?붾㈃ ?쒖떆???쇱떆 臾몄옄?댁쓣 ?쒖? ?щ㎎?쇰줈 ?쒓났?⑸땲??</p>
 */
public final class DateTimeUtils {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    public static final DateTimeFormatter DATETIME_MILLIS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private DateTimeUtils() {
    }

    /**
     * ?ㅻ뒛 ?좎쭨瑜?yyyyMMdd ?뺤떇?쇰줈 諛섑솚?⑸땲??
     *
     * @return ?ㅻ뒛 ?좎쭨 臾몄옄??     */
    public static String today() {
        return LocalDate.now().format(DATE_FORMAT);
    }

    /**
     * ?꾩옱 ?쇱떆瑜?yyyyMMddHHmmss ?뺤떇?쇰줈 諛섑솚?⑸땲??
     *
     * @return ?꾩옱 ?쇱떆 臾몄옄??     */
    public static String nowDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMAT);
    }

    /**
     * ?꾩옱 ?쇱떆瑜?yyyyMMddHHmmssSSS ?뺤떇?쇰줈 諛섑솚?⑸땲??
     *
     * @return 諛由ъ큹 ?ы븿 ?꾩옱 ?쇱떆 臾몄옄??     */
    public static String nowDateTimeMillis() {
        return LocalDateTime.now().format(DATETIME_MILLIS_FORMAT);
    }

    /**
     * yyyyMMdd ?뺤떇 臾몄옄?댁쓣 LocalDate濡?蹂?섑빀?덈떎.
     *
     * @param value yyyyMMdd 臾몄옄??     * @return 蹂?섎맂 ?좎쭨
     */
    public static LocalDate parseDate(String value) {
        return LocalDate.parse(TextUtils.requireText(value, "date"), DATE_FORMAT);
    }
}

