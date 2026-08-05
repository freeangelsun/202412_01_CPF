package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.CpfLogPolicyCacheRuntimeStatus;

import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import static com.cpf.core.api.logging.policy.LogCaptureMode.CaptureArea;

/** 로그 정책 평가 결과를 짧은 TTL로 보관하는 로컬 캐시입니다. */
public class LogPolicyCache implements CpfLogPolicyCacheRuntimeStatus {
    private static final int DEFAULT_TTL_SECONDS = 30;
    private static final String DEFAULT_PROPERTY_PREFIX = "cpf.log-policy.default.";
    private static final List<String> DEFAULT_PROPERTY_SUFFIXES = List.of(
            "file-log-level",
            "db-log-enabled",
            "query-capture-mode",
            "request-header-capture-mode",
            "response-header-capture-mode",
            "request-body-capture-mode",
            "request-body-save",
            "response-body-capture-mode",
            "response-body-save",
            "error-stack-capture-mode",
            "error-stack-save",
            "query-allowlist",
            "header-allowlist",
            "field-allowlist",
            "max-query-bytes",
            "max-header-bytes",
            "max-request-body-bytes",
            "max-response-body-bytes",
            "max-stack-bytes",
            "masking-policy-key");
    private final LogPolicyRepository repository;
    private final Environment environment;
    private final Duration ttl;
    private final int maximumEntries;
    private final Clock clock;
    private final Map<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Map<CacheKey, CpfLogPolicyVersionSnapshot> versionedPolicies = new ConcurrentHashMap<>();
    private final ReentrantLock capacityLock = new ReentrantLock();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();
    private final AtomicLong refreshCount = new AtomicLong();
    private final AtomicLong evictionCount = new AtomicLong();
    private final AtomicLong failureCount = new AtomicLong();

    /** Compatibility constructor. Auto-configuration should prefer the Clock-aware overload. */
    public LogPolicyCache(LogPolicyRepository repository, Environment environment) {
        this(repository, environment, Clock.systemUTC());
    }

