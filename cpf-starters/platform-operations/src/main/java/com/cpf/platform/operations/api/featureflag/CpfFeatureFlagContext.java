package com.cpf.platform.operations.api.featureflag;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Vendor-neutral targeting context with deterministic ordering and sensitive-key filtering. */
/** CpfFeatureFlagContext 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfFeatureFlagContext(
        String environment,
        String domain,
        String tenant,
        String channel,
        String memberKey,
        Map<String, String> attributes) {
    private static final Set<String> SENSITIVE = Set.of(
            "password", "secret", "token", "authorization", "cookie", "ssn", "residentnumber");

    public CpfFeatureFlagContext {
        var normalized = new TreeMap<String, String>();
        if (attributes != null) {
            attributes.forEach((key, value) -> {
                if (key == null || key.isBlank() || value == null) return;
                String normalizedKey = key.trim();
                if (!SENSITIVE.contains(normalizedKey.toLowerCase(Locale.ROOT))) {
                    normalized.put(normalizedKey, value.trim());
                }
            });
        }
        attributes = Map.copyOf(normalized);
        environment = clean(environment);
        domain = clean(domain);
        tenant = clean(tenant);
        channel = clean(channel);
        memberKey = clean(memberKey);
    }

    /** Compact constructor retained for new consumers that only have a targeting key. */
    /** CpfFeatureFlagContext 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfFeatureFlagContext(String targetingKey, Map<String, String> attributes) {
        this(null, null, null, null, targetingKey, attributes);
    }

    public String targetingKey() {
        return stableTargetKey();
    }

    /** stableTargetKey 작업을 CPF 표준 계약에 따라 수행한다. */
    public String stableTargetKey() {
        if (memberKey != null) return memberKey;
        if (tenant != null) return tenant;
        if (domain != null) return domain;
        return "anonymous";
    }

    /** openFeatureAttributes 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String, String> openFeatureAttributes() {
        var result = new TreeMap<String, String>(attributes);
        put(result, "environment", environment);
        put(result, "domain", domain);
        put(result, "tenant", tenant);
        put(result, "channel", channel);
        return Map.copyOf(result);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void put(Map<String, String> target, String key, String value) {
        if (value != null) target.putIfAbsent(key, value);
    }
}
