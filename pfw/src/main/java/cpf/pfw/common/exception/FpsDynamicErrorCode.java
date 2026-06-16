package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * ?낅Т 媛쒕컻?먭? enum 異붽? ?놁씠 ?고??꾩뿉 議곕┰?댁꽌 ?ъ슜?????덈뒗 ?ㅻ쪟?뺤쓽?낅땲??
 *
 * <p>????ㅻ쪟肄붾뱶???ъ궗?⑺븯怨?硫붿떆吏 ?ㅼ? 硫붿떆吏 ?몄옄留??щ━ ?곕㈃,
 * ?ㅻ쪟肄붾뱶媛 ?낅Т 耳?댁뒪蹂꾨줈 遺덊븘?뷀븯寃??섏뼱?섎뒗 臾몄젣瑜?以꾩씪 ???덉뒿?덈떎.</p>
 */
public class FpsDynamicErrorCode implements FpsErrorDefinition {
    private final String statusCode;
    private final String messageCode;
    private final String messageKeyPrefix;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    public FpsDynamicErrorCode(
            String statusCode,
            String messageCode,
            String messageKeyPrefix,
            HttpStatus httpStatus,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.messageKeyPrefix = hasText(messageKeyPrefix) ? messageKeyPrefix : messageCode;
        this.httpStatus = httpStatus;
        this.defaultExternalMessage = defaultExternalMessage;
        this.defaultInternalMessage = defaultInternalMessage;
    }

    /**
     * ?낅Т 洹쒖튃 ?꾨컲 怨꾩뿴???숈쟻 ?ㅻ쪟?뺤쓽瑜??앹꽦?⑸땲??
     *
     * @param messageKeyPrefix 硫붿떆吏 ?뚯씠釉?肄붾뱶. ?? {@code MXYZ090001}
     * @param defaultExternalMessage 硫붿떆吏 ?뚯씠釉?媛믪씠 ?놁쓣 ????怨좉컼??湲곕낯 硫붿떆吏
     * @param defaultInternalMessage 硫붿떆吏 ?뚯씠釉?媛믪씠 ?놁쓣 ?????대? 湲곕낯 硫붿떆吏
     * @return ?낅Т 洹쒖튃 ?꾨컲 ?숈쟻 ?ㅻ쪟?뺤쓽
     */
    public static FpsDynamicErrorCode business(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new FpsDynamicErrorCode(
                FpsErrorCode.BUSINESS_RULE_VIOLATION.getStatusCode(),
                FpsErrorCode.BUSINESS_RULE_VIOLATION.getMessageCode(),
                messageKeyPrefix,
                FpsErrorCode.BUSINESS_RULE_VIOLATION.getHttpStatus(),
                defaultExternalMessage,
                defaultInternalMessage);
    }

    /**
     * 以묐났 ?곗씠??怨꾩뿴???숈쟻 ?ㅻ쪟?뺤쓽瑜??앹꽦?⑸땲??
     *
     * @param messageKeyPrefix 硫붿떆吏 ?뚯씠釉?肄붾뱶. ?? {@code MXYZ090001}
     * @param defaultExternalMessage 硫붿떆吏 ?뚯씠釉?媛믪씠 ?놁쓣 ????怨좉컼??湲곕낯 硫붿떆吏
     * @param defaultInternalMessage 硫붿떆吏 ?뚯씠釉?媛믪씠 ?놁쓣 ?????대? 湲곕낯 硫붿떆吏
     * @return 以묐났 ?곗씠???숈쟻 ?ㅻ쪟?뺤쓽
     */
    public static FpsDynamicErrorCode duplicate(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new FpsDynamicErrorCode(
                FpsErrorCode.DUPLICATE.getStatusCode(),
                FpsErrorCode.DUPLICATE.getMessageCode(),
                messageKeyPrefix,
                FpsErrorCode.DUPLICATE.getHttpStatus(),
                defaultExternalMessage,
                defaultInternalMessage);
    }

    @Override
    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessageCode() {
        return messageCode;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getDefaultExternalMessage() {
        return defaultExternalMessage;
    }

    @Override
    public String getDefaultInternalMessage() {
        return defaultInternalMessage;
    }

    @Override
    public String getExternalMessageKey() {
        return messageKeyPrefix;
    }

    @Override
    public String getInternalMessageKey() {
        return messageKeyPrefix;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

