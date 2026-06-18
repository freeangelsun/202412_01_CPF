package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public class CpfDynamicErrorCode implements CpfErrorDefinition {
    private final String statusCode;
    private final String messageCode;
    private final String messageKeyPrefix;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    public CpfDynamicErrorCode(
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
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public static CpfDynamicErrorCode business(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new CpfDynamicErrorCode(
                CpfErrorCode.BUSINESS_RULE_VIOLATION.getStatusCode(),
                CpfErrorCode.BUSINESS_RULE_VIOLATION.getMessageCode(),
                messageKeyPrefix,
                CpfErrorCode.BUSINESS_RULE_VIOLATION.getHttpStatus(),
                defaultExternalMessage,
                defaultInternalMessage);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public static CpfDynamicErrorCode duplicate(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new CpfDynamicErrorCode(
                CpfErrorCode.DUPLICATE.getStatusCode(),
                CpfErrorCode.DUPLICATE.getMessageCode(),
                messageKeyPrefix,
                CpfErrorCode.DUPLICATE.getHttpStatus(),
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

