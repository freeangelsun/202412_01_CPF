package com.cpf.web.context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * CPF HTTP 표준 Header 정책 Catalog입니다.
 *
 * <p>내부 거래 경계의 필수값은 Catalog 한 곳에서 정의하여 Controller, Domain/Service Call,
 * Gateway/Observability가 서로 다른 필수 Header 집합을 만들지 않게 합니다.</p>
 */
public final class CpfHttpHeaderCatalog {
    private CpfHttpHeaderCatalog() {}

    public static final Map<String, CpfHttpHeaderSpec> SPECS;
    public static final Set<String> REQUIRED_INTERNAL;

    static {
        Map<String, CpfHttpHeaderSpec> specs = new LinkedHashMap<>();
        required(specs, CpfHttpHeaderNames.TRANSACTION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        required(specs, CpfHttpHeaderNames.EXECUTION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.ROOT_TRANSACTION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.ROOT_EXECUTION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.PARENT_EXECUTION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.SEGMENT_ID, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.PARENT_SEGMENT_ID, CpfHeaderLogPolicy.IDENTIFIER);
        optionalInternal(specs, CpfHttpHeaderNames.STANDARD_EXECUTION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        // 내부 hop에서 호출자/대상은 라우팅·감사·권한 추적의 필수값입니다.
        required(specs, CpfHttpHeaderNames.CALLER, CpfHeaderLogPolicy.IDENTIFIER);
        required(specs, CpfHttpHeaderNames.TARGET, CpfHeaderLogPolicy.IDENTIFIER);
        endToEnd(specs, CpfHttpHeaderNames.CORRELATION_ID, CpfHeaderLogPolicy.IDENTIFIER);
        endToEnd(specs, CpfHttpHeaderNames.IDEMPOTENCY_KEY, CpfHeaderLogPolicy.MASKED);
        endToEnd(specs, CpfHttpHeaderNames.TRACEPARENT, CpfHeaderLogPolicy.IDENTIFIER);
        endToEnd(specs, CpfHttpHeaderNames.TRACESTATE, CpfHeaderLogPolicy.MASKED);

        for (String name : List.of(
                CpfHttpHeaderNames.AUTHORIZATION,
                CpfHttpHeaderNames.API_KEY,
                CpfHttpHeaderNames.FORWARDED,
                CpfHttpHeaderNames.X_FORWARDED_FOR,
                CpfHttpHeaderNames.X_FORWARDED_HOST,
                CpfHttpHeaderNames.X_FORWARDED_PROTO)) {
            specs.put(name, new CpfHttpHeaderSpec(
                    name,
                    CpfHeaderPropagationScope.NEVER,
                    CpfHeaderTrustLevel.UNTRUSTED,
                    CpfHeaderMutationPolicy.DROP,
                    CpfHeaderDirection.BOTH,
                    CpfHeaderCompatibility.CANONICAL,
                    CpfHeaderLogPolicy.NEVER,
                    false));
        }
        SPECS = Map.copyOf(specs);
        REQUIRED_INTERNAL = SPECS.values().stream()
                .filter(CpfHttpHeaderSpec::requiredInternal)
                .map(CpfHttpHeaderSpec::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static void required(Map<String, CpfHttpHeaderSpec> specs, String name, CpfHeaderLogPolicy logPolicy) {
        specs.put(name, new CpfHttpHeaderSpec(
                name, CpfHeaderPropagationScope.INTERNAL_ONLY, CpfHeaderTrustLevel.INTERNAL_SIGNED,
                CpfHeaderMutationPolicy.CANONICALIZE, CpfHeaderDirection.BOTH,
                CpfHeaderCompatibility.CANONICAL, logPolicy, true));
    }

    private static void optionalInternal(Map<String, CpfHttpHeaderSpec> specs, String name, CpfHeaderLogPolicy logPolicy) {
        specs.put(name, new CpfHttpHeaderSpec(
                name, CpfHeaderPropagationScope.INTERNAL_ONLY, CpfHeaderTrustLevel.INTERNAL_SIGNED,
                CpfHeaderMutationPolicy.CANONICALIZE, CpfHeaderDirection.BOTH,
                CpfHeaderCompatibility.CANONICAL, logPolicy, false));
    }

    private static void endToEnd(Map<String, CpfHttpHeaderSpec> specs, String name, CpfHeaderLogPolicy logPolicy) {
        specs.put(name, new CpfHttpHeaderSpec(
                name, CpfHeaderPropagationScope.END_TO_END, CpfHeaderTrustLevel.UNTRUSTED,
                CpfHeaderMutationPolicy.CANONICALIZE, CpfHeaderDirection.BOTH,
                CpfHeaderCompatibility.CANONICAL, logPolicy, false));
    }
}
