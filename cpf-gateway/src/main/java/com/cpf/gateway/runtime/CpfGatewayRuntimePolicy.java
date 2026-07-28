package com.cpf.gateway.runtime;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/** Gateway 요청 처리 경로가 직접 소비하는 immutable Runtime 정책입니다. */
public final class CpfGatewayRuntimePolicy {
    private final AtomicReference<HeaderPolicy> headers = new AtomicReference<>(HeaderPolicy.defaults());
    private final AtomicReference<CorsPolicy> cors = new AtomicReference<>(CorsPolicy.disabled());
    private final AtomicReference<RateSnapshot> rates = new AtomicReference<>(RateSnapshot.unlimited());
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AtomicLong operations = new AtomicLong();
    private final Clock clock;

    public CpfGatewayRuntimePolicy() { this(Clock.systemUTC()); }
    CpfGatewayRuntimePolicy(Clock clock) { this.clock = clock; }

    public HeaderPolicy headers() { return headers.get(); }
    public CorsPolicy cors() { return cors.get(); }
    public RateSnapshot rates() { return rates.get(); }

    public HeaderPolicy replaceHeaders(long version, Set<String> request, Set<String> response) {
        HeaderPolicy next = new HeaderPolicy(version, normalize(request), normalize(response));
        headers.set(next); return next;
    }

    public CorsPolicy replaceCors(long version, boolean enabled, Set<String> origins, Set<String> methods,
                                  Set<String> allowedHeaders, Set<String> exposedHeaders,
                                  boolean credentials, long maxAgeSeconds) {
        CorsPolicy next = new CorsPolicy(version, enabled, normalizeOrigins(origins), upper(methods),
                normalize(allowedHeaders), normalize(exposedHeaders), credentials, maxAgeSeconds);
        cors.set(next); return next;
    }

    public RateSnapshot replaceRates(long version, Limit defaultLimit, Map<String,Limit> routeLimits) {
        LinkedHashMap<String,Limit> normalized = new LinkedHashMap<>();
        if (routeLimits != null) routeLimits.forEach((k,v) -> {
            if (k != null && !k.isBlank() && v != null) normalized.put(k.trim(), v);
        });
        RateSnapshot next = new RateSnapshot(version, defaultLimit == null ? Limit.unlimited() : defaultLimit,
                Map.copyOf(normalized));
        rates.set(next); buckets.clear(); return next;
    }

    public boolean allowRequestHeader(String name) {
        Set<String> allow = headers.get().requestAllowlist();
        return allow.isEmpty() || allow.contains(normalize(name));
    }
    public boolean allowResponseHeader(String name) {
        Set<String> allow = headers.get().responseAllowlist();
        return allow.isEmpty() || allow.contains(normalize(name));
    }

    public CorsDecision evaluateCors(String origin, String method, List<String> requestedHeaders) {
        CorsPolicy policy = cors.get();
        if (origin == null || origin.isBlank()) return CorsDecision.notCors();
        if (!policy.enabled()) return CorsDecision.denied("CORS_DISABLED");
        String normalizedOrigin = origin.trim();
        if (!(policy.allowedOrigins().contains("*") || policy.allowedOrigins().contains(normalizedOrigin)))
            return CorsDecision.denied("ORIGIN_DENIED");
        String normalizedMethod = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        if (!policy.allowedMethods().contains(normalizedMethod)) return CorsDecision.denied("METHOD_DENIED");
        for (String header : requestedHeaders == null ? List.<String>of() : requestedHeaders) {
            if (!policy.allowedHeaders().isEmpty() && !policy.allowedHeaders().contains(normalize(header)))
                return CorsDecision.denied("HEADER_DENIED");
        }
        String allowOrigin = policy.allowedOrigins().contains("*") && !policy.allowCredentials() ? "*" : normalizedOrigin;
        return new CorsDecision(true, allowOrigin, policy.allowCredentials(), policy.exposedHeaders(), policy.maxAgeSeconds(), "ALLOWED");
    }

