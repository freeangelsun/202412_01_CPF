package com.cpf.gateway.runtime;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

public final class CpfApiClientSecurityRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfApiClientSecurityPolicy policy;

    public CpfApiClientSecurityRuntimeApplier(CpfApiClientSecurityPolicy policy) {
        this.policy = policy;
    }

    public String changeType() {
        return "API_CLIENT";
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
            if (!payload.contains("clients")) {
                throw new IllegalArgumentException("clients array 필수");
            }
            LinkedHashMap<String, CpfApiClientSecurityPolicy.Client> clients = new LinkedHashMap<>();
            for (CpfRuntimePayload item : payload.objectList("clients")) {
                String clientId = required(item, "clientId");
                String expiresAt = item.text("expiresAt", null);
                Instant expiry = expiresAt == null ? null : Instant.parse(expiresAt);
                clients.put(clientId, new CpfApiClientSecurityPolicy.Client(
                        clientId,
                        required(item, "keyHash"),
                        item.booleanValue("active", true),
                        set(item, "allowedIpCidrs"),
                        set(item, "certificateSerials"),
                        expiry,
                        (int) item.longValue("quotaPermits", 0),
                        item.longValue("quotaWindowMillis", 60_000),
                        set(item, "authorities")));
            }
            policy.replace(delivery.desiredVersion(), clients);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure(
                    "API_CLIENT_INVALID",
                    "API client key/quota/IP/cert/expiry 정책 오류");
        }
    }

    private String required(CpfRuntimePayload payload, String fieldName) {
        String value = payload.text(fieldName, null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 필수");
        }
        return value.trim();
    }

    private Set<String> set(CpfRuntimePayload payload, String fieldName) {
        return Set.copyOf(new LinkedHashSet<>(payload.stringList(fieldName)));
    }
}
