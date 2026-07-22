package com.cpf.core.common.exception;

import org.springframework.http.HttpStatus;

/**
 * CPF에서 공통으로 사용하는 표준 오류 코드입니다.
 */
public enum CpfErrorCode implements CpfErrorDefinition {
    INVALID_PARAMETER("ECPF010001", "MCPF010001", HttpStatus.BAD_REQUEST,
            "요청 값이 올바르지 않습니다.", "요청 파라미터 검증에 실패했습니다. field={0}, value={1}"),
    NOT_FOUND("ECPF010002", "MCPF010002", HttpStatus.NOT_FOUND,
            "요청한 정보를 찾을 수 없습니다.", "조회 대상 데이터가 존재하지 않습니다. target={0}"),
    DUPLICATE("ECPF010003", "MCPF010003", HttpStatus.CONFLICT,
            "이미 등록된 정보입니다.", "중복 데이터가 감지되었습니다. key={0}"),
    VALIDATION_FAILED("ECPF010004", "MCPF010004", HttpStatus.BAD_REQUEST,
            "입력값을 확인해 주세요.", "Bean Validation 검증에 실패했습니다. field={0}"),
    UNAUTHORIZED("ECPF010005", "MCPF010005", HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다.", "인증되지 않은 요청입니다."),
    FORBIDDEN("ECPF010006", "MCPF010006", HttpStatus.FORBIDDEN,
            "처리 권한이 없습니다.", "인가되지 않은 요청입니다. user={0}"),
    BUSINESS_RULE_VIOLATION("ECPF020001", "MCPF020001", HttpStatus.BAD_REQUEST,
            "요청을 처리할 수 없습니다.", "업무 규칙 위반이 발생했습니다. rule={0}"),
    EXTERNAL_SERVICE_ERROR("ECPF030001", "MCPF030001", HttpStatus.BAD_GATEWAY,
            "일시적으로 처리할 수 없습니다.", "외부 또는 타 주제영역 연계 오류가 발생했습니다. service={0}"),
    INTERNAL_SERVER_ERROR("ECPF990000", "MCPF990000", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "서버 내부 오류가 발생했습니다. error={0}"),
    DATABASE_ERROR("ECPF990001", "MCPF990001", HttpStatus.INTERNAL_SERVER_ERROR,
            "처리 중 오류가 발생했습니다.", "데이터베이스 처리 오류가 발생했습니다. sqlState={0}");

    private final String statusCode;
    private final String messageCode;
    private final HttpStatus httpStatus;
    private final String defaultExternalMessage;
    private final String defaultInternalMessage;

    CpfErrorCode(
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
