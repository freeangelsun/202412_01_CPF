package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Standard PFW reusable response and message codes.
 */
public enum CpfErrorCode implements CpfErrorDefinition {
    INVALID_PARAMETER("EPFW010001", "MPFW010001", HttpStatus.BAD_REQUEST,
            "?붿껌 媛믪씠 ?щ컮瑜댁? ?딆뒿?덈떎.", "?붿껌 ?뚮씪誘명꽣 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}, value={1}"),
    NOT_FOUND("EPFW010002", "MPFW010002", HttpStatus.NOT_FOUND,
            "?붿껌???뺣낫瑜?李얠쓣 ???놁뒿?덈떎.", "議고쉶 ????곗씠?곌? 議댁옱?섏? ?딆뒿?덈떎. target={0}"),
    DUPLICATE("EPFW010003", "MPFW010003", HttpStatus.CONFLICT,
            "?대? ?깅줉???뺣낫?낅땲??", "以묐났 ?곗씠?곌? 媛먯??섏뿀?듬땲?? key={0}"),
    VALIDATION_FAILED("EPFW010004", "MPFW010004", HttpStatus.BAD_REQUEST,
            "?낅젰媛믪쓣 ?뺤씤??二쇱꽭??", "Bean Validation 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. field={0}"),
    UNAUTHORIZED("EPFW010005", "MPFW010005", HttpStatus.UNAUTHORIZED,
            "?몄쬆???꾩슂?⑸땲??", "?몄쬆?섏? ?딆? ?붿껌?낅땲??"),
    FORBIDDEN("EPFW010006", "MPFW010006", HttpStatus.FORBIDDEN,
            "泥섎━ 沅뚰븳???놁뒿?덈떎.", "?멸??섏? ?딆? ?붿껌?낅땲?? user={0}"),
    BUSINESS_RULE_VIOLATION("EPFW020001", "MPFW020001", HttpStatus.BAD_REQUEST,
            "?붿껌??泥섎━?????놁뒿?덈떎.", "?낅Т 洹쒖튃 ?꾨컲??諛쒖깮?덉뒿?덈떎. rule={0}"),
    EXTERNAL_SERVICE_ERROR("EPFW030001", "MPFW030001", HttpStatus.BAD_GATEWAY,
            "?쇱떆?곸쑝濡?泥섎━?????놁뒿?덈떎.", "?몃? ?먮뒗 ? 二쇱젣?곸뿭 ?곌퀎 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. service={0}"),
    INTERNAL_SERVER_ERROR("EPFW990000", "MPFW990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.", "?쒕쾭 ?대? ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. error={0}"),
    DATABASE_ERROR("EPFW990001", "MPFW990001", HttpStatus.INTERNAL_SERVER_ERROR,
            "泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.", "?곗씠?곕쿋?댁뒪 泥섎━ ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. sqlState={0}");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    CpfErrorCode(
            String statusCode,
            String messageCode,
            HttpStatus httpStatus,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.httpStatus = httpStatus;
        this.defaultExternalMessage = defaultExternalMessage;
        this.defaultInternalMessage = defaultInternalMessage;
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
}

