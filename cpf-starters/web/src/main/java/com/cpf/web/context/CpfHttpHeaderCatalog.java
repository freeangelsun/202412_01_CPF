package com.cpf.web.context;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Single source of truth for CPF-owned HTTP header policy. */
public final class CpfHttpHeaderCatalog {
    private CpfHttpHeaderCatalog() {}

    public static final Map<String, CpfHttpHeaderSpec> SPECS;
    public static final Set<String> REQUIRED_INTERNAL;
    public static final Set<String> CANONICAL_TRANSACTION;
    public static final Set<String> PROTECTED;

    static {
        LinkedHashMap<String, CpfHttpHeaderSpec> specs = new LinkedHashMap<>();
        required(specs, CpfHttpHeaderNames.TRANSACTION_ID);
        required(specs, CpfHttpHeaderNames.ORIGINAL_CHANNEL);
        required(specs, CpfHttpHeaderNames.CURRENT_CHANNEL);
        required(specs, CpfHttpHeaderNames.CALLER_CHANNEL);
        required(specs, CpfHttpHeaderNames.TARGET_CHANNEL);
        required(specs, CpfHttpHeaderNames.TARGET_OPERATION_ID);

        optionalInternal(specs, CpfHttpHeaderNames.COUNTRY_CODE, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.CLIENT_ID, CpfHeaderLogPolicy.MASKED);
        optionalInternal(specs, CpfHttpHeaderNames.CLIENT_INSTANCE_ID, CpfHeaderLogPolicy.MASKED);
        optionalInternal(specs, CpfHttpHeaderNames.CLIENT_VERSION, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.DEVICE_ID, CpfHeaderLogPolicy.MASKED);
        endToEnd(specs, CpfHttpHeaderNames.CORRELATION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        endToEnd(specs, CpfHttpHeaderNames.IDEMPOTENCY_KEY, CpfHeaderLogPolicy.MASKED);
        frameworkManaged(specs, CpfHttpHeaderNames.TRACEPARENT, CpfHeaderLogPolicy.IDENTIFIER);
        frameworkManaged(specs, CpfHttpHeaderNames.TRACESTATE, CpfHeaderLogPolicy.MASKED);
        optionalInternal(specs, CpfHttpHeaderNames.USER_AGENT, CpfHeaderLogPolicy.MASKED);
        optionalInternal(specs, CpfHttpHeaderNames.ACCEPT_LANGUAGE, CpfHeaderLogPolicy.IDENTIFIER);

        neverTrust(specs, CpfHttpHeaderNames.AUTHORIZATION);
        neverTrust(specs, CpfHttpHeaderNames.API_KEY);
        neverTrust(specs, CpfHttpHeaderNames.FORWARDED);
        neverTrust(specs, CpfHttpHeaderNames.X_FORWARDED_FOR);
        neverTrust(specs, CpfHttpHeaderNames.X_FORWARDED_HOST);
        neverTrust(specs, CpfHttpHeaderNames.X_FORWARDED_PROTO);

        ensureCaseInsensitiveUnique(specs);
        SPECS = Map.copyOf(specs);
        REQUIRED_INTERNAL = SPECS.values().stream().filter(CpfHttpHeaderSpec::requiredInternal)
                .map(CpfHttpHeaderSpec::name).collect(Collectors.toUnmodifiableSet());
        CANONICAL_TRANSACTION = Set.of(
                CpfHttpHeaderNames.TRANSACTION_ID,
                CpfHttpHeaderNames.ORIGINAL_CHANNEL,
                CpfHttpHeaderNames.CURRENT_CHANNEL,
                CpfHttpHeaderNames.CALLER_CHANNEL,
                CpfHttpHeaderNames.TARGET_CHANNEL,
                CpfHttpHeaderNames.TARGET_OPERATION_ID);
        PROTECTED = SPECS.values().stream()
                .filter(spec -> spec.requiredInternal()
                        || spec.mutation() == CpfHeaderMutationPolicy.CANONICALIZE
                        || spec.mutation() == CpfHeaderMutationPolicy.REGENERATE
                        || spec.mutation() == CpfHeaderMutationPolicy.DROP)
                .map(CpfHttpHeaderSpec::name).collect(Collectors.toUnmodifiableSet());
    }

    public static boolean isProtected(String name) {
        return name != null && PROTECTED.stream().anyMatch(v -> v.equalsIgnoreCase(name));
    }

    public static boolean isCanonicalTransaction(String name) {
        return name != null && CANONICAL_TRANSACTION.stream().anyMatch(v -> v.equalsIgnoreCase(name));
    }

    public static CpfHttpHeaderSpec find(String name) {
        if (name == null || name.isBlank()) return null;
        for (CpfHttpHeaderSpec spec : SPECS.values()) if (spec.name().equalsIgnoreCase(name)) return spec;
        return null;
    }

    private static void required(Map<String,CpfHttpHeaderSpec> specs, String name) {
        specs.put(name, new CpfHttpHeaderSpec(name, CpfHeaderPropagationScope.INTERNAL_ONLY,
                CpfHeaderTrustLevel.INTERNAL_SIGNED, CpfHeaderMutationPolicy.CANONICALIZE,
                CpfHeaderDirection.BOTH, CpfHeaderCompatibility.CANONICAL,
                CpfHeaderLogPolicy.IDENTIFIER, true));
    }

    private static void optionalInternal(Map<String,CpfHttpHeaderSpec> specs, String name, CpfHeaderLogPolicy logPolicy) {
        specs.put(name, new CpfHttpHeaderSpec(name, CpfHeaderPropagationScope.INTERNAL_ONLY,
                CpfHeaderTrustLevel.UNTRUSTED, CpfHeaderMutationPolicy.PRESERVE,
                CpfHeaderDirection.BOTH, CpfHeaderCompatibility.CANONICAL, logPolicy, false));
    }

    private static void endToEnd(Map<String,CpfHttpHeaderSpec> specs, String name, CpfHeaderLogPolicy logPolicy) {
        specs.put(name, new CpfHttpHeaderSpec(name, CpfHeaderPropagationScope.END_TO_END,
                CpfHeaderTrustLevel.UNTRUSTED, CpfHeaderMutationPolicy.PRESERVE,
                CpfHeaderDirection.BOTH, CpfHeaderCompatibility.CANONICAL, logPolicy, false));
    }

    private static void frameworkManaged(Map<String,CpfHttpHeaderSpec> specs, String name, CpfHeaderLogPolicy logPolicy) {
        specs.put(name, new CpfHttpHeaderSpec(name, CpfHeaderPropagationScope.END_TO_END,
                CpfHeaderTrustLevel.EDGE_ASSERTED, CpfHeaderMutationPolicy.REGENERATE,
                CpfHeaderDirection.BOTH, CpfHeaderCompatibility.CANONICAL, logPolicy, false));
    }

    private static void neverTrust(Map<String,CpfHttpHeaderSpec> specs, String name) {
        specs.put(name, new CpfHttpHeaderSpec(name, CpfHeaderPropagationScope.NEVER,
                CpfHeaderTrustLevel.UNTRUSTED, CpfHeaderMutationPolicy.DROP,
                CpfHeaderDirection.BOTH, CpfHeaderCompatibility.CANONICAL,
                CpfHeaderLogPolicy.NEVER, false));
    }

    private static void ensureCaseInsensitiveUnique(Map<String,CpfHttpHeaderSpec> specs) {
        java.util.HashSet<String> names = new java.util.HashSet<>();
        for (String name : specs.keySet()) {
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new IllegalStateException("Duplicate CPF HTTP header catalog entry: " + name);
            }
        }
    }
}
