package com.cpf.platform.operations.channelregistry.jdbc.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
import com.cpf.platform.operations.channelregistry.model.CpfChannelPolicySnapshot;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;

/** DB 정본 채널 정책을 Runtime의 lock-free snapshot으로 실제 교체합니다. */
public final class CpfChannelPolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "CHANNEL_POLICY";
    private final CpfChannelPolicyService policyService;

    public CpfChannelPolicyRuntimeApplier(CpfChannelPolicyService policyService) {
        this.policyService = policyService;
    }

    @Override public String changeType() { return CHANGE_TYPE; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        Object expected = CpfRuntimePayloadReader.value(delivery.payload(), "minimumSnapshotVersion");
        long minimumVersion = expected instanceof Number number ? number.longValue() : 0L;
        CpfChannelPolicySnapshot snapshot = policyService.refresh();
        if (snapshot.version() < minimumVersion) {
            return CpfRuntimeApplyResult.failure(
                    "CHANNEL_POLICY_VERSION_BEHIND",
                    "채널 정책 스냅샷 버전이 Runtime Change 기대 버전보다 낮습니다.");
        }
        return CpfRuntimeApplyResult.success(delivery.payloadHash());
    }
}
