package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRateLimitCounterPort;
import com.cpf.core.api.gateway.CpfGatewayRateLimitPort;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/** Gateway 요청 처리 경로가 직접 소비하는 immutable Runtime 정책입니다. */
public final class CpfGatewayRuntimePolicy implements CpfGatewayRateLimitPort {
    private final AtomicReference<HeaderPolicy> headers = new AtomicReference<>(HeaderPolicy.defaults());
    private final AtomicReference<CorsPolicy> cors = new AtomicReference<>(CorsPolicy.disabled());
    private final AtomicReference<RateSnapshot> rates;
    private final CpfGatewayRateLimitCounterPort counters;
    private final Clock clock;

    public CpfGatewayRuntimePolicy() {
        this(new InMemoryCpfGatewayRateLimitCounterAdapter(100_000), true, Clock.systemUTC());
    }

    public CpfGatewayRuntimePolicy(
            CpfGatewayRateLimitCounterPort counters,
            boolean failClosedOnCounterFailure) {
        this(counters, failClosedOnCounterFailure, Clock.systemUTC());
    }

    CpfGatewayRuntimePolicy(
            CpfGatewayRateLimitCounterPort counters,
            boolean failClosedOnCounterFailure,
            Clock clock) {
        this.counters = Objects.requireNonNull(counters, "counters");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.rates = new AtomicReference<>(RateSnapshot.unlimited(failClosedOnCounterFailure));
    }

    public HeaderPolicy headers() { return headers.get(); }
    public CorsPolicy cors() { return cors.get(); }
    public RateSnapshot rates() { return rates.get(); }

    public HeaderPolicy replaceHeaders(long version, Set<String> request, Set<String> response) {
        HeaderPolicy next = new HeaderPolicy(version, normalize(request), normalize(response));
        for (;;) {
            HeaderPolicy current = headers.get();
            validateVersion("GATEWAY_HEADER", current.version(), next.version(), current.equals(next));
            if (current.equals(next) || headers.compareAndSet(current, next)) return next;
        }
    }

    public CorsPolicy replaceCors(
            long version,
            boolean enabled,
            Set<String> origins,
            Set<String> methods,
            Set<String> allowedHeaders,
            Set<String> exposedHeaders,
            boolean credentials,
            long maxAgeSeconds) {
        CorsPolicy next = new CorsPolicy(version, enabled, normalizeOrigins(origins), upper(methods),
                normalize(allowedHeaders), normalize(exposedHeaders), credentials, maxAgeSeconds);
        for (;;) {
            CorsPolicy current = cors.get();
            validateVersion("GATEWAY_CORS", current.version(), next.version(), current.equals(next));
            if (current.equals(next) || cors.compareAndSet(current, next)) return next;
        }
    }

    /** 기존 route-only payload 호환 경로입니다. */
    public RateSnapshot replaceRates(long version, Limit defaultLimit, Map<String, Limit> routeLimits) {
        RateSnapshot current = rates.get();
        return replaceRates(version, defaultLimit, routeLimits, Map.of(), Map.of(), Map.of(),
                current.failClosedOnCounterFailure());
    }

