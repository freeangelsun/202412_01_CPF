package com.cpf.core.api.error;

import static com.cpf.core.api.error.CpfErrorDefinition.Category.*;
import static com.cpf.core.api.error.CpfErrorDefinition.RetryDisposition.*;

/** CPF 내부 프레임워크 오류 의미의 기술중립 정본입니다. HTTP/DB/Broker 상태는 각 Boundary Owner가 매핑합니다. */
public enum CpfFrameworkErrorCode implements CpfErrorDefinition {
    MISSING_TRANSACTION_HEADER("ECPF900001", "MCPF900001", VALIDATION, NEVER,
            "Required transaction header is missing.", "CPF transaction header validation failed. header={0}, uri={1}"),
    INVALID_TRANSACTION_METADATA("ECPF900002", "MCPF900002", INTERNAL, NEVER,
            "Transaction metadata is invalid.", "CPF 표준 실행 메타데이터 검증에 실패했습니다. executionId={0}"),
    SERVICE_ENDPOINT_NOT_FOUND("ECPF900003", "MCPF900003", INFRASTRUCTURE, NEVER,
            "Service endpoint configuration was not found.", "CPF service endpoint configuration was not found. serviceId={0}"),
    DYNAMIC_LOG_RULE_INVALID("ECPF900004", "MCPF900004", VALIDATION, NEVER,
            "Dynamic log-level rule is invalid.", "CPF dynamic log-level rule validation failed. reason={0}"),
    INTERNAL_SERVICE_ACCESS_DENIED("ECPF900005", "MCPF900005", AUTHORIZATION, NEVER,
            "Internal service access is not allowed.", "CPF 내부 서비스 신원 또는 호출 경로 검증에 실패했습니다. reason={0}"),
    INTERNAL_SERVER_ERROR("ECPF990000", "MCPF990000", INTERNAL, NEVER,
            "An internal framework error occurred.", "CPF internal framework error occurred. error={0}");

    private final String statusCode;
    private final String messageCode;
    private final Category category;
    private final RetryDisposition retryDisposition;
    private final String externalMessage;
    private final String internalMessage;

    CpfFrameworkErrorCode(String statusCode, String messageCode, Category category,
                          RetryDisposition retryDisposition, String externalMessage, String internalMessage) {
        this.statusCode=statusCode; this.messageCode=messageCode; this.category=category;
        this.retryDisposition=retryDisposition; this.externalMessage=externalMessage; this.internalMessage=internalMessage;
    }
    @Override public String statusCode(){return statusCode;}
    @Override public String messageCode(){return messageCode;}
    @Override public Category category(){return category;}
    @Override public RetryDisposition retryDisposition(){return retryDisposition;}
    @Override public String defaultExternalMessage(){return externalMessage;}
    @Override public String defaultInternalMessage(){return internalMessage;}
}
