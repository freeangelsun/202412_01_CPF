package cpf.pfw.common.exception;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPF 표준 오류 응답을 만들기 위한 공통 예외입니다.
 *
 * 응답코드 기반 예외와 enum 기반 예외를 모두 지원하며, 메시지 치환 인자를 함께 전달합니다.
 */
public class CpfException extends RuntimeException {
    private final CpfErrorDefinition errorCode;
    private final String responseCode;
    private final String externalMessage;
    private final String internalMessage;
    private final String detail;
    private final Map<String, Object> messageArguments;

    public CpfException(CpfErrorDefinition errorCode) {
        this(errorCode, null, null, null, null, null);
    }

    public CpfException(String responseCode) {
        this(responseCode, null, null, null);
    }

    public CpfException(String responseCode, String detail) {
        this(responseCode, detail, null, null);
    }

    public CpfException(String responseCode, String detail, Map<String, Object> messageArguments) {
        this(responseCode, detail, null, messageArguments);
    }

    public CpfException(String responseCode, String detail, Throwable cause, Map<String, Object> messageArguments) {
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

    public CpfException(CpfErrorDefinition errorCode, String detail) {
        this(errorCode, null, null, detail, null, null);
    }

    public CpfException(CpfErrorDefinition errorCode, String detail, Map<String, Object> messageArguments) {
        this(errorCode, null, null, detail, null, messageArguments);
    }

    public CpfException(CpfErrorDefinition errorCode, String externalMessage, String internalMessage, String detail) {
        this(errorCode, externalMessage, internalMessage, detail, null, null);
    }

    public CpfException(
            CpfErrorDefinition errorCode,
            String externalMessage,
            String internalMessage,
            String detail,
            Throwable cause) {
        this(errorCode, externalMessage, internalMessage, detail, cause, null);
    }

    public CpfException(
            CpfErrorDefinition errorCode,
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

    public CpfErrorDefinition getErrorCode() {
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

    private static String resolveInternalMessage(CpfErrorDefinition errorCode, String internalMessage, String detail) {
        if (hasText(internalMessage)) {
            return internalMessage;
        }
        if (hasText(detail)) {
            return detail;
        }
        return errorCode != null ? errorCode.getDefaultInternalMessage() : "CPF 처리 중 오류가 발생했습니다.";
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