    public boolean tryAcquire(String executionId, String principalId, String channelCode) {
        RateSnapshot snapshot = rates.get();
        Limit limit = snapshot.routeLimits().getOrDefault(executionId, snapshot.defaultLimit());
        if (limit.permits() < 1) return true;
        long now = Instant.now(clock).toEpochMilli();
        long window = Math.max(1_000L, limit.windowMillis());
        long windowStart = now - Math.floorMod(now, window);
        String subject = principalId != null && !principalId.isBlank() && !"anonymous".equalsIgnoreCase(principalId)
                ? principalId.trim() : (channelCode == null || channelCode.isBlank() ? "ANONYMOUS" : channelCode.trim());
        String key = executionId + '|' + subject;
        Bucket bucket = buckets.compute(key, (ignored, current) -> {
            if (current == null || current.windowStart() != windowStart) return new Bucket(windowStart, 1);
            return new Bucket(windowStart, current.count() + 1);
        });
        if ((operations.incrementAndGet() & 1023L) == 0L) buckets.entrySet().removeIf(e -> e.getValue().windowStart() + window * 2 < now);
        return bucket.count() <= limit.permits();
    }

    private static Set<String> normalize(Set<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) if (value != null && !value.isBlank()) result.add(normalize(value));
        return Set.copyOf(result);
    }
    private static Set<String> upper(Set<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) if (value != null && !value.isBlank()) result.add(value.trim().toUpperCase(Locale.ROOT));
        return Set.copyOf(result);
    }
    private static Set<String> normalizeOrigins(Set<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) if (value != null && !value.isBlank()) result.add(value.trim());
        return Set.copyOf(result);
    }
    private static String normalize(String value) { return value == null ? "" : value.trim().toLowerCase(Locale.ROOT); }

    public record HeaderPolicy(long version, Set<String> requestAllowlist, Set<String> responseAllowlist) {
        public HeaderPolicy { requestAllowlist = requestAllowlist == null ? Set.of() : Set.copyOf(requestAllowlist); responseAllowlist = responseAllowlist == null ? Set.of() : Set.copyOf(responseAllowlist); }
        private static HeaderPolicy defaults() { return new HeaderPolicy(0L, Set.of(), Set.of()); }
    }
    public record CorsPolicy(long version, boolean enabled, Set<String> allowedOrigins, Set<String> allowedMethods,
                             Set<String> allowedHeaders, Set<String> exposedHeaders, boolean allowCredentials, long maxAgeSeconds) {
        public CorsPolicy {
            allowedOrigins=allowedOrigins==null?Set.of():Set.copyOf(allowedOrigins); allowedMethods=allowedMethods==null?Set.of():Set.copyOf(allowedMethods);
            allowedHeaders=allowedHeaders==null?Set.of():Set.copyOf(allowedHeaders); exposedHeaders=exposedHeaders==null?Set.of():Set.copyOf(exposedHeaders);
            if(maxAgeSeconds<0||maxAgeSeconds>86400)throw new IllegalArgumentException("CORS maxAge 범위 오류");
            if(allowCredentials&&allowedOrigins.contains("*"))throw new IllegalArgumentException("credentials=true이면 wildcard origin 금지");
        }
        private static CorsPolicy disabled(){return new CorsPolicy(0L,false,Set.of(),Set.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"),Set.of(),Set.of(),false,3600);}
    }
    public record Limit(int permits,long windowMillis){public Limit{if(permits<0||windowMillis<1000||windowMillis>86400000)throw new IllegalArgumentException("Gateway rate limit 범위 오류");}public static Limit unlimited(){return new Limit(0,60000);}}
    public record RateSnapshot(long version,Limit defaultLimit,Map<String,Limit> routeLimits){public RateSnapshot{defaultLimit=defaultLimit==null?Limit.unlimited():defaultLimit;routeLimits=routeLimits==null?Map.of():Map.copyOf(routeLimits);}private static RateSnapshot unlimited(){return new RateSnapshot(0,Limit.unlimited(),Map.of());}}
    public record CorsDecision(boolean allowed,String allowOrigin,boolean allowCredentials,Set<String> exposedHeaders,long maxAgeSeconds,String reason){private static CorsDecision notCors(){return new CorsDecision(true,"",false,Set.of(),0,"NOT_CORS");}private static CorsDecision denied(String reason){return new CorsDecision(false,"",false,Set.of(),0,reason);}}
    private record Bucket(long windowStart,long count){}
}
