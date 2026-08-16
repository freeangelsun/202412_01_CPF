package com.cpf.messaging.reliability.api.jdbc;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cpf.messaging.reliability")
/** CpfMessagingReliabilityProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfMessagingReliabilityProperties {
    private boolean enabled = true;
    private boolean schemaRequired = true;
    private int claimLimit = 100;
    private Duration lease = Duration.ofSeconds(30);
    private int maxReplayBatch = 500;
    private Duration publisherDelay = Duration.ofSeconds(1);
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
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

    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    public Duration getUnknownReconcileDelay() {
        return unknownReconcileDelay;
    }

    public void setUnknownReconcileDelay(Duration unknownReconcileDelay) {
        this.unknownReconcileDelay = unknownReconcileDelay;
    }

    /** validate 작업을 CPF 표준 계약에 따라 수행한다. */
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
