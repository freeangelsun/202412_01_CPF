package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/**
 * PFW core response and message codes.
 */
public enum FpsFrameworkErrorCode implements FpsErrorDefinition {
    MISSING_TRANSACTION_HEADER("EPFW900001", "MPFW900001", HttpStatus.BAD_REQUEST,
            "Required transaction header is missing.", "PFW transaction header validation failed. header={0}, uri={1}"),
    INVALID_TRANSACTION_METADATA("EPFW900002", "MPFW900002", HttpStatus.INTERNAL_SERVER_ERROR,
            "Transaction metadata is invalid.", "PFW @FpsTransaction metadata validation failed. transactionId={0}"),
    SERVICE_ENDPOINT_NOT_FOUND("EPFW900003", "MPFW900003", HttpStatus.INTERNAL_SERVER_ERROR,
            "Service endpoint configuration was not found.", "PFW service endpoint configuration was not found. serviceId={0}"),
    DYNAMIC_LOG_RULE_INVALID("EPFW900004", "MPFW900004", HttpStatus.BAD_REQUEST,
            "Dynamic log-level rule is invalid.", "PFW dynamic log-level rule validation failed. reason={0}"),
    INTERNAL_SERVER_ERROR("EPFW990000", "MPFW990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "An internal framework error occurred.", "PFW internal framework error occurred. error={0}");

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
