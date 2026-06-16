package cpf.pfw.common.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FPS ?쒖? ?덉쇅??理쒖긽???대옒?ㅼ엯?덈떎.
 *
 * <p>?몃? 怨좉컼?먭쾶 蹂댁뿬以?硫붿떆吏? ?대? 濡쒓렇???④만 硫붿떆吏瑜?遺꾨━?⑸땲??
 * 怨좉컼??硫붿떆吏??{@code EXTERNAL}, ?댁쁺/媛쒕컻?먯슜 硫붿떆吏??{@code INTERNAL} 硫붿떆吏濡?愿由ы빀?덈떎.</p>
 */
public class FpsException extends RuntimeException {
    private final FpsErrorDefinition errorCode;
    private final String responseCode;
    private final String externalMessage;
    private final String internalMessage;
    private final String detail;
    private final Map<String, Object> messageArguments;

    public FpsException(FpsErrorDefinition errorCode) {
        this(errorCode, null, null, null, null, null);
    }

    public FpsException(String responseCode) {
        this(responseCode, null, null, null);
    }

    public FpsException(String responseCode, String detail) {
        this(responseCode, detail, null, null);
    }

    public FpsException(String responseCode, String detail, Map<String, Object> messageArguments) {
        this(responseCode, detail, null, messageArguments);
    }

    public FpsException(String responseCode, String detail, Throwable cause, Map<String, Object> messageArguments) {
        super(hasText(detail) ? detail : responseCode, cause);
        this.errorCode = null;
        this.responseCode = responseCode;
        this.externalMessage = null;
        this.internalMessage = null;
        this.detail = detail;
        this.messageArguments = messageArguments == null || messageArguments.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(messageArguments));
    }

    public FpsException(FpsErrorDefinition errorCode, String detail) {
        this(errorCode, null, null, detail, null, null);
    }

    public FpsException(FpsErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        this(errorCode, null, null, detail, null, messageArguments);
    }

    public FpsException(FpsErrorDefinition errorCode, String externalMessage, String internalMessage, String detail) {
        this(errorCode, externalMessage, internalMessage, detail, null, null);
    }

    public FpsException(
            FpsErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Throwable cause) {
        this(errorCode, externalMessage, internalMessage, detail, cause, null);
    }

    public FpsException(
            FpsErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Throwable cause,
            Map<String, Object> messageArguments) {
        super(resolveInternalMessage(errorCode, internalMessage, detail), cause);
        this.errorCode = errorCode;
        this.responseCode = errorCode != null ? errorCode.getStatusCode() : null;
        this.externalMessage = externalMessage;
        this.internalMessage = internalMessage;
        this.detail = detail;
        this.messageArguments = messageArguments == null || messageArguments.isEmpty()
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(messageArguments));
    }

    public FpsErrorDefinition getErrorCode() {
        return errorCode;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getExternalMessage() {
        return externalMessage;
    }

    public String getInternalMessage() {
        return internalMessage;
    }

    public String getDetail() {
        return detail;
    }

    public Map<String, Object> getMessageArguments() {
        return messageArguments;
    }

    private static String resolveInternalMessage(FpsErrorDefinition errorCode, String internalMessage, String detail) {
        if (hasText(internalMessage)) {
            return internalMessage;
        }
        if (hasText(detail)) {
            return detail;
        }
        return errorCode != null ? errorCode.getDefaultInternalMessage() : "FPS ?쒖? ?덉쇅媛 諛쒖깮?덉뒿?덈떎.";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

