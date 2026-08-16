package com.cpf.core.api.version;

import java.util.Objects;

/**
 * CPF platform/component 버전의 topology-independent immutable value입니다.
 * Classpath/package metadata 접근은 Foundation runtime loader가 소유합니다.
 */
public record CpfPlatformVersion(
        String platformVersion,
        String compatibleRange,
        String componentVersion,
        String component) {
    public CpfPlatformVersion {
        platformVersion = normalize(platformVersion);
        compatibleRange = normalize(compatibleRange);
        componentVersion = normalize(componentVersion);
        component = normalize(component);
    }

    /** unknown 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfPlatformVersion unknown() {
        return new CpfPlatformVersion("UNKNOWN", "UNKNOWN", "UNKNOWN", "cpf");
    }

    private static String normalize(String value) {
        String v = Objects.toString(value, "").trim();
        return v.isEmpty() ? "UNKNOWN" : v;
    }
}
