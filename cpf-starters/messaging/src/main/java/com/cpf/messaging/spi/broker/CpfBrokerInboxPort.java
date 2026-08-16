package com.cpf.messaging.spi.broker;

/** Broker 수신 중복 처리와 처리 완료 기록을 위한 Inbox SPI입니다. */
public interface CpfBrokerInboxPort {
    boolean markReceived(String messageId, String idempotencyKey);
    void markConsumed(String messageId, CpfBrokerResult result);

    /** 여러 Consumer가 같은 messageId를 독립 처리할 때 사용하는 canonical 계약입니다. */
    default boolean markReceived(String consumerIdentity,String messageId,String idempotencyKey) {
        return markReceived(messageId,idempotencyKey);
    }
    default void markConsumed(String consumerIdentity,String messageId,CpfBrokerResult result) {
        markConsumed(messageId,result);
    }
    default void markConsumerUnknown(String messageId,String detail) {
        markConsumed(messageId,CpfBrokerResult.failed(messageId,"CPF_INBOX",detail));
    }
    default void markConsumerUnknown(String consumerIdentity,String messageId,String detail) {
        markConsumerUnknown(messageId,detail);
    }
}
