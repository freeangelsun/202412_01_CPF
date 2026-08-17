package com.cpf.web.context;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/** Produces a bounded, read-only header snapshot safe for file/DB log and ADM evidence. */
public final class CpfHttpHeaderLogSanitizer {
    private static final int MAX_LOG_VALUE = 256;
    private final CpfHeaderPolicyRegistry policies;

    public CpfHttpHeaderLogSanitizer(CpfHeaderPolicyRegistry policies) {
        this.policies = policies == null ? new CpfHeaderPolicyRegistry(null) : policies;
    }

    public Map<String, String> sanitize(Map<String, ? extends Collection<String>> headers) {
        if (headers == null || headers.isEmpty()) return Map.of();
        LinkedHashMap<String, String> safe = new LinkedHashMap<>();
        headers.forEach((name, values) -> {
            if (name == null || name.isBlank() || values == null || values.isEmpty()) return;
            CpfHeaderLogPolicy policy = policies.logPolicy(name);
            String joined = values.stream().filter(v -> v != null && !v.isBlank())
                    .map(v -> sanitizeValue(v, policy)).reduce((a, b) -> a + "," + b).orElse(null);
            if (joined != null) safe.put(name, joined);
        });
        return Map.copyOf(safe);
    }

    private String sanitizeValue(String value, CpfHeaderLogPolicy policy) {
        if (policy == CpfHeaderLogPolicy.NEVER) return "****";
        String bounded = value.length() <= MAX_LOG_VALUE ? value : value.substring(0, MAX_LOG_VALUE) + "…";
        if (policy == CpfHeaderLogPolicy.IDENTIFIER) return stripControls(bounded);
        String normalized = stripControls(bounded);
        if (normalized.length() <= 4) return "****";
        return normalized.substring(0, 2) + "****" + normalized.substring(normalized.length() - 2);
    }

    private String stripControls(String value) {
        StringBuilder safe = new StringBuilder(value.length());
        value.chars().filter(c -> !Character.isISOControl(c)).forEach(c -> safe.append((char) c));
        return safe.toString();
    }
}
