package com.cpf.core.api.security.crypto;

import java.util.Locale;
import java.util.Set;

/** Topology-independent cryptographic agility policy. */
/** CpfCryptoPolicy 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCryptoPolicy(
        Set<String> allowedAlgorithms,
        Set<String> allowedProviders,
        Set<String> deprecatedAlgorithms,
        int minimumKeyBits,
        boolean pqcReady) {
    public CpfCryptoPolicy {
        allowedAlgorithms = normalize(allowedAlgorithms);
        allowedProviders = normalize(allowedProviders);
        deprecatedAlgorithms = normalize(deprecatedAlgorithms);
        if (allowedAlgorithms.isEmpty()) throw new IllegalArgumentException("allowedAlgorithms must not be empty");
        if (minimumKeyBits < 128) throw new IllegalArgumentException("minimumKeyBits must be >= 128");
    }
    /** assertAllowed 작업을 CPF 표준 계약에 따라 수행한다. */
    public void assertAllowed(String algorithm, String provider, int keyBits) {
        String a = normalize(algorithm);
        String p = normalize(provider);
        if (deprecatedAlgorithms.contains(a)) throw new IllegalArgumentException("Deprecated algorithm rejected: " + algorithm);
        if (!allowedAlgorithms.contains(a)) throw new IllegalArgumentException("Algorithm not allowed: " + algorithm);
        if (!allowedProviders.isEmpty() && !allowedProviders.contains(p)) throw new IllegalArgumentException("Provider not allowed: " + provider);
        if (keyBits < minimumKeyBits) throw new IllegalArgumentException("Key strength below policy: " + keyBits);
    }
    private static Set<String> normalize(Set<String> values) {
        if (values == null) return Set.of();
        return values.stream().filter(v -> v != null && !v.isBlank()).map(CpfCryptoPolicy::normalize).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }
    private static String normalize(String value) { return value == null ? "" : value.trim().toUpperCase(Locale.ROOT); }
}
