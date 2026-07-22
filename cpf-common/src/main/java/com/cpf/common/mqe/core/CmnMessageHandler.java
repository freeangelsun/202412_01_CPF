package com.cpf.common.mqe.core;

/** 구독한 목적지에서 수신한 메시지 봉투를 처리하는 함수형 계약입니다. */
@FunctionalInterface
public interface CmnMessageHandler {

    /** 수신 메시지를 처리하며 실패 시 예외를 호출자에게 전파합니다. */
    void handle(CmnMessageEnvelope envelope);
}

