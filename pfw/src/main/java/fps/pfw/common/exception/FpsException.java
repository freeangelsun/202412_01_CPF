package fps.pfw.common.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FPS 표준 예외의 최상위 클래스입니다.
 *
 * <p>외부 고객에게 보여줄 메시지와 내부 로그에 남길 메시지를 분리합니다.
 * 고객용 메시지는 {@code EXTERNAL}, 운영/개발자용 메시지는 {@code INTERNAL} 메시지로 관리합니다.</p>
 */
public class FpsException extends RuntimeException {
    private final FpsErrorDefinition errorCode;
    private final String externalMessage;
    private final String internalMessage;
    private final String detail;
    private final Map<String, Object> messageArguments;

    public FpsException(FpsErrorDefinition errorCode) {
        this(errorCode, null, null, null, null, null);
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
        return errorCode != null ? errorCode.getDefaultInternalMessage() : "FPS 표준 예외가 발생했습니다.";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
