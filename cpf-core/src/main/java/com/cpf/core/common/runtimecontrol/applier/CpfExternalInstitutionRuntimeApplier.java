package com.cpf.core.common.runtimecontrol.applier;

import com.cpf.core.api.fixedlength.CpfFixedLengthLayoutRegistry;
import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.http.CpfServiceEndpointRegistry;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 기관/외부 시스템 endpoint 정본을 layout version과 timeout까지 검증한 뒤 원자 적용합니다. */
public final class CpfExternalInstitutionRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfServiceEndpointRegistry registry;
    private final CpfFixedLengthLayoutRegistry layouts;

    public CpfExternalInstitutionRuntimeApplier(
            CpfServiceEndpointRegistry registry,
            CpfFixedLengthLayoutRegistry layouts) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
        this.layouts = layouts;
    }

    @Override public String changeType() { return "EXTERNAL_INSTITUTION"; }
    @Override public boolean supportsIdempotentReplay() { return true; }
    @Override public boolean snapshotCapable() { return true; }

    @Override
    @SuppressWarnings("unchecked")
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            Object raw = delivery.payload().get("endpoints");
            if (!(raw instanceof List<?> entries)) {
                throw new IllegalArgumentException("endpoints array가 필요합니다.");
            }
            LinkedHashMap<String, CpfServiceEndpointRegistry.RuntimeEndpoint> next = new LinkedHashMap<>();
            for (Object entry : entries) {
                if (!(entry instanceof Map<?, ?> rawEndpoint)) {
                    throw new IllegalArgumentException("endpoint object가 필요합니다.");
                }
                Map<String, Object> endpoint = (Map<String, Object>) rawEndpoint;
                String serviceId = required(endpoint, "serviceId");
                String layoutId = optional(endpoint, "layoutId", "");
                String layoutVersion = optional(endpoint, "layoutVersion", "");
                validateLayout(layoutId, layoutVersion);

                LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
                if (endpoint.get("attributes") instanceof Map<?, ?> values) {
                    values.forEach((key, value) -> attributes.put(String.valueOf(key), String.valueOf(value)));
                }
                var candidate = new CpfServiceEndpointRegistry.RuntimeEndpoint(
                        serviceId,
                        optional(endpoint, "endpointType", "HTTP"),
                        required(endpoint, "baseUrl"),
                        optional(endpoint, "credentialRef", ""),
                        layoutId,
                        layoutVersion,
                        integer(endpoint.get("timeoutMillis"), 3_000),
                        bool(endpoint, "active", true),
                        bool(endpoint, "maintenance", false),
                        attributes);
                if (next.putIfAbsent(serviceId, candidate) != null) {
                    throw new IllegalArgumentException("serviceId가 중복되었습니다: " + serviceId);
                }
            }
            registry.replaceRuntime(delivery.desiredVersion(), next);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("EXTERNAL_INSTITUTION_INVALID", ex.getMessage());
        }
    }

    private void validateLayout(String layoutId, String layoutVersion) {
        if (layoutId.isBlank() && layoutVersion.isBlank()) return;
        if (layoutId.isBlank() || layoutVersion.isBlank() || layouts == null) {
            throw new IllegalArgumentException("layoutId/layoutVersion/registry가 함께 필요합니다.");
        }
        layouts.require(layoutId, layoutVersion);
    }

    private String required(Map<String, Object> source, String key) {
        String value = optional(source, key, "");
        if (value.isBlank()) throw new IllegalArgumentException(key + "가 필요합니다.");
        return value;
    }

    private String optional(Map<String, Object> source, String key, String fallback) {
        Object value = source.get(key);
        return value == null ? fallback : String.valueOf(value).trim();
    }

    private boolean bool(Map<String, Object> source, String key, boolean fallback) {
        Object value = source.get(key);
        return value instanceof Boolean flag ? flag : value == null ? fallback : Boolean.parseBoolean(String.valueOf(value));
    }

    private int integer(Object value, int fallback) {
        long parsed = value instanceof Number number ? number.longValue()
                : value == null ? fallback : Long.parseLong(String.valueOf(value));
        if (parsed < 1 || parsed > 300_000) {
            throw new IllegalArgumentException("timeoutMillis는 1~300000 범위여야 합니다.");
        }
        return Math.toIntExact(parsed);
    }
}
