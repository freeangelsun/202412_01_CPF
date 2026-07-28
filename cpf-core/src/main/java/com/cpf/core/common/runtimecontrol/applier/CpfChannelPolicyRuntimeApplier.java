package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.channel.application.CpfChannelPolicyService;
import com.cpf.core.channel.model.CpfChannelPolicySnapshot;

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
        Object expected = delivery.payload().get("minimumSnapshotVersion");
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
