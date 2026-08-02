package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.logging.CpfTraceSamplingPolicy;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

import java.util.LinkedHashMap;
import java.util.Map;

/** 실제 LoggingAspect가 소비하는 Trace sampling snapshot을 원자 교체합니다. */
public final class CpfTraceSamplingRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "TRACE_SAMPLING";
    private final CpfTraceSamplingPolicy policy;

    public CpfTraceSamplingRuntimeApplier(CpfTraceSamplingPolicy policy) {
        this.policy = policy;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            long version = number(
                    CpfRuntimePayloadJson.value(delivery.payload(), "version"),
                    delivery.desiredVersion());
            double defaultRate = decimal(
                    CpfRuntimePayloadJson.value(delivery.payload(), "defaultRate"),
                    1.0d);
            boolean alwaysSampleErrors = bool(
                    CpfRuntimePayloadJson.value(delivery.payload(), "alwaysSampleErrors"),
                    true);
            Map<String, Double> moduleRates = rates(
                    CpfRuntimePayloadJson.value(delivery.payload(), "moduleRates"));
            Map<String, Double> transactionRates = rates(
                    CpfRuntimePayloadJson.value(delivery.payload(), "businessTransactionRates"));
            CpfTraceSamplingPolicy.Snapshot applied = policy.replace(
                    version, defaultRate, moduleRates, transactionRates, alwaysSampleErrors);
            if (applied.version() != version) {
                return CpfRuntimeApplyResult.failure("TRACE_SAMPLING_NOT_CONFIRMED", "Trace sampling version 교체를 확인하지 못했습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("TRACE_SAMPLING_POLICY_INVALID", "Trace sampling payload가 유효하지 않습니다.");
        }
    }

    private Map<String, Double> rates(Object raw) {
        if (!(raw instanceof Map<?, ?> source)) return Map.of();
        LinkedHashMap<String, Double> result = new LinkedHashMap<>();
        source.forEach((key, value) -> {
            if (key != null && value != null) result.put(String.valueOf(key), decimal(value, 1.0d));
        });
        return Map.copyOf(result);
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }

    private double decimal(Object value, double fallback) {
        if (value instanceof Number number) return number.doubleValue();
        return value == null ? fallback : Double.parseDouble(String.valueOf(value));
    }

    private boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean bool) return bool;
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }
}
