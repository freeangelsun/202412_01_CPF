package com.cpf.integration.http.runtimecontrol;

import com.cpf.integration.fixedlength.api.CpfFixedLengthLayoutRegistry;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.spi.CpfRuntimePayloadReader;
import com.cpf.integration.http.internal.CpfServiceEndpointRegistry;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/** 기관/외부 시스템 endpoint 정본을 layout version과 timeout까지 검증한 뒤 원자 적용합니다. */
public final class CpfExternalInstitutionRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfServiceEndpointRegistry registry;
    private final CpfFixedLengthLayoutRegistry layouts;

    public CpfExternalInstitutionRuntimeApplier(CpfServiceEndpointRegistry registry, CpfFixedLengthLayoutRegistry layouts) {
        this.registry = java.util.Objects.requireNonNull(registry, "registry");
        this.layouts = layouts;
    }

    @Override

    public String changeType() { return "EXTERNAL_INSTITUTION"; }
    @Override
    public boolean supportsIdempotentReplay() { return true; }
    @Override
    public boolean snapshotCapable() { return true; }

    @Override
    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            JsonNode entries = CpfRuntimePayloadReader.field(delivery.payload(),"endpoints");
            if (!entries.isArray()) throw new IllegalArgumentException("endpoints array가 필요합니다.");
            LinkedHashMap<String, CpfServiceEndpointRegistry.RuntimeEndpoint> next = new LinkedHashMap<>();
            for (JsonNode endpoint : entries) {
                if (!endpoint.isObject()) throw new IllegalArgumentException("endpoint object가 필요합니다.");
                String serviceId = required(endpoint, "serviceId");
                String layoutId = optional(endpoint, "layoutId", "");
                String layoutVersion = optional(endpoint, "layoutVersion", "");
                validateLayout(layoutId, layoutVersion);
                LinkedHashMap<String, String> attributes = new LinkedHashMap<>();
                JsonNode values = endpoint.get("attributes");
                if (values != null && values.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> fields = values.properties().iterator();
                    fields.forEachRemaining(entry -> attributes.put(entry.getKey(), entry.getValue().asText()));
                }
                String baseUrl = validateBaseUrl(required(endpoint, "baseUrl"));
                var candidate = new CpfServiceEndpointRegistry.RuntimeEndpoint(serviceId,
                        optional(endpoint, "endpointType", "HTTP"), baseUrl,
                        optional(endpoint, "credentialRef", ""), layoutId, layoutVersion,
                        integer(endpoint.get("timeoutMillis"), 3_000), bool(endpoint, "active", true),
                        bool(endpoint, "maintenance", false), attributes);
                if (next.putIfAbsent(serviceId, candidate) != null) {
                    throw new IllegalArgumentException("serviceId가 중복되었습니다: " + serviceId);
                }
            }
            CpfServiceEndpointRegistry.Snapshot previous = registry.runtimeSnapshot();
            try {
                CpfServiceEndpointRegistry.Snapshot applied = registry.replaceRuntime(delivery.desiredVersion(), next);
                if (applied.version() != delivery.desiredVersion() || !applied.endpoints().equals(Map.copyOf(next))) {
                    return CpfRuntimeApplyResult.unknown(
                            "EXTERNAL_INSTITUTION_NOT_CONFIRMED",
                            "기관 endpoint snapshot 적용 결과를 확인하지 못했습니다. mutation 재실행 없이 현재 상태를 재확인해야 합니다.");
                }
                return CpfRuntimeApplyResult.success(delivery.payloadHash());
            } catch (RuntimeException applyFailure) {
                CpfServiceEndpointRegistry.Snapshot current = registry.runtimeSnapshot();
                if (current.equals(previous)) {
                    return CpfRuntimeApplyResult.failure(
                            "EXTERNAL_INSTITUTION_APPLY_FAILED",
                            "기관 endpoint snapshot 적용이 거부되었으며 기존 snapshot은 유지되었습니다.");
                }
                return CpfRuntimeApplyResult.unknown(
                        "EXTERNAL_INSTITUTION_APPLY_UNKNOWN",
                        "기관 endpoint snapshot 적용 중 상태가 변경되었습니다. mutation 재실행 없이 현재 snapshot을 재확인해야 합니다.");
            }
        } catch (IllegalArgumentException ex) {
            return CpfRuntimeApplyResult.failure("EXTERNAL_INSTITUTION_INVALID", "기관 endpoint payload가 유효하지 않습니다.");
        }
    }

    private String validateBaseUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(java.util.Locale.ROOT);
            if (!(scheme.equals("http") || scheme.equals("https")) || uri.getHost() == null
                    || uri.getUserInfo() != null || uri.getFragment() != null) {
                throw new IllegalArgumentException("baseUrl 형식 오류");
            }
            return uri.toString().replaceAll("/+$", "");
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("baseUrl은 사용자정보와 fragment가 없는 http(s) URI여야 합니다.", ex);
        }
    }

    private void validateLayout(String layoutId, String layoutVersion) {
        if (layoutId.isBlank() && layoutVersion.isBlank()) return;
        if (layoutId.isBlank() || layoutVersion.isBlank() || layouts == null) {
            throw new IllegalArgumentException("layoutId/layoutVersion/registry가 함께 필요합니다.");
        }
        layouts.require(layoutId, layoutVersion);
    }

    private String required(JsonNode source, String key) {
        String value = optional(source, key, "");
        if (value.isBlank()) throw new IllegalArgumentException(key + "가 필요합니다.");
        return value;
    }

    private String optional(JsonNode source, String key, String fallback) {
        JsonNode value = source.get(key);
        return value == null || value.isNull() || value.isMissingNode() ? fallback : value.asText().trim();
    }

    private boolean bool(JsonNode source, String key, boolean fallback) {
        JsonNode value = source.get(key);
        if (value == null || value.isNull() || value.isMissingNode()) return fallback;
        return value.isBoolean() ? value.booleanValue() : Boolean.parseBoolean(value.asText());
    }

    private int integer(JsonNode value, int fallback) {
        long parsed = value == null || value.isNull() || value.isMissingNode() ? fallback
                : value.isNumber() ? value.longValue() : Long.parseLong(value.asText());
        if (parsed < 1 || parsed > 300_000) throw new IllegalArgumentException("timeoutMillis는 1~300000 범위여야 합니다.");
        return Math.toIntExact(parsed);
    }
}
