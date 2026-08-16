package com.cpf.messaging.spi.broker;

import java.util.List;

/** 거래 commit 이후 broker 발행을 보장하기 위한 outbox port입니다. */
public interface CpfBrokerOutboxPort {
    CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope);
    List<CpfBrokerEnvelope> claimPending(String workerId, int limit);

    /** Legacy unfenced mutation. Implementations should fail closed when fencing is required. */
    void markPublished(String messageId, CpfBrokerResult result);

    /**
     * Returns whether this adapter enforces worker/process-incarnation ownership on completion.
     * Legacy adapters remain binary compatible but are rejected by durable workers.
     */
    default boolean supportsFencedPublishMutation() {
        return false;
    }

    /** Completes only the claim owned by the supplied process-incarnation worker id. */
    default void markPublished(String workerId, String messageId, CpfBrokerResult result) {
        markPublished(messageId, result);
    }
}
