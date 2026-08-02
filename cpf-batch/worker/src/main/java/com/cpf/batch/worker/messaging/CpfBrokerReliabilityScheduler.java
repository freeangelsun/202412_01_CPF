package com.cpf.batch.worker.messaging;

import com.cpf.core.common.broker.CpfBrokerPublisherWorker;
import com.cpf.starter.messaging.reliability.CpfBrokerUnknownResultReconciler;
import com.cpf.starter.messaging.reliability.CpfMessagingReliabilityProperties;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public final class CpfBrokerReliabilityScheduler {
    private final ObjectProvider<CpfBrokerPublisherWorker> publisher;
    private final CpfBrokerUnknownResultReconciler reconciler;
    private final CpfMessagingReliabilityProperties properties;
    private final String workerId = "CPF-BROKER-" + UUID.randomUUID();

    public CpfBrokerReliabilityScheduler(
            ObjectProvider<CpfBrokerPublisherWorker> publisher,
            CpfBrokerUnknownResultReconciler reconciler,
            CpfMessagingReliabilityProperties properties) {
        this.publisher = publisher;
        this.reconciler = reconciler;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${cpf.messaging.reliability.publisher-delay:PT1S}")
    public void publish() {
        if (!properties.isEnabled()) {
            return;
        }
        CpfBrokerPublisherWorker worker = publisher.getIfAvailable();
        if (worker != null) {
            worker.runOnce(workerId, properties.getClaimLimit());
        }
    }

    @Scheduled(fixedDelayString = "${cpf.messaging.reliability.unknown-reconcile-delay:PT30S}")
    public void reconcile() {
        if (properties.isEnabled()) {
            reconciler.runOnce(workerId + "-RECON", properties.getClaimLimit());
        }
    }
}
