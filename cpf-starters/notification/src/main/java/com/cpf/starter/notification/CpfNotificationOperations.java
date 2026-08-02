package com.cpf.starter.notification;

import java.time.Clock;

/** 위험한 재처리를 운영자·사유와 함께 감사하는 Notification 운영 API입니다. */
public final class CpfNotificationOperations {
    private final JdbcCpfNotificationOutbox outbox;
    private final Clock clock;

    public CpfNotificationOperations(JdbcCpfNotificationOutbox outbox, Clock clock) {
        this.outbox = outbox;
        this.clock = clock;
    }

    public void approveReprocess(String notificationId, String operatorId, String reason) {
        outbox.approveReprocess(notificationId, operatorId, reason, clock.instant());
    }

    public void recordReceipt(CpfNotificationReceipt receipt, String operatorId) {
        outbox.recordReceipt(receipt, operatorId);
    }
}
