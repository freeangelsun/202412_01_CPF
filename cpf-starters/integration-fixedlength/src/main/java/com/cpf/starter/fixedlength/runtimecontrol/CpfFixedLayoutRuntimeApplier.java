package com.cpf.starter.fixedlength.runtimecontrol;

import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.starter.runtimecontrol.spi.CpfRuntimePayloadReader;

import java.util.Map;

/** 단일 Fixed Length Layout을 실제 Registry에 적용합니다. */
public final class CpfFixedLayoutRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfFixedLengthLayoutRegistry registry;
    private final CpfFixedLengthLayoutPayloadDecoder decoder = new CpfFixedLengthLayoutPayloadDecoder();

    public CpfFixedLayoutRuntimeApplier(CpfFixedLengthLayoutRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String changeType() {
        return "FIXED_LAYOUT";
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
    @SuppressWarnings("unchecked")
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Object raw = CpfRuntimePayloadReader.value(delivery.payload(), "layout");
            if (!(raw instanceof Map<?, ?> map)) {
                throw new IllegalArgumentException("layout object 필수");
            }
            registry.upsert(
                    delivery.desiredVersion(),
                    decoder.decode((Map<String, Object>) map),
                    String.valueOf(CpfRuntimePayloadReader.valueOrDefault(
                            delivery.payload(),
                            "expectedRegistryHash",
                            "")));
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "FIXED_LAYOUT_INVALID",
                    "Fixed layout payload/compatibility 오류");
        }
    }
}
