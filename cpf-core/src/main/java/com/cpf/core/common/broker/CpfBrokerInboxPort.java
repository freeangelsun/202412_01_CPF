package com.cpf.core.common.broker;

/** broker 수신 중복 처리와 처리 완료 기록을 위한 inbox port입니다. Consumer identity는 dedup namespace입니다. */
public interface CpfBrokerInboxPort {
    default boolean markReceived(String messageId, String idempotencyKey) { return markReceived("default", messageId, idempotencyKey); }
    boolean markReceived(String consumerIdentity, String messageId, String idempotencyKey);

    default void markConsumed(String messageId, CpfBrokerResult result) { markConsumed("default", messageId, result); }
    void markConsumed(String consumerIdentity, String messageId, CpfBrokerResult result);

    default void markConsumerUnknown(String messageId, String detail) { markConsumerUnknown("default", messageId, detail); }
    default void markConsumerUnknown(String consumerIdentity, String messageId, String detail) {
        markConsumed(consumerIdentity, messageId, CpfBrokerResult.failed(messageId, "CPF_INBOX", detail));
    }
}
