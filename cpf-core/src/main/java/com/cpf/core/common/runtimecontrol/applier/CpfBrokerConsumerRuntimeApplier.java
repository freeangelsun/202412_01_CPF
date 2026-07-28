package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.broker.CpfBrokerConsumerControl;
import com.cpf.core.api.broker.CpfBrokerConsumerControlPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.broker.CpfBrokerConsumerRuntimePolicy;

/** Generic Worker와 실제 transport container의 consumer 제어를 함께 적용합니다. */
public final class CpfBrokerConsumerRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "BROKER_CONSUMER";
    private final CpfBrokerConsumerRuntimePolicy policy;
    private final CpfBrokerConsumerControlPort transportControl;

    public CpfBrokerConsumerRuntimeApplier(
            CpfBrokerConsumerRuntimePolicy policy,
            CpfBrokerConsumerControlPort transportControl) {
        this.policy = policy;
        this.transportControl = transportControl;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            CpfBrokerConsumerRuntimePolicy.Snapshot current = policy.current();
            boolean paused = bool(delivery.payload().get("paused"), current.paused());
            int concurrency = integer(delivery.payload().get("concurrency"), current.concurrency());
            int prefetch = integer(delivery.payload().get("prefetch"), current.prefetch());
            boolean transportChangeRequested = delivery.payload().containsKey("concurrency")
                    || delivery.payload().containsKey("prefetch");
            CpfBrokerConsumerControl control = new CpfBrokerConsumerControl(paused, concurrency, prefetch);
            if (transportChangeRequested && transportControl == null) {
                return CpfRuntimeApplyResult.failure(
                        "BROKER_TRANSPORT_CONTROL_UNAVAILABLE",
                        "현재 Broker adapter는 concurrency/prefetch runtime control을 지원하지 않습니다.");
            }
            if (transportControl != null) transportControl.apply(control);
            CpfBrokerConsumerRuntimePolicy.Snapshot applied = policy.replaceConsumer(paused, concurrency, prefetch);
            if (applied.paused() != paused || applied.concurrency() != concurrency || applied.prefetch() != prefetch) {
                return CpfRuntimeApplyResult.failure("BROKER_CONSUMER_NOT_CONFIRMED", "Broker consumer 정책 적용을 확인하지 못했습니다.");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("BROKER_CONSUMER_POLICY_INVALID", "Broker consumer 정책이 유효하지 않습니다.");
        }
    }

    private int integer(Object value, int fallback) {
        if (value instanceof Number number) return number.intValue();
        return value == null ? fallback : Integer.parseInt(String.valueOf(value));
    }

    private boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean bool) return bool;
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }
}
