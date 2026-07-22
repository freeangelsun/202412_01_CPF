package com.cpf.core.common.exception;

import org.springframework.http.HttpStatus;

/** CPF 코어에서 사용하는 표준 응답·메시지 코드입니다. */
public enum CpfFrameworkErrorCode implements CpfErrorDefinition {
    MISSING_TRANSACTION_HEADER("ECPF900001", "MCPF900001", HttpStatus.BAD_REQUEST,
            "Required transaction header is missing.", "CPF transaction header validation failed. header={0}, uri={1}"),
    INVALID_TRANSACTION_METADATA("ECPF900002", "MCPF900002", HttpStatus.INTERNAL_SERVER_ERROR,
            "Transaction metadata is invalid.", "CPF 표준 실행 메타데이터 검증에 실패했습니다. executionId={0}"),
    SERVICE_ENDPOINT_NOT_FOUND("ECPF900003", "MCPF900003", HttpStatus.INTERNAL_SERVER_ERROR,
            "Service endpoint configuration was not found.", "CPF service endpoint configuration was not found. serviceId={0}"),
    DYNAMIC_LOG_RULE_INVALID("ECPF900004", "MCPF900004", HttpStatus.BAD_REQUEST,
            "Dynamic log-level rule is invalid.", "CPF dynamic log-level rule validation failed. reason={0}"),
    INTERNAL_SERVICE_ACCESS_DENIED("ECPF900005", "MCPF900005", HttpStatus.FORBIDDEN,
            "Internal service access is not allowed.", "CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}"),
    INTERNAL_SERVER_ERROR("ECPF990000", "MCPF990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "An internal framework error occurred.", "CPF internal framework error occurred. error={0}");

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
