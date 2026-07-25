package com.cpf.core.api.error;

import java.util.Map;

/**
 * 업무/요청 입력 검증 실패를 Generated Domain에 노출하기 위한 공개 예외입니다.
 *
 * <p>기존 CPF 표준 예외를 상속하므로 Global Exception Handler, 응답 코드/메시지 Resolver와
 * 동일한 처리 경로를 사용합니다.</p>
 */
public class CpfValidationException extends com.cpf.core.common.exception.CpfValidationException {
    public CpfValidationException(String detail) {
        super(detail);
    }

    public CpfValidationException(CpfErrorCode errorCode, String detail, Map<String, Object> messageArguments) {
        super(errorCode.internalDefinition(), detail, messageArguments);
    }
}
