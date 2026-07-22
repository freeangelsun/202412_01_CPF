package com.cpf.core.common.exception;

import org.springframework.http.HttpStatus;

/** CPF 공통 오류가 외부 응답과 내부 운영 로그에 제공해야 하는 표준 계약입니다. */
public interface CpfErrorDefinition {

    /** CPF 표준 상태 코드를 반환합니다. */
    String getStatusCode();

    /** 다국어 메시지 조회에 사용할 메시지 코드를 반환합니다. */
    String getMessageCode();

    /** HTTP 응답 상태를 반환합니다. */
    HttpStatus getHttpStatus();

    /** 메시지 저장소를 사용할 수 없을 때 노출할 기본 외부 메시지를 반환합니다. */
    String getDefaultExternalMessage();

    /** 운영 로그에 기록할 기본 내부 메시지를 반환합니다. */
    String getDefaultInternalMessage();

    /** 외부 메시지 저장소 조회 키를 반환합니다. */
    default String getExternalMessageKey() {
        return getMessageCode();
    }

    /** 내부 메시지 저장소 조회 키를 반환합니다. */
    default String getInternalMessageKey() {
        return getMessageCode();
    }
}

