package com.cpf.messaging.reliability.api.jdbc.runtimecontrol;

import com.cpf.messaging.api.CpfBrokerConsumerControl;
import com.cpf.messaging.api.CpfBrokerConsumerControlPort;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.fasterxml.jackson.databind.JsonNode;

/** Generic Worker와 실제 transport container의 consumer 제어를 함께 적용합니다. */
final class CpfBrokerConsumerRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "BROKER_CONSUMER";
    private final CpfBrokerConsumerRuntimePolicy policy;
    private final CpfBrokerConsumerControlPort transportControl;

    CpfBrokerConsumerRuntimeApplier(
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
            boolean paused = bool(CpfRuntimePayloadReader.field(delivery.payload(),"paused"), current.paused());
            int concurrency = integer(CpfRuntimePayloadReader.field(delivery.payload(),"concurrency"), current.concurrency());
            int prefetch = integer(CpfRuntimePayloadReader.field(delivery.payload(),"prefetch"), current.prefetch());
            validate(concurrency, prefetch);
            boolean transportChangeRequested = CpfRuntimePayloadReader.contains(delivery.payload(),"concurrency")
                    || CpfRuntimePayloadReader.contains(delivery.payload(),"prefetch");
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
                    // 적용 실패 시 이전 상태로 되돌려 Runtime 정책이 부분 적용된 채 남지 않도록 보장합니다.
                    return rollback(current, transportAttempted,
                            "BROKER_CONSUMER_NOT_CONFIRMED", "Broker consumer 정책 적용을 확인하지 못했습니다.");
                }
                return CpfRuntimeApplyResult.success(delivery.payloadHash());
            // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
            } catch (RuntimeException applyFailure) {
                // 적용 실패 시 이전 상태로 되돌려 Runtime 정책이 부분 적용된 채 남지 않도록 보장합니다.
                return rollback(current, transportAttempted,
                        "BROKER_CONSUMER_APPLY_FAILED", "Broker consumer 정책 적용 중 오류가 발생했습니다.");
            }
        // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
        } catch (IllegalArgumentException ex) {
            return CpfRuntimeApplyResult.failure("BROKER_CONSUMER_POLICY_INVALID", "Broker consumer 정책이 유효하지 않습니다.");
        }
    }

    // 적용 실패 시 이전 상태로 되돌려 Runtime 정책이 부분 적용된 채 남지 않도록 보장합니다.
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
        // 적용 실패 시 이전 상태로 되돌려 Runtime 정책이 부분 적용된 채 남지 않도록 보장합니다.
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
