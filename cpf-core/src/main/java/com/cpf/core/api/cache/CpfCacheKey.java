package com.cpf.core.api.cache;

import java.util.Locale;
import java.util.Objects;

/** CPF Cache Provider와 Consumer가 공유하는 topology-independent key입니다. */
public record CpfCacheKey(String namespace, String key, String tenantId) {
    public CpfCacheKey {
        namespace = segment(normalize(namespace, "namespace"), "namespace").toLowerCase(Locale.ROOT);
        key = normalize(key, "key");
        tenantId = tenantId == null || tenantId.isBlank()
                ? "GLOBAL" : segment(tenantId.trim(), "tenantId");
        if (namespace.length() > 80 || key.length() > 512 || tenantId.length() > 80) {
            throw new IllegalArgumentException("CPF cache key 구성값이 허용 길이를 초과했습니다.");
        }
    }
    public String canonical() { return "cpf:" + tenantId + ":" + namespace + ":" + key; }
    private static String segment(String value, String field) {
        if (!value.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException(field + "에는 영문자, 숫자, 점, 밑줄, 하이픈만 사용할 수 있습니다.");
        }
        return value;
    }

    private static String normalize(String value, String field) {
        String result = Objects.requireNonNull(value, field + "는 필수입니다.").trim();
        if (result.isEmpty()) throw new IllegalArgumentException(field + "는 비어 있을 수 없습니다.");
        if (result.indexOf('\n') >= 0 || result.indexOf('\r') >= 0 || result.indexOf('\0') >= 0) {
            throw new IllegalArgumentException(field + "에 제어문자를 사용할 수 없습니다.");
        }
        return result;
    }
}
