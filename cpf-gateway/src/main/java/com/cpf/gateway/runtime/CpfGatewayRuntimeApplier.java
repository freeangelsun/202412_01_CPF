package com.cpf.gateway.runtime;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public final class CpfGatewayRuntimeApplier implements CpfRuntimeChangeApplier {
    private final String type;
    private final CpfGatewayRuntimePolicy policy;

    public CpfGatewayRuntimeApplier(String type, CpfGatewayRuntimePolicy policy) {
        this.type = type;
        this.policy = policy;
    }

    public String changeType() { return type; }
    public boolean supportsIdempotentReplay() { return true; }
    public boolean snapshotCapable() { return true; }

    public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
        try {
            CpfRuntimePayload payload = delivery.payload();
            switch (type) {
                case "GATEWAY_HEADER" -> policy.replaceHeaders(
                        delivery.desiredVersion(),
                        set(payload, "requestAllowlist"),
                        set(payload, "responseAllowlist"));
                case "GATEWAY_CORS" -> policy.replaceCors(
                        delivery.desiredVersion(),
                        payload.booleanValue("enabled", true),
                        set(payload, "allowedOrigins"),
                        set(payload, "allowedMethods"),
                        set(payload, "allowedHeaders"),
                        set(payload, "exposedHeaders"),
                        payload.booleanValue("allowCredentials", false),
                        payload.longValue("maxAgeSeconds", 3_600));
                case "RATE_LIMIT" -> policy.replaceRates(
                        delivery.desiredVersion(),
                        limit(payload),
                        limits(payload, "routes"),
                        limits(payload, "clients"),
                        limits(payload, "channels"),
                        limits(payload, "tenants"),
                        payload.booleanValue("failClosedOnCounterFailure", true));
                default -> throw new IllegalArgumentException("unsupported");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    type + "_INVALID",
                    "Gateway runtime policy payload 오류");
        }
    }

    private Map<String, CpfGatewayRuntimePolicy.Limit> limits(
            CpfRuntimePayload payload, String fieldName) {
        Map<String, CpfGatewayRuntimePolicy.Limit> values = new LinkedHashMap<>();
        payload.objectMap(fieldName).forEach((id, item) -> {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException(fieldName + " key is required");
            }
            if (values.putIfAbsent(id.trim(), limit(item)) != null) {
                throw new IllegalArgumentException("duplicate " + fieldName + " key: " + id);
            }
        });
        return Map.copyOf(values);
    }

    private CpfGatewayRuntimePolicy.Limit limit(CpfRuntimePayload payload) {
        return new CpfGatewayRuntimePolicy.Limit(
                boundedInt(payload.longValue("permits", 0), "permits"),
                payload.longValue("windowMillis", 60_000),
                boundedInt(payload.longValue("burst", 0), "burst"),
                boundedInt(payload.longValue("abuseThreshold", 0), "abuseThreshold"),
                payload.longValue("blockMillis", 0));
    }

    private static int boundedInt(long value, String fieldName) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException(fieldName + " is outside integer range", overflow);
        }
    }

    private Set<String> set(CpfRuntimePayload payload, String fieldName) {
        return Set.copyOf(new LinkedHashSet<>(payload.stringList(fieldName)));
    }
}
