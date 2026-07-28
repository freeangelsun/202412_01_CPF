package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.database.CpfReadRoutingRuntimePolicy;

/** 실제 cpfDataSource의 Primary/Replica 선택 정책을 hot-apply합니다. */
public final class CpfDbReadRoutingRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "DB_READ_ROUTING";
    private final CpfReadRoutingRuntimePolicy policy;

    public CpfDbReadRoutingRuntimeApplier(CpfReadRoutingRuntimePolicy policy) { this.policy = policy; }
    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            CpfReadRoutingRuntimePolicy.Snapshot current = policy.current();
            boolean enabled = bool(delivery.payload().get("enabled"), current.enabled());
            long maxLag = number(delivery.payload().get("maxReplicaLagMillis"), current.maxReplicaLagMillis());
            long readAfterWrite = number(delivery.payload().get("readAfterWriteMillis"), current.readAfterWriteMillis());
            CpfReadRoutingRuntimePolicy.Snapshot applied = policy.replace(enabled, maxLag, readAfterWrite);
            if (applied.enabled() != enabled || applied.maxReplicaLagMillis() != maxLag) {
                return CpfRuntimeApplyResult.failure("DB_READ_ROUTING_NOT_CONFIRMED", "DB read routing 정책 적용을 확인하지 못했습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("DB_READ_ROUTING_POLICY_INVALID", "DB read routing 정책이 유효하지 않습니다.");
        }
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }
    private boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean bool) return bool;
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }
}
