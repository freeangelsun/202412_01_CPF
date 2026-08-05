package com.cpf.core.common.broker;

/** broker 수신 중복 처리와 처리 완료 기록을 위한 inbox port입니다. */
public interface CpfBrokerInboxPort {
    boolean markReceived(String messageId, String idempotencyKey);
    void markConsumed(String messageId, CpfBrokerResult result);

    /** Preserves a result-unknown state after business handling succeeded but durable finalization was uncertain. */
    default void markConsumerUnknown(String messageId, String detail) {
        markConsumed(messageId, CpfBrokerResult.failed(messageId, "CPF_INBOX", detail));
    }
}
