package com.cpf.core.common.broker;

/**
 * broker consumer가 업무 처리를 위임하는 표준 함수형 포트입니다.
 */
@FunctionalInterface
public interface CpfBrokerMessageHandler {
    CpfBrokerResult handle(CpfBrokerEnvelope envelope);
}