    public LogPolicyCache(LogPolicyRepository repository, Environment environment, Clock clock) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.environment = Objects.requireNonNull(environment, "environment");
        this.clock = Objects.requireNonNull(clock, "clock");
        int ttlSeconds = environment.getProperty(
                "cpf.log-policy.cache.ttl-seconds", Integer.class, DEFAULT_TTL_SECONDS);
        if (ttlSeconds < 1 || ttlSeconds > 3_600) {
            throw new IllegalArgumentException(
                    "cpf.log-policy.cache.ttl-seconds must be between 1 and 3600");
        }
        this.ttl = Duration.ofSeconds(ttlSeconds);
        this.maximumEntries = environment.getProperty(
                "cpf.log-policy.cache.max-entries", Integer.class, 4_096);
        if (maximumEntries < 1 || maximumEntries > 100_000) {
            throw new IllegalArgumentException(
                    "cpf.log-policy.cache.max-entries must be between 1 and 100000");
        }
    }

    public LogPolicyDecision resolve(LogPolicyTargetType targetType, String targetId) {
        CacheKey key = new CacheKey(targetType, LogPolicyDecision.normalizeTargetId(targetId));
        Instant now = clock.instant();
        CpfLogPolicyVersionSnapshot managed = findManaged(key);
        if (managed != null && managed.status() == CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            hitCount.incrementAndGet();
            return managed.decision();
        }
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(now)) {
            hitCount.incrementAndGet();
            return cached.decision();
        }
        missCount.incrementAndGet();
        if (cached != null && cache.remove(key, cached)) evictionCount.incrementAndGet();
        try {
            LogPolicyDecision decision = resolveFresh(targetType, key.targetId());
            putBounded(key, new CacheEntry(decision, safePlus(now, ttl)), now);
            return decision;
        } catch (RuntimeException failure) {
            failureCount.incrementAndGet();
            throw failure;
        }
    }

    public LogPolicyDecision refresh(LogPolicyTargetType targetType, String targetId) {
        CacheKey key = new CacheKey(targetType, LogPolicyDecision.normalizeTargetId(targetId));
        CpfLogPolicyVersionSnapshot managed = findManaged(key);
        if (managed != null && managed.status() == CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            refreshCount.incrementAndGet();
            if (cache.remove(key) != null) evictionCount.incrementAndGet();
            return managed.decision();
        }
        try {
            LogPolicyDecision decision = resolveFresh(targetType, key.targetId());
            Instant now = clock.instant();
            putBounded(key, new CacheEntry(decision, safePlus(now, ttl)), now);
            refreshCount.incrementAndGet();
            return decision;
        } catch (RuntimeException failure) {
            failureCount.incrementAndGet();
            throw failure;
        }
    }

    /** Applies one committed managed version immediately to the actual runtime evaluator. */
    public void applyVersionedPolicy(CpfLogPolicyVersionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (snapshot.status() != CpfLogPolicyVersionSnapshot.Status.ACTIVE) {
            throw new IllegalArgumentException("only ACTIVE managed log policies can be applied");
        }
        CacheKey key = new CacheKey(snapshot.targetType(), snapshot.targetId());
        capacityLock.lock();
        try {
            CpfLogPolicyVersionSnapshot current = versionedPolicies.get(key);
            if (current != null) {
                if (snapshot.version() < current.version()) {
                    throw new IllegalStateException("stale managed log policy version");
                }
                if (snapshot.version() == current.version()) {
                    if (!snapshot.decision().policyChecksum().equals(current.decision().policyChecksum())) {
                        throw new IllegalStateException("managed log policy version checksum conflict");
                    }
                    return;
                }
            }
            boolean newDistinctKey = current == null && !cache.containsKey(key);
            if (newDistinctKey && distinctEntryCount() >= maximumEntries) {
                evictOneEphemeralEntry();
            }
            if (newDistinctKey && distinctEntryCount() >= maximumEntries) {
                throw new IllegalStateException("managed log policy capacity exhausted");
            }
            versionedPolicies.put(key, snapshot);
            if (cache.remove(key) != null) evictionCount.incrementAndGet();
            refreshCount.incrementAndGet();
        } finally {
            capacityLock.unlock();
        }
    }

    public CpfLogPolicyVersionSnapshot versionedPolicy(LogPolicyTargetType targetType, String targetId) {
        return versionedPolicies.get(new CacheKey(targetType, targetId));
    }

    public void evict(LogPolicyTargetType targetType, String targetId) {
        CacheKey key = new CacheKey(targetType, LogPolicyDecision.normalizeTargetId(targetId));
        if (cache.remove(key) != null) evictionCount.incrementAndGet();
    }
    public void clear() {
        int removed = cache.size();
        cache.clear();
        evictionCount.addAndGet(removed);
    }
    int size() { return cache.size(); }

    @Override
    public RuntimeSnapshot logPolicyCacheRuntimeSnapshot() {
        long failures = failureCount.get();
        return new RuntimeSnapshot(
                failures > 0L ? Health.DEGRADED : Health.UP,
                distinctEntryCount(), maximumEntries, ttl,
                hitCount.get(), missCount.get(), refreshCount.get(),
                evictionCount.get(), failures, clock.instant());
    }

    private void putBounded(CacheKey key, CacheEntry entry, Instant now) {
        capacityLock.lock();
        try {
            cache.entrySet().removeIf(candidate -> {
                boolean expired = !candidate.getValue().expiresAt().isAfter(now);
                if (expired) evictionCount.incrementAndGet();
                return expired;
            });
            boolean newDistinctKey = !cache.containsKey(key) && !versionedPolicies.containsKey(key);
            if (newDistinctKey && distinctEntryCount() >= maximumEntries) {
                evictOneEphemeralEntry();
            }
            if (!newDistinctKey || distinctEntryCount() < maximumEntries) {
                cache.put(key, entry);
            }
        } finally {
            capacityLock.unlock();
        }
    }

    private CpfLogPolicyVersionSnapshot findManaged(CacheKey key) {
        CpfLogPolicyVersionSnapshot exact = versionedPolicies.get(key);
        if (exact != null) return exact;
        if ("*".equals(key.targetId())) return null;
        return versionedPolicies.get(new CacheKey(key.targetType(), "*"));
    }

    private int distinctEntryCount() {
        if (cache.isEmpty()) return versionedPolicies.size();
        if (versionedPolicies.isEmpty()) return cache.size();
        java.util.HashSet<CacheKey> keys = new java.util.HashSet<>(cache.keySet());
        keys.addAll(versionedPolicies.keySet());
        return keys.size();
    }

    private void evictOneEphemeralEntry() {
        Map.Entry<CacheKey, CacheEntry> victim = cache.entrySet().stream()
                .min(java.util.Comparator
                        .comparing((Map.Entry<CacheKey, CacheEntry> candidate) ->
                                candidate.getValue().expiresAt())
                        .thenComparing(candidate -> candidate.getKey().targetType().code())
                        .thenComparing(candidate -> candidate.getKey().targetId()))
                .orElse(null);
        if (victim != null && cache.remove(victim.getKey(), victim.getValue())) {
            evictionCount.incrementAndGet();
        }
    }

    private static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MAX;
        }
    }

    private LogPolicyDecision resolveFresh(LogPolicyTargetType type, String targetId) {
        LogPolicyDecision base = repository.findActivePolicy(type, targetId)
                .map(row -> fromRow(type, targetId, row, null))
                .orElseGet(() -> applicationDefault(type, targetId));
        return repository.findActiveOverride(type, targetId, LocalDateTime.ofInstant(clock.instant(), clock.getZone()))
                .map(row -> fromRow(type, targetId, row, base)).orElse(base);
    }

    private LogPolicyDecision fromRow(
            LogPolicyTargetType type, String requestedTargetId, LogPolicyRow row, LogPolicyDecision base) {
        String level = firstText(row.logLevel(), base == null ? null : base.fileLogLevel(), "INFO");
        boolean fileEnabled = yn(row.fileLogEnabledYn(), base == null || !"OFF".equals(base.fileLogLevel()));
        String fileLevel = fileEnabled ? LogPolicyDecision.normalizeLevel(level, "INFO") : "OFF";
        return new LogPolicyDecision(
                first(row.schemaVersion(), base == null ? null : base.schemaVersion(), LogPolicyDecision.CURRENT_SCHEMA_VERSION),
                type.code(), LogPolicyDecision.normalizeTargetId(requestedTargetId), fileLevel,
                yn(row.dbLogEnabledYn(), base == null || base.dbLogEnabled()),
                LogPolicyDecision.normalizeLevel(level, base == null ? "INFO" : base.dbLogLevel()),
                parse(row.queryCaptureMode(), base == null ? LogCaptureMode.NONE : base.queryCaptureMode(), CaptureArea.QUERY),
                parse(row.requestHeaderCaptureMode(), base == null ? LogCaptureMode.NONE : base.requestHeaderCaptureMode(), CaptureArea.HEADER),
                parse(row.responseHeaderCaptureMode(), base == null ? LogCaptureMode.NONE : base.responseHeaderCaptureMode(), CaptureArea.HEADER),
                parse(row.requestBodyCaptureMode(), base == null ? LogCaptureMode.NONE : base.requestBodyCaptureMode(), CaptureArea.BODY),
                parse(row.responseBodyCaptureMode(), base == null ? LogCaptureMode.NONE : base.responseBodyCaptureMode(), CaptureArea.BODY),
                parse(row.errorStackCaptureMode(), base == null ? LogCaptureMode.SUMMARY : base.errorStackCaptureMode(), CaptureArea.STACK),
                csv(row.queryAllowlist(), base == null ? List.of() : base.queryAllowlist()),
                csv(row.headerAllowlist(), base == null ? List.of() : base.headerAllowlist()),
                csv(row.fieldAllowlist(), base == null ? List.of() : base.fieldAllowlist()),
                first(row.maxQueryBytes(), base == null ? null : base.maxQueryBytes(), 4096),
                first(row.maxHeaderBytes(), base == null ? null : base.maxHeaderBytes(), 8192),
                first(row.maxRequestBodyBytes(), base == null ? null : base.maxRequestBodyBytes(), 65536),
                first(row.maxResponseBodyBytes(), base == null ? null : base.maxResponseBodyBytes(), 65536),
                first(row.maxStackBytes(), base == null ? null : base.maxStackBytes(), 32768),
                firstText(row.maskingPolicyKey(), base == null ? null : base.maskingPolicyKey(), "DEFAULT"),
                null, row.source(), row.overrideId(),
                row.policyId() != null ? row.policyId() : base == null ? null : base.policyId());
    }

    private LogPolicyDecision applicationDefault(LogPolicyTargetType type, String targetId) {
        LogPolicyDecision d = LogPolicyDecision.cpfDefault(type, targetId);
        if (!hasApplicationDefault()) return d;
        String level = LogPolicyDecision.normalizeLevel(
                environment.getProperty(DEFAULT_PROPERTY_PREFIX + "file-log-level"), d.fileLogLevel());
        return new LogPolicyDecision(
                LogPolicyDecision.CURRENT_SCHEMA_VERSION, type.code(), LogPolicyDecision.normalizeTargetId(targetId), level,
                booleanProperty("cpf.log-policy.default.db-log-enabled", d.dbLogEnabled()), level,
                propertyMode("query-capture-mode", d.queryCaptureMode(), CaptureArea.QUERY),
                propertyMode("request-header-capture-mode", d.requestHeaderCaptureMode(), CaptureArea.HEADER),
                propertyMode("response-header-capture-mode", d.responseHeaderCaptureMode(), CaptureArea.HEADER),
                bodyPropertyMode("request-body-capture-mode", "request-body-save", d.requestBodyCaptureMode()),
                bodyPropertyMode("response-body-capture-mode", "response-body-save", d.responseBodyCaptureMode()),
                stackPropertyMode(d.errorStackCaptureMode()),
                LogPolicyDecision.parseCsv(environment.getProperty("cpf.log-policy.default.query-allowlist")),
                fallbackCsv("cpf.log-policy.default.header-allowlist", d.headerAllowlist()),
                LogPolicyDecision.parseCsv(environment.getProperty("cpf.log-policy.default.field-allowlist")),
                intProperty("cpf.log-policy.default.max-query-bytes", d.maxQueryBytes()),
                intProperty("cpf.log-policy.default.max-header-bytes", d.maxHeaderBytes()),
                intProperty("cpf.log-policy.default.max-request-body-bytes", d.maxRequestBodyBytes()),
                intProperty("cpf.log-policy.default.max-response-body-bytes", d.maxResponseBodyBytes()),
                intProperty("cpf.log-policy.default.max-stack-bytes", d.maxStackBytes()),
                firstText(environment.getProperty("cpf.log-policy.default.masking-policy-key"), d.maskingPolicyKey(), "DEFAULT"),
                null, "APPLICATION_DEFAULT", null, null);
    }

    private boolean hasApplicationDefault() {
        return DEFAULT_PROPERTY_SUFFIXES.stream()
                .map(DEFAULT_PROPERTY_PREFIX::concat)
                .anyMatch(key -> hasText(environment.getProperty(key)));
    }

    private LogCaptureMode propertyMode(String suffix, LogCaptureMode fallback, CaptureArea area) {
        return LogCaptureMode.parse(environment.getProperty("cpf.log-policy.default." + suffix), fallback, area);
    }
    private LogCaptureMode bodyPropertyMode(String modeSuffix, String legacySuffix, LogCaptureMode fallback) {
        String mode = environment.getProperty("cpf.log-policy.default." + modeSuffix);
        if (hasText(mode)) return LogCaptureMode.parse(mode, fallback, CaptureArea.BODY);
        String legacy = environment.getProperty("cpf.log-policy.default." + legacySuffix);
        if (hasText(legacy)) return yn(legacy, false) ? LogCaptureMode.MASKED_BODY : LogCaptureMode.NONE;
        return fallback;
    }
    private LogCaptureMode stackPropertyMode(LogCaptureMode fallback) {
        String mode = environment.getProperty("cpf.log-policy.default.error-stack-capture-mode");
        if (hasText(mode)) return LogCaptureMode.parse(mode, fallback, CaptureArea.STACK);
        String legacy = environment.getProperty("cpf.log-policy.default.error-stack-save");
        if (hasText(legacy)) return yn(legacy, false) ? LogCaptureMode.FULL_MASKED : LogCaptureMode.NONE;
        return fallback;
    }
    private List<String> fallbackCsv(String key, List<String> fallback) {
        String value=environment.getProperty(key); return hasText(value) ? LogPolicyDecision.parseCsv(value) : fallback;
    }
    private int intProperty(String key,int fallback) {
        Integer value=environment.getProperty(key,Integer.class); return value==null ? fallback : Math.max(0,value);
    }
    private boolean booleanProperty(String key, boolean fallback) {
        String value=environment.getProperty(key); return hasText(value) ? yn(value,fallback) : fallback;
    }
    private LogCaptureMode parse(String value,LogCaptureMode fallback,CaptureArea area) {
        return LogCaptureMode.parse(value,fallback,area);
    }
    private List<String> csv(String value,List<String> fallback) {
        return hasText(value) ? LogPolicyDecision.parseCsv(value) : fallback;
    }
    private boolean yn(String value,boolean fallback) {
        if (!hasText(value)) return fallback;
        return "Y".equalsIgnoreCase(value)||"TRUE".equalsIgnoreCase(value)||"ON".equalsIgnoreCase(value)||"1".equals(value);
    }
    private String firstText(String first,String second,String fallback) {
        if (hasText(first)) return first.trim(); if (hasText(second)) return second.trim(); return fallback;
    }
    private boolean hasText(String value) { return value!=null&&!value.isBlank(); }
    private int first(Integer a,Integer b,int fallback) { return a!=null?a:b!=null?b:fallback; }

    private record CacheKey(LogPolicyTargetType targetType,String targetId) {
        private CacheKey { Objects.requireNonNull(targetType,"targetType"); targetId=LogPolicyDecision.normalizeTargetId(targetId); }
    }
    private record CacheEntry(LogPolicyDecision decision,Instant expiresAt) {}
}
