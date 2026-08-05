package com.cpf.common.cache;

import java.net.InetAddress;
import java.util.Objects;

/** Durable cache invalidation consumer contract shared by cache providers and ADM. */
public class CpfRedisProperties {
    private String consumerId = defaultConsumerId();
    private String invalidationChannel = "cpf.cache.invalidate";
    private int reconcileBatchSize = 200;
    private int reconcileMaxBatches = 20;

    public String getConsumerId() { return consumerId; }
    public void setConsumerId(String consumerId) { this.consumerId = consumerId; }
    public String getInvalidationChannel() { return invalidationChannel; }
    public void setInvalidationChannel(String invalidationChannel) { this.invalidationChannel = invalidationChannel; }
    public int getReconcileBatchSize() { return reconcileBatchSize; }
    public void setReconcileBatchSize(int reconcileBatchSize) { this.reconcileBatchSize = reconcileBatchSize; }
    public int getReconcileMaxBatches() { return reconcileMaxBatches; }
    public void setReconcileMaxBatches(int reconcileMaxBatches) { this.reconcileMaxBatches = reconcileMaxBatches; }

    public void validate() {
        consumerId = requiredToken(consumerId, "consumerId", 180);
        invalidationChannel = requiredToken(invalidationChannel, "invalidationChannel", 180);
        if (reconcileBatchSize < 1 || reconcileBatchSize > 10_000) {
            throw new IllegalStateException("reconcile-batch-size must be between 1 and 10000");
        }
        if (reconcileMaxBatches < 1 || reconcileMaxBatches > 1_000) {
            throw new IllegalStateException("reconcile-max-batches must be between 1 and 1000");
        }
    }

    private static String requiredToken(String value, String field, int maxLength) {
        String normalized = Objects.requireNonNull(value, field + " is required").trim();
        if (normalized.isEmpty() || normalized.length() > maxLength
                || !normalized.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalStateException(field + " format is invalid");
        }
        return normalized;
    }

    private static String defaultConsumerId() {
        String host = "unknown-host";
        try { host = InetAddress.getLocalHost().getHostName(); } catch (Exception ignored) { }
        String instance = System.getenv().getOrDefault("CPF_INSTANCE_ID", "default");
        return ("cache-" + host + "-" + instance).replaceAll("[^A-Za-z0-9._:-]", "-");
    }
}
