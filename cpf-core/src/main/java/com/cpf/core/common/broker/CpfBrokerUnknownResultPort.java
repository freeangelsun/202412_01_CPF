package com.cpf.core.common.broker;

import java.time.Instant;
import java.util.List;

/** Result-unknown broker publication recovery port. */
public interface CpfBrokerUnknownResultPort {
    void markUnknown(String messageId, CpfBrokerResult result, Instant nextReconcileAt);
    List<CpfBrokerEnvelope> claimUnknown(String workerId, int limit);
    void releaseUnknown(String messageId, String detail, Instant nextReconcileAt);

    default void markUnknown(String workerId, String messageId, CpfBrokerResult result, Instant nextReconcileAt) {
        markUnknown(messageId, result, nextReconcileAt);
    }

    default void releaseUnknown(String workerId, String messageId, String detail, Instant nextReconcileAt) {
        releaseUnknown(messageId, detail, nextReconcileAt);
    }
}