    public RateSnapshot replaceRates(
            long version,
            Limit defaultLimit,
            Map<String, Limit> routeLimits,
            Map<String, Limit> clientLimits,
            Map<String, Limit> channelLimits,
            Map<String, Limit> tenantLimits,
            boolean failClosedOnCounterFailure) {
        RateSnapshot next = new RateSnapshot(
                version,
                defaultLimit == null ? Limit.unlimited() : defaultLimit,
                normalizeLimits(routeLimits),
                normalizeLimits(clientLimits),
                normalizeLimits(channelLimits),
                normalizeLimits(tenantLimits),
                failClosedOnCounterFailure);
        for (;;) {
            RateSnapshot current = rates.get();
            validateVersion("RATE_LIMIT", current.version(), next.version(), current.equals(next));
            if (current.equals(next) || rates.compareAndSet(current, next)) return next;
        }
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
        if (!(policy.allowedOrigins().contains("*") || policy.allowedOrigins().contains(normalizedOrigin))) {
            return CorsDecision.denied("ORIGIN_DENIED");
        }
        String normalizedMethod = method == null ? "" : method.trim().toUpperCase(Locale.ROOT);
        if (!policy.allowedMethods().contains(normalizedMethod)) return CorsDecision.denied("METHOD_DENIED");
        for (String header : requestedHeaders == null ? List.<String>of() : requestedHeaders) {
            if (!policy.allowedHeaders().isEmpty()
                    && !policy.allowedHeaders().contains(normalize(header))) {
                return CorsDecision.denied("HEADER_DENIED");
            }
        }
        String allowOrigin = policy.allowedOrigins().contains("*") && !policy.allowCredentials()
                ? "*" : normalizedOrigin;
        return new CorsDecision(true, allowOrigin, policy.allowCredentials(),
                policy.exposedHeaders(), policy.maxAgeSeconds(), "ALLOWED");
    }

    @Override
    public Decision acquire(Request request) {
        Objects.requireNonNull(request, "request");
        RateSnapshot snapshot = rates.get();
        List<ScopedLimit> applicable = applicable(snapshot, request);
        Instant observedAt = Instant.now(clock);
        if (applicable.isEmpty()) {
            return new Decision(true, "UNLIMITED", null, Long.MAX_VALUE,
                    observedAt, Duration.ZERO, false, !counters.distributed(), "UNLIMITED");
        }

        long now = observedAt.toEpochMilli();
        List<CpfGatewayRateLimitCounterPort.CounterCommand> commands = new ArrayList<>(applicable.size());
        for (ScopedLimit scoped : applicable) {
            Limit limit = scoped.limit();
            long window = limit.windowMillis();
            long windowStart = now - Math.floorMod(now, window);
            commands.add(new CpfGatewayRateLimitCounterPort.CounterCommand(
                    snapshot.version(),
                    counterKey(snapshot.version(), scoped, request),
                    request.requestId() + '|' + scoped.scope().name(),
                    windowStart,
                    window,
                    limit.permits(),
                    limit.burst(),
                    request.permits(),
                    limit.abuseThreshold(),
                    limit.blockMillis(),
                    now));
        }

        CpfGatewayRateLimitCounterPort.BatchResult batch;
        try {
            batch = counters.consumeAtomically(List.copyOf(commands));
        } catch (RuntimeException counterFailure) {
            ScopedLimit first = applicable.getFirst();
            if (snapshot.failClosedOnCounterFailure()) {
                return new Decision(false, first.policyId(), first.scope(), 0L,
                        Instant.ofEpochMilli(now + 1_000L), Duration.ofSeconds(1),
                        false, true, "COUNTER_UNAVAILABLE");
            }
            return new Decision(true, "COUNTER_BYPASS", null, Long.MAX_VALUE,
                    observedAt, Duration.ZERO, false, true, "ALLOWED_DEGRADED");
        }

        String contractViolation = providerContractViolation(batch, commands);
        if (contractViolation != null) {
            ScopedLimit first = applicable.getFirst();
            return new Decision(false, first.policyId(), first.scope(), 0L,
                    Instant.ofEpochMilli(now + 1_000L), Duration.ofSeconds(1),
                    false, true, "COUNTER_CONTRACT_INVALID");
        }

        long minimumRemaining = Long.MAX_VALUE;
        long maximumReset = now;
        boolean allDuplicate = true;
        for (CpfGatewayRateLimitCounterPort.CounterResult result : batch.results()) {
            allDuplicate &= result.duplicate();
            minimumRemaining = Math.min(minimumRemaining, result.remaining());
            maximumReset = Math.max(maximumReset, result.resetAtEpochMillis());
        }

        boolean degraded = !counters.distributed();
        if (!batch.accepted()) {
            int limitingIndex = batch.limitingIndex();
            ScopedLimit limiting = applicable.get(limitingIndex);
            CpfGatewayRateLimitCounterPort.CounterResult result = batch.results().get(limitingIndex);
            long retryMillis = Math.max(1_000L, result.resetAtEpochMillis() - now);
            return new Decision(false, limiting.policyId(), limiting.scope(), result.remaining(),
                    Instant.ofEpochMilli(result.resetAtEpochMillis()),
                    Duration.ofMillis(retryMillis), result.duplicate(), degraded, result.reason());
        }

        String allowedPolicy = applicable.size() == 1 ? applicable.getFirst().policyId() : "COMPOSITE";
        return new Decision(true, allowedPolicy, null,
                minimumRemaining == Long.MAX_VALUE ? 0L : minimumRemaining,
                Instant.ofEpochMilli(maximumReset), Duration.ZERO,
                allDuplicate, degraded, degraded ? "ALLOWED_DEGRADED" : "ALLOWED");
    }

