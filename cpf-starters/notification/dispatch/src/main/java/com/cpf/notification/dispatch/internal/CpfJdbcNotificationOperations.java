package com.cpf.notification.dispatch.internal;

import com.cpf.notification.api.CpfNotificationOperations;
import com.cpf.notification.api.CpfNotificationReceipt;
import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;
import com.cpf.notification.dispatch.JdbcCpfNotificationOutbox;
import java.time.Clock;

/** JDBC-backed notification public operation adapter; all retry/reprocess commands are durably audited by the outbox. */
public final class CpfJdbcNotificationOperations implements CpfNotificationOperations {
    private final JdbcCpfNotificationOutbox outbox;
    private final Clock clock;

    public CpfJdbcNotificationOperations(JdbcCpfNotificationOutbox outbox, Clock clock) {
        this.outbox = java.util.Objects.requireNonNull(outbox, "outbox");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfNotificationResult dispatch(CpfNotificationRequest request) {
        return outbox.enqueue(request);
    }

    @Override
    public CpfNotificationResult findResult(String notificationId) {
        return outbox.currentResult(required(notificationId, "notificationId"));
    }

    @Override
    public CpfNotificationResult approveReprocess(String notificationId, String operatorId, String reason) {
        outbox.approveReprocess(required(notificationId, "notificationId"), required(operatorId, "operatorId"),
                required(reason, "reason"), clock.instant());
        return outbox.currentResult(notificationId);
    }

    @Override
    public void recordReceipt(CpfNotificationReceipt receipt, String operatorId) {
        outbox.recordReceipt(java.util.Objects.requireNonNull(receipt, "receipt"), required(operatorId, "operatorId"));
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
