package cpf.pfw.common.exception;

import org.springframework.http.HttpStatus;

/** PFW 코어에서 사용하는 표준 응답·메시지 코드입니다. */
public enum CpfFrameworkErrorCode implements CpfErrorDefinition {
    MISSING_TRANSACTION_HEADER("EPFW900001", "MPFW900001", HttpStatus.BAD_REQUEST,
            "Required transaction header is missing.", "PFW transaction header validation failed. header={0}, uri={1}"),
    INVALID_TRANSACTION_METADATA("EPFW900002", "MPFW900002", HttpStatus.INTERNAL_SERVER_ERROR,
            "Transaction metadata is invalid.", "PFW 표준 실행 메타데이터 검증에 실패했습니다. executionId={0}"),
    SERVICE_ENDPOINT_NOT_FOUND("EPFW900003", "MPFW900003", HttpStatus.INTERNAL_SERVER_ERROR,
            "Service endpoint configuration was not found.", "PFW service endpoint configuration was not found. serviceId={0}"),
    DYNAMIC_LOG_RULE_INVALID("EPFW900004", "MPFW900004", HttpStatus.BAD_REQUEST,
            "Dynamic log-level rule is invalid.", "PFW dynamic log-level rule validation failed. reason={0}"),
    INTERNAL_SERVICE_ACCESS_DENIED("EPFW900005", "MPFW900005", HttpStatus.FORBIDDEN,
            "Internal service access is not allowed.", "PFW 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}"),
    INTERNAL_SERVER_ERROR("EPFW990000", "MPFW990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "An internal framework error occurred.", "PFW internal framework error occurred. error={0}");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    CpfFrameworkErrorCode(
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
