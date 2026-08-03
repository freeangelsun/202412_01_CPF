package com.cpf.starter.data.persistence.jdbc.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.database.CpfConnectionPoolRuntimeController;
import com.cpf.starter.runtimecontrol.spi.CpfRuntimePayloadReader;

/** 실제 CPF Primary/Replica Hikari Pool의 동적 설정과 soft-evict를 적용합니다. */
public final class CpfConnectionPoolRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "CONNECTION_POOL";
    private final CpfConnectionPoolRuntimeController controller;

    public CpfConnectionPoolRuntimeApplier(CpfConnectionPoolRuntimeController controller) { this.controller = controller; }
    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            CpfConnectionPoolRuntimeController.Policy policy = new CpfConnectionPoolRuntimeController.Policy(
                    integer(CpfRuntimePayloadReader.value(delivery.payload(), "maximumPoolSize"), 20),
                    integer(CpfRuntimePayloadReader.value(delivery.payload(), "minimumIdle"), 5),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "connectionTimeoutMillis"), 30000L),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "validationTimeoutMillis"), 5000L),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "idleTimeoutMillis"), 600000L),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "maxLifetimeMillis"), 1800000L),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "softEvict"), false));
            CpfConnectionPoolRuntimeController.Result result = controller.apply(policy);
            if (result.controlledPoolCount() < 1) {
                return CpfRuntimeApplyResult.failure("CONNECTION_POOL_NOT_CONFIRMED", "조정된 Connection Pool이 없습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("CONNECTION_POOL_APPLY_FAILED", "Connection Pool 정책 적용에 실패했습니다.");
        }
    }

    private int integer(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
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
