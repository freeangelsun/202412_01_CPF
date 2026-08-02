package com.cpf.batch.worker.notification;

import com.cpf.starter.notification.CpfNotificationProperties;
import com.cpf.starter.notification.CpfNotificationWorker;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration(proxyBeanMethods = false)
@EnableScheduling
@ConditionalOnBean(CpfNotificationWorker.class)
@ConditionalOnProperty(prefix = "cpf.notification.dispatch", name = "enabled", havingValue = "true")
public class CpfNotificationDispatchScheduler {
    private final CpfNotificationWorker worker;
    private final CpfNotificationProperties properties;
    private final String workerId;

    public CpfNotificationDispatchScheduler(
            CpfNotificationWorker worker, CpfNotificationProperties properties) {
        this.worker = worker;
        this.properties = properties;
        workerId = properties.workerId() + "-" + UUID.randomUUID();
    }

    @Scheduled(fixedDelayString = "${cpf.notification.dispatch.fixed-delay:5000}")
    public void dispatch() {
        worker.runOnce(workerId, properties.batchSize());
    }

    @Scheduled(fixedDelayString = "${cpf.notification.dispatch.reconcile-fixed-delay:30000}")
    public void reconcileUnknown() {
        worker.reconcileUnknown(workerId + "-reconcile", properties.batchSize());
    }
}
