package com.cpf.platform.operations.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;

/** ADM에서 변경된 Canonical Operation Policy를 각 Runtime LKG에 hot-apply합니다. */
public final class CpfOperationPolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    public static final String CHANGE_TYPE = "OPERATION_POLICY";

    private final CpfJdbcOperationAccessPolicy policy;

    public CpfOperationPolicyRuntimeApplier(CpfJdbcOperationAccessPolicy policy) {
        this.policy = java.util.Objects.requireNonNull(policy, "policy");
    }

    @Override
    public String changeType() {
        return CHANGE_TYPE;
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
        Object expected = CpfRuntimePayloadReader.value(delivery.payload(), "minimumPolicyVersion");
        long minimumVersion = expected instanceof Number number ? number.longValue() : 0L;
        CpfJdbcOperationAccessPolicy.RuntimeStatus status = policy.refresh();
        if (status.policyVersion() < minimumVersion) {
            return CpfRuntimeApplyResult.failure(
                    "OPERATION_POLICY_VERSION_BEHIND",
                    "Operation Policy snapshot version is behind the requested ADM policy version.");
        }
        if (status.status() == CpfJdbcOperationAccessPolicy.Status.EXPIRED) {
            return CpfRuntimeApplyResult.failure(
                    "OPERATION_POLICY_EXPIRED",
                    "Operation Policy refresh did not produce a valid LKG snapshot.");
        }
        return CpfRuntimeApplyResult.success(delivery.payloadHash());
    }
}
