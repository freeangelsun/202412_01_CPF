package com.cpf.core.api.servicecall;

/** Core가 선택한 대상에 실제 전송만 수행하는 외부 모듈용 callback 계약입니다. */
@FunctionalInterface
public interface CpfServiceTransport<T> {
    T exchange(CpfServiceTarget target);
}
