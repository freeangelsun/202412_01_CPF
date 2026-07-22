package com.cpf.core.common.exception;

import org.springframework.http.HttpStatus;

/** 코드 enum을 추가하지 않고도 업무별 메시지 키를 조합할 수 있는 오류 정의입니다. */
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

    /** 업무 규칙 위반 상태를 사용하는 동적 오류 코드를 생성합니다. */
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

    /** 중복 자원 상태를 사용하는 동적 오류 코드를 생성합니다. */
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

