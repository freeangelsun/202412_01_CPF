package com.cpf.core.api.error;

import org.springframework.http.HttpStatus;

/** 외부 모듈이 CPF 응답 코드와 메시지 계약을 구현할 때 사용하는 공개 오류 정의입니다. */
public interface CpfErrorDefinition {
    String getStatusCode();
    String getMessageCode();
    HttpStatus getHttpStatus();
    String getDefaultExternalMessage();
    String getDefaultInternalMessage();

    default String getExternalMessageKey() {
        return getMessageCode();
    }

    default String getInternalMessageKey() {
        return getMessageCode();
    }
}
