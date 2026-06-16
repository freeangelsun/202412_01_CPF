package cpf.mbr.common.response;

/**
 * MBR response and message code catalog.
 */
public enum ResponseCode {
    SUCCESS("SMBR000000", "MMBR000000", "?깃났"),
    CREATED("SMBR010001", "MMBR010001", "?앹꽦 ?깃났"),
    UPDATED("SMBR010002", "MMBR010002", "?섏젙 ?깃났"),
    DELETED("SMBR010003", "MMBR010003", "??젣 ?깃났"),

    BAD_REQUEST("EMBR010001", "MMBR010101", "?섎せ???붿껌?낅땲??"),
    INVALID_PARAMETER("EMBR010002", "MMBR010102", "?좏슚?섏? ?딆? ?뚮씪誘명꽣?낅땲??"),
    NOT_FOUND("EMBR010003", "MMBR010103", "?붿껌???먯썝??李얠쓣 ???놁뒿?덈떎."),
    DUPLICATE("EMBR010004", "MMBR010104", "以묐났???곗씠?곌? ?덉뒿?덈떎."),
    VALIDATION_FAILED("EMBR010005", "MMBR010105", "?낅젰媛?寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎."),
    UNAUTHORIZED("EMBR010006", "MMBR010106", "?몄쬆???꾩슂?⑸땲??"),
    FORBIDDEN("EMBR010007", "MMBR010107", "?묎렐 沅뚰븳???놁뒿?덈떎."),

    INTERNAL_SERVER_ERROR("EMBR990000", "MMBR990000", "?대? ?쒕쾭 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎."),
    DATABASE_ERROR("EMBR990001", "MMBR990001", "?곗씠?곕쿋?댁뒪 ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎."),
    EXTERNAL_SERVICE_ERROR("EMBR030001", "MMBR030001", "?몃? ?쒕퉬???ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.");

    private final String code;
    private final String messageCode;
    private final String message;

    ResponseCode(String code, String messageCode, String message) {
        this.code = code;
        this.messageCode = messageCode;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getMessage() {
        return message;
    }
}

