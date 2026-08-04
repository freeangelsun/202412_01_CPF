package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.reconciliation.CpfReconciliationRuntimePolicy;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reconciliation 조회 주기·임계치·lease·attempt·circuit 정책을 Runtime snapshot에 적용합니다. */
public final class CpfReconciliationRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfReconciliationRuntimePolicy policy;

    public CpfReconciliationRuntimeApplier(CpfReconciliationRuntimePolicy policy) {
        this.policy = policy;
    }

    @Override
    public String changeType() {
        return "RECONCILIATION";
    }

    @Override
    public boolean supportsIdempotentReplay() {
        return true;
    }

    @Override
    public boolean snapshotCapable() {
        return true;
    }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Map<String, Object> payload = CpfRuntimePayloadJson.asMap(delivery.payload());
            policy.replace(
                    delivery.desiredVersion(),
                    bool(payload, "enabled", true),
                    number(payload.get("queryIntervalMillis"), 30_000L),
                    (int) number(payload.get("thresholdSeconds"), 60L),
                    (int) number(payload.get("batchSize"), 100L),
                    (int) number(payload.get("leaseSeconds"), 60L),
                    bool(payload, "manualResolutionRequired", true),
                    strings(payload.get("unknownTypes")),
                    (int) number(payload.get("maxAttempts"), 8L),
                    (int) number(payload.get("circuitFailureThreshold"), 3L),
                    number(payload.get("circuitOpenMillis"), 30_000L));
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "RECONCILIATION_INVALID",
                    "Reconciliation allowlist/query/attempt/circuit/manual resolution 정책 오류");
        }
    }

    private boolean bool(Map<String, Object> source, String key, boolean fallback) {
        Object value = source.get(key);
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }

    private Set<String> strings(Object value) {
        if (!(value instanceof List<?> list)) {
            return Set.of();
        }
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Object entry : list) {
            if (entry != null) {
                result.add(String.valueOf(entry));
            }
        }
        return Set.copyOf(result);
    }
}