    /** 기존 Consumer 호환용입니다. 신규 Consumer는 {@link #acquire(Request)}를 사용합니다. */
    public boolean tryAcquire(String executionId, String principalId, String channelCode) {
        return acquire(new Request(
                executionId,
                executionId,
                principalId,
                channelCode,
                "",
                UUID.randomUUID().toString(),
                1,
                Instant.now(clock))).allowed();
    }

    @Override
    public PolicyStatus status() {
        RateSnapshot snapshot = rates.get();
        int apiPolicies = snapshot.routeLimits().size() + (snapshot.defaultLimit().active() ? 1 : 0);
        return new PolicyStatus(
                snapshot.version(), apiPolicies, snapshot.clientLimits().size(),
                snapshot.channelLimits().size(), snapshot.tenantLimits().size(),
                snapshot.failClosedOnCounterFailure(), health());
    }

    @Override
    public Health health() {
        try {
            CpfGatewayRateLimitCounterPort.CounterHealth health = counters.health();
            return new Health(health.ready(), counters.distributed(), health.activeCounters(),
                    health.status(), health.observedAt());
        } catch (RuntimeException failure) {
            return new Health(false, counters.distributed(), 0L,
                    "COUNTER_HEALTH_UNAVAILABLE", Instant.now(clock));
        }
    }

    private static String providerContractViolation(
            CpfGatewayRateLimitCounterPort.BatchResult batch,
            List<CpfGatewayRateLimitCounterPort.CounterCommand> commands) {
        if (batch == null || batch.results().size() != commands.size()) {
            return "INCOMPLETE_RESULT";
        }
        boolean anyDuplicate = batch.results().stream().anyMatch(
                CpfGatewayRateLimitCounterPort.CounterResult::duplicate);
        boolean allDuplicate = batch.results().stream().allMatch(
                CpfGatewayRateLimitCounterPort.CounterResult::duplicate);
        if (anyDuplicate != allDuplicate) {
            return "MIXED_DUPLICATE_STATE";
        }
        if (batch.accepted()) {
            if (batch.results().stream().anyMatch(result -> !result.accepted())) {
                return "ACCEPTED_BATCH_CONTAINS_DENIAL";
            }
        } else {
            int limitingIndex = batch.limitingIndex();
            if (limitingIndex < 0 || limitingIndex >= batch.results().size()
                    || batch.results().get(limitingIndex).accepted()) {
                return "INVALID_LIMITING_RESULT";
            }
            for (int index = 0; index < limitingIndex; index++) {
                if (!batch.results().get(index).accepted()) {
                    return "DENIAL_PRECEDES_LIMITING_INDEX";
                }
            }
        }
        for (int index = 0; index < commands.size(); index++) {
            CpfGatewayRateLimitCounterPort.CounterCommand command = commands.get(index);
            CpfGatewayRateLimitCounterPort.CounterResult result = batch.results().get(index);
            long capacity = Math.addExact((long) command.quota(), command.burst());
            if (result.used() > capacity || result.remaining() > capacity
                    || result.resetAtEpochMillis() < command.resetAtEpochMillis()) {
                return "COUNTER_RANGE_INVALID";
            }
        }
        return null;
    }

