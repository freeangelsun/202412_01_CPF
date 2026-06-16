package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * PFW core response and message codes.
 */
public enum FpsFrameworkErrorCode implements FpsErrorDefinition {
    MISSING_TRANSACTION_HEADER("EPFW900001", "MPFW900001", HttpStatus.BAD_REQUEST,
            "?꾩닔 嫄곕옒 ?ㅻ뜑媛 ?꾨씫?섏뿀?듬땲??", "PFW 嫄곕옒 ?ㅻ뜑 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. header={0}, uri={1}"),
    INVALID_TRANSACTION_METADATA("EPFW900002", "MPFW900002", HttpStatus.INTERNAL_SERVER_ERROR,
            "嫄곕옒 硫뷀??곗씠???ㅼ젙???щ컮瑜댁? ?딆뒿?덈떎.", "PFW @FpsTransaction 硫뷀??곗씠??寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. transactionId={0}"),
    SERVICE_ENDPOINT_NOT_FOUND("EPFW900003", "MPFW900003", HttpStatus.INTERNAL_SERVER_ERROR,
            "?쒕퉬???묒냽 ?뺣낫媛 ?놁뒿?덈떎.", "PFW ?쒕퉬???붾뱶?ъ씤???ㅼ젙??李얠쓣 ???놁뒿?덈떎. serviceId={0}"),
    DYNAMIC_LOG_RULE_INVALID("EPFW900004", "MPFW900004", HttpStatus.BAD_REQUEST,
            "?숈쟻 濡쒓렇?덈꺼 ?ㅼ젙 ?붿껌???щ컮瑜댁? ?딆뒿?덈떎.", "PFW ?숈쟻 濡쒓렇?덈꺼 洹쒖튃 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. reason={0}"),
    INTERNAL_SERVER_ERROR("EPFW990000", "MPFW990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "泥섎━ 以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.", "PFW ?대? ?ㅻ쪟媛 諛쒖깮?덉뒿?덈떎. error={0}");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    FpsFrameworkErrorCode(
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

