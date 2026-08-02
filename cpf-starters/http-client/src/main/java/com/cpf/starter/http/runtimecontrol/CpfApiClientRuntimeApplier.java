package com.cpf.starter.http.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.common.http.CpfApiClientRuntimePolicy;
import com.cpf.starter.runtimecontrol.spi.CpfRuntimePayloadReader;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** CpfWebClient typed 요청의 timeout/retry/header 정책을 적용합니다. */
public final class CpfApiClientRuntimeApplier implements CpfRuntimeChangeApplier {
    private final CpfApiClientRuntimePolicy policy;

    public CpfApiClientRuntimeApplier(CpfApiClientRuntimePolicy policy) {
        this.policy = policy;
    }

    @Override
    public String changeType() {
        return "API_CLIENT";
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
            Map<String, Object> payload = CpfRuntimePayloadReader.asMap(delivery.payload());
            var defaults = decode(payload.get("defaults"));
            LinkedHashMap<String, CpfApiClientRuntimePolicy.ClientPolicy> services = new LinkedHashMap<>();
            Object raw = payload.get("services");
            if (raw instanceof Map<?, ?> entries) {
                for (var entry : entries.entrySet()) {
                    services.put(String.valueOf(entry.getKey()), decode(entry.getValue()));
                }
            }
            policy.replace(delivery.desiredVersion(), defaults, services);
            return CpfRuntimeApplyResult.success(delivery.payloadHash());
        } catch (RuntimeException ex) {
            return CpfRuntimeApplyResult.failure("API_CLIENT_INVALID", "API client policy payload 오류");
        }
    }

    @SuppressWarnings("unchecked")
    private CpfApiClientRuntimePolicy.ClientPolicy decode(Object raw) {
        Map<String, Object> value = raw instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : Map.of();
        return new CpfApiClientRuntimePolicy.ClientPolicy(
                (int) number(value.get("timeoutMillis"), 3000),
                (int) number(value.get("retryCount"), 0),
                strings(value.get("allowedHeaders")));
    }

    private long number(Object value, long fallback) {
        if (value instanceof Number number) return number.longValue();
        return value == null ? fallback : Long.parseLong(String.valueOf(value));
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
