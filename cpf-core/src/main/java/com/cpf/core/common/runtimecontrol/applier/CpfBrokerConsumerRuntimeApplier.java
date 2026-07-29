package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.broker.CpfBrokerConsumerControl;
import com.cpf.core.api.broker.CpfBrokerConsumerControlPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;
import com.cpf.core.common.broker.CpfBrokerConsumerRuntimePolicy;
import com.fasterxml.jackson.databind.JsonNode;

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

    @Override

    public String changeType() { return CHANGE_TYPE; }
    @Override
    public boolean supportsIdempotentReplay() { return true; }
    @Override
    public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            CpfBrokerConsumerRuntimePolicy.Snapshot current = policy.current();
            boolean paused = bool(CpfRuntimePayloadJson.field(delivery.payload(),"paused"), current.paused());
            int concurrency = integer(CpfRuntimePayloadJson.field(delivery.payload(),"concurrency"), current.concurrency());
            int prefetch = integer(CpfRuntimePayloadJson.field(delivery.payload(),"prefetch"), current.prefetch());
            validate(concurrency, prefetch);
            boolean transportChangeRequested = CpfRuntimePayloadJson.contains(delivery.payload(),"concurrency")
                    || CpfRuntimePayloadJson.contains(delivery.payload(),"prefetch");
            CpfBrokerConsumerControl control = new CpfBrokerConsumerControl(paused, concurrency, prefetch);
            if (transportChangeRequested && transportControl == null) {
                return CpfRuntimeApplyResult.failure(
                        "BROKER_TRANSPORT_CONTROL_UNAVAILABLE",
                        "현재 Broker adapter는 concurrency/prefetch runtime control을 지원하지 않습니다.");
            }

            boolean transportAttempted = false;
            try {
                if (transportControl != null) {
                    transportAttempted = true;
                    transportControl.apply(control);
                }
                CpfBrokerConsumerRuntimePolicy.Snapshot applied = policy.replaceConsumer(paused, concurrency, prefetch);
                if (applied.paused() != paused || applied.concurrency() != concurrency || applied.prefetch() != prefetch) {
                    return rollback(current, transportAttempted,
                            "BROKER_CONSUMER_NOT_CONFIRMED", "Broker consumer 정책 적용을 확인하지 못했습니다.");
                }
                return CpfRuntimeApplyResult.success(delivery.payloadHash());
            } catch (RuntimeException applyFailure) {
                return rollback(current, transportAttempted,
                        "BROKER_CONSUMER_APPLY_FAILED", "Broker consumer 정책 적용 중 오류가 발생했습니다.");
            }
        } catch (IllegalArgumentException ex) {
            return CpfRuntimeApplyResult.failure("BROKER_CONSUMER_POLICY_INVALID", "Broker consumer 정책이 유효하지 않습니다.");
        }
    }

    private CpfRuntimeApplyResult rollback(
            CpfBrokerConsumerRuntimePolicy.Snapshot previous,
            boolean transportAttempted,
            String code,
            String message) {
        try {
            CpfBrokerConsumerControl previousControl = new CpfBrokerConsumerControl(
                    previous.paused(), previous.concurrency(), previous.prefetch());
            if (transportAttempted && transportControl != null) transportControl.apply(previousControl);
            policy.replaceConsumer(previous.paused(), previous.concurrency(), previous.prefetch());
            return CpfRuntimeApplyResult.failure(code, message + " 이전 snapshot으로 복원했습니다.");
        } catch (RuntimeException rollbackFailure) {
            return CpfRuntimeApplyResult.unknown(code + "_ROLLBACK_UNKNOWN",
                    message + " 이전 snapshot 복원 결과를 확인할 수 없습니다.");
        }
    }

    private void validate(int concurrency, int prefetch) {
        if (concurrency < 1 || concurrency > 1024) throw new IllegalArgumentException("concurrency 범위 오류");
        if (prefetch < 1 || prefetch > 100000) throw new IllegalArgumentException("prefetch 범위 오류");
    }

    private int integer(JsonNode value, int fallback) {
        if (value == null || value.isMissingNode() || value.isNull()) return fallback;
        return value.isNumber() ? value.intValue() : Integer.parseInt(value.asText());
    }

    private boolean bool(JsonNode value, boolean fallback) {
        if (value == null || value.isMissingNode() || value.isNull()) return fallback;
        return value.isBoolean() ? value.booleanValue() : Boolean.parseBoolean(value.asText());
    }
}
