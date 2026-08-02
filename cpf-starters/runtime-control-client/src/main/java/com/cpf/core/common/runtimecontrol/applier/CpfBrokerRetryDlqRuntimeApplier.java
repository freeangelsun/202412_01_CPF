package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.broker.CpfBrokerConsumerRuntimePolicy;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Generic Worker의 bounded retry와 최종 DLQ 정책을 실제 처리 경로에 적용합니다. */
public final class CpfBrokerRetryDlqRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "BROKER_RETRY_DLQ";
    private final CpfBrokerConsumerRuntimePolicy policy;

    public CpfBrokerRetryDlqRuntimeApplier(CpfBrokerConsumerRuntimePolicy policy) {
        this.policy = policy;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            CpfBrokerConsumerRuntimePolicy.Snapshot current = policy.current();
            int maxAttempts = integer(
                    CpfRuntimePayloadJson.value(delivery.payload(), "maxAttempts"),
                    current.maxAttempts());
            long initial = number(
                    CpfRuntimePayloadJson.value(delivery.payload(), "initialBackoffMillis"),
                    current.initialBackoffMillis());
            long maximum = number(
                    CpfRuntimePayloadJson.value(delivery.payload(), "maxBackoffMillis"),
                    Math.max(initial, current.maxBackoffMillis()));
            Set<String> retryable = strings(
                    CpfRuntimePayloadJson.value(delivery.payload(), "retryableExceptionClasses"));
            CpfBrokerConsumerRuntimePolicy.Snapshot applied = policy.replaceRetry(maxAttempts, initial, maximum, retryable);
            if (applied.maxAttempts() != maxAttempts || applied.initialBackoffMillis() != initial) {
                return CpfRuntimeApplyResult.failure("BROKER_RETRY_NOT_CONFIRMED", "Broker retry/DLQ 정책 적용을 확인하지 못했습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("BROKER_RETRY_POLICY_INVALID", "Broker retry/DLQ 정책이 유효하지 않습니다.");
        }
    }

    private Set<String> strings(Object raw) {
        if (!(raw instanceof List<?> list)) return Set.of();
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (Object item : list) if (item != null && !String.valueOf(item).isBlank()) values.add(String.valueOf(item));
        return Set.copyOf(values);
    }

    private int integer(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }
}