    private static List<ScopedLimit> applicable(RateSnapshot snapshot, Request request) {
        List<ScopedLimit> limits = new ArrayList<>(4);
        add(limits, Scope.TENANT, request.tenantId(), snapshot.tenantLimits());
        add(limits, Scope.CLIENT, request.clientId(), snapshot.clientLimits());
        add(limits, Scope.CHANNEL, request.channelId(), snapshot.channelLimits());

        Limit api = snapshot.routeLimits().get(request.executionId());
        String apiId = request.executionId();
        if (api == null) {
            api = snapshot.routeLimits().get(request.routeId());
            apiId = request.routeId();
        }
        if (api == null) {
            api = snapshot.defaultLimit();
            apiId = "DEFAULT";
        }
        if (api.active()) limits.add(new ScopedLimit(Scope.API, apiId, "API:" + opaque(apiId), api));
        return List.copyOf(limits);
    }

    private static void add(
            List<ScopedLimit> limits,
            Scope scope,
            String subject,
            Map<String, Limit> configured) {
        if (subject == null || subject.isBlank()) return;
        Limit limit = configured.get(subject.trim());
        if (limit != null && limit.active()) {
            String normalized = subject.trim();
            limits.add(new ScopedLimit(scope, normalized, scope.name() + ':' + opaque(normalized), limit));
        }
    }

    private static String counterKey(long version, ScopedLimit scoped, Request request) {
        // API는 execution/route 단위, CLIENT/CHANNEL/TENANT는 해당 주체 전체에 적용합니다.
        // 비-API Scope에 executionId를 섞으면 Route를 변경해 quota를 우회할 수 있습니다.
        return version + "|" + scoped.scope().name() + "|" + opaque(scoped.subject());
    }

    private static String opaque(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest, 0, 16);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static void validateVersion(
            String policyType, long currentVersion, long nextVersion, boolean identical) {
        if (nextVersion < 0L) {
            throw new IllegalArgumentException(policyType + " version must not be negative");
        }
        if (nextVersion < currentVersion) {
            throw new IllegalStateException(policyType + " stale version: " + nextVersion);
        }
        if (nextVersion == currentVersion && !identical) {
            throw new IllegalStateException(policyType + " same-version payload conflict");
        }
    }

