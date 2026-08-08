package com.cpf.starter.messaging.reliability.jdbc;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.reliability")
public class CpfMessagingReliabilityProperties {
    private boolean enabled = true;
    private boolean schemaRequired = true;
    private int claimLimit = 100;
    private Duration lease = Duration.ofSeconds(30);
    private int maxReplayBatch = 500;
    private Duration publisherDelay = Duration.ofSeconds(1);
    private Duration unknownReconcileDelay = Duration.ofSeconds(30);

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isSchemaRequired() {
        return schemaRequired;
    }

    public void setSchemaRequired(boolean schemaRequired) {
        this.schemaRequired = schemaRequired;
    }

    public int getClaimLimit() {
        return claimLimit;
    }

    public void setClaimLimit(int claimLimit) {
        this.claimLimit = claimLimit;
    }

    public Duration getLease() {
        return lease;
    }

    public void setLease(Duration lease) {
        this.lease = lease;
    }

    public int getMaxReplayBatch() {
        return maxReplayBatch;
    }

    public void setMaxReplayBatch(int maxReplayBatch) {
        this.maxReplayBatch = maxReplayBatch;
    }

    public Duration getPublisherDelay() {
        return publisherDelay;
    }

    public void setPublisherDelay(Duration publisherDelay) {
        this.publisherDelay = publisherDelay;
    }

    public Duration getUnknownReconcileDelay() {
        return unknownReconcileDelay;
    }

    public void setUnknownReconcileDelay(Duration unknownReconcileDelay) {
        this.unknownReconcileDelay = unknownReconcileDelay;
    }

    public void validate() {
        if (claimLimit < 1 || claimLimit > 1000) {
            throw new IllegalStateException("claim-limit must be 1..1000");
        }
        positive(lease, "lease");
        positive(publisherDelay, "publisher-delay");
        positive(unknownReconcileDelay, "unknown-reconcile-delay");
        if (maxReplayBatch < 1 || maxReplayBatch > 5000) {
            throw new IllegalStateException("max-replay-batch must be 1..5000");
        }
    }

    private static void positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalStateException(name + " must be positive");
        }
    }
}
