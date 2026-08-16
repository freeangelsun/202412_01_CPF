package com.cpf.file.attachment.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.file.attachment.internal.CpfAttachmentRuntimePolicy;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;

/** Download 권한·승인·만료·워터마크 정책을 Runtime snapshot에 적용합니다. */
public final class CpfDownloadPolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfAttachmentRuntimePolicy policy;

    public CpfDownloadPolicyRuntimeApplier(CpfAttachmentRuntimePolicy policy) {
        this.policy = policy;
    }

    @Override
    public String changeType() {
        return "DOWNLOAD_POLICY";
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
            policy.replaceDownload(
                    delivery.desiredVersion(),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "permissionRequired"), true),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "approvalRequired"), false),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "linkExpirySeconds"), 3600L),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "watermarkRequired"), false));
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "DOWNLOAD_POLICY_INVALID",
                    "Download policy payload 오류");
        }
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
    }

    private boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean bool) return bool;
        return value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }
}
