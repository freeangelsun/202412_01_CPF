package com.cpf.security.secret.runtimecontrol;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Calls the actual provider reload SPI for security material changes. */
public final class CpfSecurityMaterialRuntimeApplier implements CpfRuntimeChangeApplier {
    @FunctionalInterface
    public interface ReloadAction {
        String reload(Set<String> references, long desiredVersion);
    }

    private final String changeType;
    private final ReloadAction action;

    public CpfSecurityMaterialRuntimeApplier(String changeType, ReloadAction action) {
        this.changeType = changeType;
        this.action = action;
    }

    @Override
    public String changeType() {
        return changeType;
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
            Set<String> references = references(
                    CpfRuntimePayloadReader.value(
                            delivery.payload(), "credentialReferences"));
            if (references.isEmpty()) {
                return CpfRuntimeApplyResult.failure(
                        "SECURITY_REFERENCE_REQUIRED",
                        "credentialReferences가 필요합니다.");
            }
            String actualHash = action.reload(references, delivery.desiredVersion());
            if (actualHash == null || actualHash.isBlank()) {
                return CpfRuntimeApplyResult.failure(
                        "SECURITY_RELOAD_NOT_CONFIRMED",
                        "Provider가 적용 hash를 반환하지 않았습니다.");
            }
            return CpfRuntimeApplyResult.success(actualHash);
        } catch (RuntimeException exception) {
            return CpfRuntimeApplyResult.failure(
                    "SECURITY_RELOAD_FAILED",
                    "Security material reload에 실패했습니다.");
        }
    }

    private static Set<String> references(Object raw) {
        if (!(raw instanceof List<?> values)) {
            return Set.of();
        }
        Set<String> references = new LinkedHashSet<>();
        for (Object value : values) {
            if (value == null) {
                continue;
            }
            String text = String.valueOf(value).trim();
            if (!text.isBlank()) {
                references.add(text);
            }
        }
        return Set.copyOf(references);
    }
}
