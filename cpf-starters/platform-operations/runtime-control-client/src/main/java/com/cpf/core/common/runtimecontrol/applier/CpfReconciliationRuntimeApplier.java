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
    private static final Set<String> SUPPORTED_FIELDS = Set.of(
            "enabled", "queryIntervalMillis", "thresholdSeconds", "batchSize", "leaseSeconds",
            "manualResolutionRequired", "unknownTypes", "maxAttempts",
            "circuitFailureThreshold", "circuitOpenMillis");
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
            rejectUnknownFields(payload);
            policy.replace(
                    delivery.desiredVersion(),
                    bool(payload, "enabled", true),
                    number(payload.get("queryIntervalMillis"), 30_000L),
                    integer(payload.get("thresholdSeconds"), 60, "thresholdSeconds"),
                    integer(payload.get("batchSize"), 100, "batchSize"),
                    integer(payload.get("leaseSeconds"), 60, "leaseSeconds"),
                    bool(payload, "manualResolutionRequired", true),
                    strings(payload.get("unknownTypes")),
                    integer(payload.get("maxAttempts"), 8, "maxAttempts"),
                    integer(payload.get("circuitFailureThreshold"), 3, "circuitFailureThreshold"),
                    number(payload.get("circuitOpenMillis"), 30_000L));
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "RECONCILIATION_INVALID",
                    "Reconciliation allowlist/query/attempt/circuit/manual resolution 정책 오류");
        }
    }

    private void rejectUnknownFields(Map<String, Object> payload) {
        for (String key : payload.keySet()) {
            if (!SUPPORTED_FIELDS.contains(key)) {
                throw new IllegalArgumentException("지원하지 않는 Reconciliation 정책 필드: " + key);
            }
        }
    }

    private boolean bool(Map<String, Object> source, String key, boolean fallback) {
        Object value = source.get(key);
        if (value == null) return fallback;
        if (value instanceof Boolean bool) return bool;
        throw new IllegalArgumentException(key + "는 JSON boolean이어야 합니다.");
    }

    private long number(Object value, long fallback) {
        if (value == null) return fallback;
        if (value instanceof Byte || value instanceof Short
                || value instanceof Integer || value instanceof Long) {
            return ((Number) value).longValue();
        }
        if (value instanceof java.math.BigInteger integer) {
            return integer.longValueExact();
        }
        if (value instanceof java.math.BigDecimal decimal) {
            return decimal.longValueExact();
        }
        if (value instanceof Float || value instanceof Double) {
            double numeric = ((Number) value).doubleValue();
            if (!Double.isFinite(numeric) || numeric != Math.rint(numeric)) {
                throw new IllegalArgumentException("숫자 정책 값은 유한한 정수여야 합니다.");
            }
            return java.math.BigDecimal.valueOf(numeric).longValueExact();
        }
        throw new IllegalArgumentException("숫자 정책 값은 JSON integer여야 합니다.");
    }

    private int integer(Object value, int fallback, String name) {
        long parsed=number(value,fallback);
        if(parsed<Integer.MIN_VALUE||parsed>Integer.MAX_VALUE)
            throw new IllegalArgumentException(name + "가 int 범위를 초과했습니다.");
        return Math.toIntExact(parsed);
    }

    private Set<String> strings(Object value) {
        if (value == null) return Set.of();
        if (!(value instanceof List<?> list))
            throw new IllegalArgumentException("unknownTypes는 문자열 배열이어야 합니다.");
        if(list.size()>1000) throw new IllegalArgumentException("unknownTypes는 최대 1000개입니다.");
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Object entry : list) {
            if (!(entry instanceof String text))
                throw new IllegalArgumentException("unknownTypes 항목은 문자열이어야 합니다.");
            String normalized=text.trim();
            if(normalized.length()>100) throw new IllegalArgumentException("unknownTypes 항목은 최대 100자입니다.");
            if(!normalized.isEmpty()) result.add(normalized);
        }
        return Set.copyOf(result);
    }
}
