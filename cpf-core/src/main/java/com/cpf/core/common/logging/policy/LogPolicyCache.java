package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import org.springframework.core.env.Environment;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.cpf.core.api.logging.policy.LogCaptureMode.CaptureArea;

/** 로그 정책 평가 결과를 짧은 TTL로 보관하는 로컬 캐시입니다. */
public class LogPolicyCache {
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
    private final Map<CacheKey, CacheEntry> cache = new ConcurrentHashMap<>();

    public LogPolicyCache(LogPolicyRepository repository, Environment environment) {
        this.repository = repository;
        this.environment = environment;
        this.ttl = Duration.ofSeconds(Math.max(1,
                environment.getProperty("cpf.log-policy.cache.ttl-seconds", Integer.class, DEFAULT_TTL_SECONDS)));
    }

    public LogPolicyDecision resolve(LogPolicyTargetType targetType, String targetId) {
        CacheKey key = new CacheKey(targetType, LogPolicyDecision.normalizeTargetId(targetId));
        Instant now = Instant.now();
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.expiresAt().isAfter(now)) return cached.decision();
        LogPolicyDecision decision = resolveFresh(targetType, key.targetId());
        cache.put(key, new CacheEntry(decision, now.plus(ttl)));
        return decision;
    }

    public LogPolicyDecision refresh(LogPolicyTargetType targetType, String targetId) {
        CacheKey key = new CacheKey(targetType, LogPolicyDecision.normalizeTargetId(targetId));
        LogPolicyDecision decision = resolveFresh(targetType, key.targetId());
        cache.put(key, new CacheEntry(decision, Instant.now().plus(ttl)));
        return decision;
    }

    public void evict(LogPolicyTargetType targetType, String targetId) { cache.remove(new CacheKey(targetType, targetId)); }
    public void clear() { cache.clear(); }
    int size() { return cache.size(); }

    private LogPolicyDecision resolveFresh(LogPolicyTargetType type, String targetId) {
        LogPolicyDecision base = repository.findActivePolicy(type, targetId)
                .map(row -> fromRow(type, targetId, row, null))
                .orElseGet(() -> applicationDefault(type, targetId));
        return repository.findActiveOverride(type, targetId, LocalDateTime.now())
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
