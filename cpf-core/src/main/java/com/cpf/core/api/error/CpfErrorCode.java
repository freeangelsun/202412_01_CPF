package com.cpf.core.api.error;

import static com.cpf.core.api.error.CpfErrorDefinition.Category.*;
import static com.cpf.core.api.error.CpfErrorDefinition.RetryDisposition.*;

/** CPF 공통 오류 의미의 기술중립 정본입니다. */
public enum CpfErrorCode implements CpfErrorDefinition {
    INVALID_PARAMETER("ECPF010001", "MCPF010001", VALIDATION, NEVER, "요청 값이 올바르지 않습니다.", "요청 파라미터 검증 실패"),
    NOT_FOUND("ECPF010002", "MCPF010002", CpfErrorDefinition.Category.NOT_FOUND, NEVER, "요청한 정보를 찾을 수 없습니다.", "조회 대상 없음"),
    DUPLICATE("ECPF010003", "MCPF010003", CpfErrorDefinition.Category.CONFLICT, NEVER, "이미 등록된 정보입니다.", "중복 데이터"),
    VALIDATION_FAILED("ECPF010004", "MCPF010004", VALIDATION, NEVER, "입력값을 확인해 주세요.", "입력 검증 실패"),
    UNAUTHORIZED("ECPF010005", "MCPF010005", AUTHENTICATION, NEVER, "인증이 필요합니다.", "인증되지 않은 요청"),
    FORBIDDEN("ECPF010006", "MCPF010006", AUTHORIZATION, NEVER, "처리 권한이 없습니다.", "인가되지 않은 요청"),
    CONFLICT("ECPF010007", "MCPF010007", CpfErrorDefinition.Category.CONFLICT, NEVER, "다른 요청에 의해 정보가 변경되었습니다.", "상태/동시성 충돌"),
    RATE_LIMITED("ECPF010008", "MCPF010008", RATE_LIMIT, SAFE, "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요.", "요청 한도 초과"),
    BUSINESS_RULE_VIOLATION("ECPF020001", "MCPF020001", BUSINESS, NEVER, "요청을 처리할 수 없습니다.", "업무 규칙 위반"),
    EXTERNAL_SERVICE_ERROR("ECPF030001", "MCPF030001", EXTERNAL, SAFE, "일시적으로 처리할 수 없습니다.", "외부 연계 오류"),
    EXTERNAL_UNKNOWN_OUTCOME("ECPF030002", "MCPF030002", EXTERNAL, RECONCILE, "처리 결과를 확인 중입니다.", "외부 연계 결과 UNKNOWN"),
    DATABASE_ERROR("ECPF990001", "MCPF990001", INFRASTRUCTURE, UNKNOWN, "처리 중 오류가 발생했습니다.", "데이터 저장소 오류"),
    INFRASTRUCTURE_UNAVAILABLE("ECPF990002", "MCPF990002", INFRASTRUCTURE, SAFE, "일시적으로 서비스를 사용할 수 없습니다.", "필수 인프라 사용 불가"),
    INTERNAL_SERVER_ERROR("ECPF990000", "MCPF990000", INTERNAL, NEVER, "처리 중 오류가 발생했습니다.", "내부 오류");

    private final String statusCode;
    private final String messageCode;
    private final Category category;
    private final RetryDisposition retryDisposition;
    private final String externalMessage;
    private final String internalMessage;

    CpfErrorCode(String statusCode, String messageCode, Category category,
                 RetryDisposition retryDisposition, String externalMessage, String internalMessage) {
        this.statusCode = statusCode;
        this.messageCode = messageCode;
        this.category = category;
        this.retryDisposition = retryDisposition;
        this.externalMessage = externalMessage;
        this.internalMessage = internalMessage;
    }

    @Override public String statusCode() { return statusCode; }
    @Override public String messageCode() { return messageCode; }
    @Override public Category category() { return category; }
    @Override public RetryDisposition retryDisposition() { return retryDisposition; }
    @Override public String defaultExternalMessage() { return externalMessage; }
    @Override public String defaultInternalMessage() { return internalMessage; }
    public CpfErrorDefinition internalDefinition() { return this; }
}
