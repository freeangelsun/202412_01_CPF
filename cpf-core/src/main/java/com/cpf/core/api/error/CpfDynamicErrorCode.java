package com.cpf.core.api.error;

import org.springframework.http.HttpStatus;

/** Enum 수정 없이 업무별 메시지 키를 구성하는 공개 동적 오류 정의입니다. */
public class CpfDynamicErrorCode
        extends com.cpf.core.common.exception.CpfDynamicErrorCode
        implements CpfErrorDefinition {

    public CpfDynamicErrorCode(
            String statusCode,
            String messageCode,
            String messageKeyPrefix,
            HttpStatus httpStatus,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        super(
                statusCode,
                messageCode,
                messageKeyPrefix,
                httpStatus,
                defaultExternalMessage,
                defaultInternalMessage);
    }

    public static CpfDynamicErrorCode business(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return from(
                CpfErrorCode.BUSINESS_RULE_VIOLATION,
                messageKeyPrefix,
                defaultExternalMessage,
                defaultInternalMessage);
    }

    public static CpfDynamicErrorCode duplicate(
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return from(
                CpfErrorCode.DUPLICATE,
                messageKeyPrefix,
                defaultExternalMessage,
                defaultInternalMessage);
    }

    private static CpfDynamicErrorCode from(
            CpfErrorCode errorCode,
            String messageKeyPrefix,
            String defaultExternalMessage,
            String defaultInternalMessage) {
        return new CpfDynamicErrorCode(
                errorCode.statusCode(),
                errorCode.messageCode(),
                messageKeyPrefix,
                errorCode.httpStatus(),
                defaultExternalMessage,
                defaultInternalMessage);
    }
}
