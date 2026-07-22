package com.cpf.core.common.broker;

/**
 * broker 수신 중복 처리와 처리 완료 기록을 위한 inbox port입니다.
 */
public interface CpfBrokerInboxPort {

    boolean markReceived(String messageId, String idempotencyKey);

    void markConsumed(String messageId, CpfBrokerResult result);
}
