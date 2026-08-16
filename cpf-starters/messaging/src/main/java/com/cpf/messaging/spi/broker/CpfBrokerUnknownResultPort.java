package com.cpf.messaging.spi.broker;

import java.time.Instant;
import java.util.List;

/** Result-unknown broker publication recovery port. */
/** CpfBrokerUnknownResultPort는 Broker 처리 결과의 확인·복구 경계를 외부 구현과 분리하는 SPI입니다. */
public interface CpfBrokerUnknownResultPort {
    void markUnknown(String messageId, CpfBrokerResult result, Instant nextReconcileAt);
    List<CpfBrokerEnvelope> claimUnknown(String workerId, int limit);
    void releaseUnknown(String messageId, String detail, Instant nextReconcileAt);

    /**
     * Returns whether this adapter enforces worker/process-incarnation ownership on UNKNOWN state
     * transitions. Legacy adapters remain binary compatible but are rejected by durable workers.
     */
    default boolean supportsFencedUnknownMutation() {
        return false;
    }

    default void markUnknown(String workerId, String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
        markUnknown(messageId, result, nextReconcileAt);
    }

    default void releaseUnknown(String workerId, String messageId, String detail, Instant nextReconcileAt) {
        releaseUnknown(messageId, detail, nextReconcileAt);
    }
}