    private static Map<String, Limit> normalizeLimits(Map<String, Limit> values) {
        if (values == null || values.isEmpty()) return Map.of();
        LinkedHashMap<String, Limit> normalized = new LinkedHashMap<>();
        values.forEach((key, limit) -> {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("rate-limit key is required");
            }
            if (limit == null) {
                throw new IllegalArgumentException("rate-limit value is required");
            }
            String id = key.trim();
            if (id.length() > 200) {
                throw new IllegalArgumentException("rate-limit key is too long");
            }
            if (normalized.putIfAbsent(id, limit) != null) {
                throw new IllegalArgumentException("duplicate rate-limit key: " + id);
            }
        });
        return Map.copyOf(normalized);
    }

    private static Set<String> normalize(Set<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) result.add(normalize(value));
        }
        return Set.copyOf(result);
    }

    private static Set<String> upper(Set<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value.trim().toUpperCase(Locale.ROOT));
            }
        }
        return Set.copyOf(result);
    }

    private static Set<String> normalizeOrigins(Set<String> values) {
        if (values == null) return Set.of();
        LinkedHashSet<String> result = new LinkedHashSet<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) result.add(value.trim());
        }
        return Set.copyOf(result);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record HeaderPolicy(long version, Set<String> requestAllowlist, Set<String> responseAllowlist) {
        public HeaderPolicy {
            requestAllowlist = requestAllowlist == null ? Set.of() : Set.copyOf(requestAllowlist);
            responseAllowlist = responseAllowlist == null ? Set.of() : Set.copyOf(responseAllowlist);
        }

        private static HeaderPolicy defaults() {
            return new HeaderPolicy(0L, Set.of(), Set.of());
        }
    }

    public record CorsPolicy(
            long version,
            boolean enabled,
            Set<String> allowedOrigins,
            Set<String> allowedMethods,
            Set<String> allowedHeaders,
            Set<String> exposedHeaders,
            boolean allowCredentials,
            long maxAgeSeconds) {
        public CorsPolicy {
            allowedOrigins = allowedOrigins == null ? Set.of() : Set.copyOf(allowedOrigins);
            allowedMethods = allowedMethods == null ? Set.of() : Set.copyOf(allowedMethods);
            allowedHeaders = allowedHeaders == null ? Set.of() : Set.copyOf(allowedHeaders);
            exposedHeaders = exposedHeaders == null ? Set.of() : Set.copyOf(exposedHeaders);
            if (maxAgeSeconds < 0 || maxAgeSeconds > 86_400) {
                throw new IllegalArgumentException("CORS maxAge 범위 오류");
            }
            if (allowCredentials && allowedOrigins.contains("*")) {
                throw new IllegalArgumentException("credentials=true이면 wildcard origin 금지");
            }
        }

        private static CorsPolicy disabled() {
            return new CorsPolicy(0L, false, Set.of(),
                    Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
                    Set.of(), Set.of(), false, 3_600);
        }
    }

    public record Limit(
            int permits,
            long windowMillis,
            int burst,
            int abuseThreshold,
            long blockMillis) {
        public Limit(int permits, long windowMillis) {
            this(permits, windowMillis, 0, 0, 0L);
        }

        public Limit {
            if (permits < 0 || permits > 10_000_000
                    || windowMillis < 1_000L || windowMillis > 86_400_000L
                    || burst < 0 || burst > 10_000_000
                    || abuseThreshold < 0 || abuseThreshold > 1_000_000
                    || blockMillis < 0L || blockMillis > 86_400_000L) {
                throw new IllegalArgumentException("Gateway rate limit 범위 오류");
            }
        }

        public boolean active() {
            return permits > 0;
        }

        public static Limit unlimited() {
            return new Limit(0, 60_000L, 0, 0, 0L);
        }
    }

    public record RateSnapshot(
            long version,
            Limit defaultLimit,
            Map<String, Limit> routeLimits,
            Map<String, Limit> clientLimits,
            Map<String, Limit> channelLimits,
            Map<String, Limit> tenantLimits,
            boolean failClosedOnCounterFailure) {
        public RateSnapshot(long version, Limit defaultLimit, Map<String, Limit> routeLimits) {
            this(version, defaultLimit, routeLimits, Map.of(), Map.of(), Map.of(), true);
        }

        public RateSnapshot {
            defaultLimit = defaultLimit == null ? Limit.unlimited() : defaultLimit;
            routeLimits = routeLimits == null ? Map.of() : Map.copyOf(routeLimits);
            clientLimits = clientLimits == null ? Map.of() : Map.copyOf(clientLimits);
            channelLimits = channelLimits == null ? Map.of() : Map.copyOf(channelLimits);
            tenantLimits = tenantLimits == null ? Map.of() : Map.copyOf(tenantLimits);
        }

        private static RateSnapshot unlimited(boolean failClosed) {
            return new RateSnapshot(0L, Limit.unlimited(), Map.of(), Map.of(), Map.of(), Map.of(), failClosed);
        }
    }

    public record CorsDecision(
            boolean allowed,
            String allowOrigin,
            boolean allowCredentials,
            Set<String> exposedHeaders,
            long maxAgeSeconds,
            String reason) {
        private static CorsDecision notCors() {
            return new CorsDecision(true, "", false, Set.of(), 0, "NOT_CORS");
        }

        private static CorsDecision denied(String reason) {
            return new CorsDecision(false, "", false, Set.of(), 0, reason);
        }
    }

    private record ScopedLimit(Scope scope, String subject, String policyId, Limit limit) {
    }
}
