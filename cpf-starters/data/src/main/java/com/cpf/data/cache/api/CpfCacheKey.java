package com.cpf.data.cache.api;

import java.util.Objects;

/** CPF provider-neutral cache key. */
/** CpfCacheKey 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCacheKey(String namespace, String key, String tenantId) {
    public CpfCacheKey {
        namespace = token(namespace, "namespace");
        key = token(key, "key");
        tenantId = token(tenantId, "tenantId");
    }
    public String canonical() { return "cpf:" + tenantId + ":" + namespace + ":" + key; }
    private static String token(String value, String name) {
        String normalized = Objects.requireNonNull(value, name).trim();
        if (normalized.isEmpty() || normalized.length() > 180 || !normalized.matches("[A-Za-z0-9._:-]+")) {
            throw new IllegalArgumentException(name + " format is invalid");
        }
        return normalized;
    }
}
