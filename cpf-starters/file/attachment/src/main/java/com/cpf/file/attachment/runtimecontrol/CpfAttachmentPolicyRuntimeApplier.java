package com.cpf.file.attachment.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.file.attachment.internal.CpfAttachmentRuntimePolicy;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Attachment 저장·검증 정책을 실제 Runtime snapshot에 적용합니다. */
public final class CpfAttachmentPolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfAttachmentRuntimePolicy policy;

    public CpfAttachmentPolicyRuntimeApplier(CpfAttachmentRuntimePolicy policy) {
        this.policy = policy;
    }

    @Override
    public String changeType() {
        return "ATTACHMENT_POLICY";
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
            policy.replaceAttachment(
                    delivery.desiredVersion(),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "maxBytes"), 10_485_760L),
                    strings(CpfRuntimePayloadReader.value(delivery.payload(), "allowedExtensions")),
                    strings(CpfRuntimePayloadReader.value(delivery.payload(), "allowedMimeTypes")),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "scanRequired"), false),
                    number(CpfRuntimePayloadReader.value(delivery.payload(), "retentionDays"), 3650L),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "quarantineOnFailure"), true),
                    bool(CpfRuntimePayloadReader.value(delivery.payload(), "watermarkOnStore"), false));
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "ATTACHMENT_POLICY_INVALID",
                    "Attachment policy payload 오류");
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

    private Set<String> strings(Object value) {
        if (!(value instanceof List<?> list)) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (Object entry : list) {
            if (entry != null) result.add(String.valueOf(entry));
        }
        return Set.copyOf(result);
    }
}
