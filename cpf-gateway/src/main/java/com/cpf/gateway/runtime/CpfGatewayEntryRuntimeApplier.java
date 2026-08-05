package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayEntryPolicyPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;

import java.time.Duration;
import java.util.Locale;
import java.util.Objects;

/** 승인된 Runtime Control 명령을 Gateway Entry 상태에 CAS 적용합니다. */
public final class CpfGatewayEntryRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfGatewayEntryPolicyPort policy;

    public CpfGatewayEntryRuntimeApplier(CpfGatewayEntryPolicyPort policy) {
        this.policy = Objects.requireNonNull(policy, "policy");
    }

    @Override
    public String changeType() {
        return "GATEWAY_ENTRY";
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
            String rawState = delivery.payload().text("state", "ACTIVE");
            CpfGatewayEntryPolicyPort.State state = CpfGatewayEntryPolicyPort.State.valueOf(
                    rawState.trim().toUpperCase(Locale.ROOT));
            long expectedVersion = delivery.payload().longValue(
                    "expectedVersion", Math.max(0L, delivery.desiredVersion() - 1L));
            long retrySeconds = delivery.payload().longValue("retryAfterSeconds", 60L);
            policy.replace(
                    expectedVersion,
                    delivery.desiredVersion(),
                    state,
                    Duration.ofSeconds(retrySeconds));
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException failure) {
            return CpfRuntimeApplyResult.failure(
                    "GATEWAY_ENTRY_INVALID",
                    "Gateway Entry runtime policy payload 오류");
        }
    }
}
