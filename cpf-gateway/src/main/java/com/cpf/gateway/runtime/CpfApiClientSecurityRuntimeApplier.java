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
                Instant expiry = expiresAt == null || expiresAt.isBlank() ? null : Instant.parse(expiresAt);
                CpfApiClientSecurityPolicy.Client client = new CpfApiClientSecurityPolicy.Client(
                        clientId,
                        required(item, "keyHash"),
                        item.booleanValue("active", true),
                        set(item, "allowedIpCidrs"),
                        set(item, "certificateSerials"),
                        expiry,
                        exactInt(item.longValue("quotaPermits", 0L), "quotaPermits"),
                        item.longValue("quotaWindowMillis", 60_000L),
                        set(item, "authorities"));
                if (clients.putIfAbsent(clientId, client) != null) {
                    throw new IllegalArgumentException("API client ID 중복: " + clientId);
                }
            }
            policy.replace(delivery.desiredVersion(), clients);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException invalidPolicy) {
            return CpfRuntimeApplyResult.failure(
                    "API_CLIENT_INVALID",
                    "API client key/IP/cert/expiry/version 정책 오류");
        }
    }

    private static int exactInt(long value, String fieldName) {
        try {
            return Math.toIntExact(value);
        } catch (ArithmeticException overflow) {
            throw new IllegalArgumentException(fieldName + " integer 범위 오류", overflow);
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
