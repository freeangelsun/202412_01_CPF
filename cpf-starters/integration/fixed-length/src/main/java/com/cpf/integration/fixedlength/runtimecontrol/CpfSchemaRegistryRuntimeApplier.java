package com.cpf.integration.fixedlength.runtimecontrol;

import com.cpf.integration.fixedlength.api.CpfFixedLengthLayout;
import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Fixed Length Schema Registry 전체 snapshot을 원자 교체합니다. */
public final class CpfSchemaRegistryRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfFixedLengthLayoutRegistry registry;
    private final CpfFixedLengthLayoutPayloadDecoder decoder = new CpfFixedLengthLayoutPayloadDecoder();

    public CpfSchemaRegistryRuntimeApplier(CpfFixedLengthLayoutRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String changeType() {
        return "SCHEMA_REGISTRY";
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
            Object raw = CpfRuntimePayloadReader.value(delivery.payload(), "layouts");
            if (!(raw instanceof List<?> entries)) {
                throw new IllegalArgumentException("layouts array 필수");
            }
            List<CpfFixedLengthLayout> layouts = new ArrayList<>();
            for (Object entry : entries) {
                if (!(entry instanceof Map<?, ?> map)) {
                    throw new IllegalArgumentException("layout object 필요");
                }
                layouts.add(decoder.decode((Map<String, Object>) map));
            }
            var compatibility = CpfFixedLengthLayoutRegistry.Compatibility.valueOf(
                    String.valueOf(CpfRuntimePayloadReader.valueOrDefault(
                            delivery.payload(),
                            "compatibility",
                            "BACKWARD")).toUpperCase(Locale.ROOT));
            registry.replaceSnapshot(
                    delivery.desiredVersion(),
                    layouts,
                    String.valueOf(CpfRuntimePayloadReader.valueOrDefault(
                            delivery.payload(),
                            "expectedRegistryHash",
                            "")),
                    compatibility);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "SCHEMA_REGISTRY_INVALID",
                    "Schema registry payload/hash/compatibility 오류");
        }
    }
}
