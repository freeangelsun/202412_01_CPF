package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.filetransfer.CpfFileTransferRuntimeState;
import com.cpf.core.common.runtimecontrol.CpfRuntimePayloadJson;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 실제 FileTransferEngine의 검증 정책을 hot-apply합니다. */
public final class CpfFilePolicyRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfFileTransferRuntimeState state;

    public CpfFilePolicyRuntimeApplier(CpfFileTransferRuntimeState state) {
        this.state = state;
    }

    @Override
    public String changeType() {
        return "FILE_POLICY";
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
            var policy = new CpfFileTransferRuntimeState.FilePolicy(
                    number(CpfRuntimePayloadJson.value(delivery.payload(), "maxFileSize"), 1_073_741_824L),
                    strings(CpfRuntimePayloadJson.value(delivery.payload(), "allowedExtensions")),
                    strings(CpfRuntimePayloadJson.value(delivery.payload(), "allowedMimeTypes")),
                    bool(CpfRuntimePayloadJson.value(delivery.payload(), "checksumRequired"), true),
                    bool(CpfRuntimePayloadJson.value(delivery.payload(), "scanRequired"), false),
                    bool(CpfRuntimePayloadJson.value(delivery.payload(), "quarantineOnFailure"), true));
            state.replacePolicy(policy);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "FILE_POLICY_INVALID",
                    "File policy payload가 유효하지 않습니다.");
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
