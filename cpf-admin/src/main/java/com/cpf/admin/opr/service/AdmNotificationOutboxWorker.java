package com.cpf.admin.opr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;

/** Durable Notification Outbox를 주기적으로 전달하는 다중 인스턴스 안전 Worker입니다. */
@Component
public class AdmNotificationOutboxWorker {
    private final AdmNotificationOutboxService outboxService;
    private final String workerId;
    private final int batchSize;

    public AdmNotificationOutboxWorker(
            AdmNotificationOutboxService outboxService,
            @Value("${cpf.notification.outbox.worker-id:}") String configuredWorkerId,
            @Value("${cpf.notification.outbox.batch-size:20}") int batchSize) {
        this.outboxService = outboxService;
        this.workerId = configuredWorkerId == null || configuredWorkerId.isBlank()
                ? ManagementFactory.getRuntimeMXBean().getName()
                : configuredWorkerId.trim();
        this.batchSize = Math.max(1, Math.min(batchSize, 100));
    }

    @Scheduled(fixedDelayString = "${cpf.notification.outbox.poll-delay-ms:5000}")
    public void deliver() {
        outboxService.processDue(workerId, batchSize);
    }
}
