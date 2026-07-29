package com.cpf.gateway.runtime;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;

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

    public String changeType() {
        return type;
    }

    public boolean supportsIdempotentReplay() {
        return true;
    }

    public boolean snapshotCapable() {
        return true;
    }

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
                case "RATE_LIMIT" -> {
                    Map<String, CpfGatewayRuntimePolicy.Limit> routes = new LinkedHashMap<>();
                    payload.objectMap("routes").forEach(
                            (routeId, routePayload) -> routes.put(routeId, limit(routePayload)));
                    policy.replaceRates(delivery.desiredVersion(), limit(payload), routes);
                }
                default -> throw new IllegalArgumentException("unsupported");
            }
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    type + "_INVALID",
                    "Gateway runtime policy payload 오류");
        }
    }

    private CpfGatewayRuntimePolicy.Limit limit(CpfRuntimePayload payload) {
        return new CpfGatewayRuntimePolicy.Limit(
                (int) payload.longValue("permits", 0),
                payload.longValue("windowMillis", 60_000));
    }

    private Set<String> set(CpfRuntimePayload payload, String fieldName) {
        return Set.copyOf(new LinkedHashSet<>(payload.stringList(fieldName)));
    }
}
