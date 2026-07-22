package com.cpf.core.common.logging.policy;

import org.springframework.core.env.Environment;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 로그 정책 평가 결과를 짧은 TTL로 보관하는 로컬 캐시입니다.
 *
 * <p>ADM에서 정책을 변경하면 해당 key를 즉시 evict/refresh할 수 있고, 별도 전파가
 * 없는 인스턴스도 TTL 이후에는 DB 정책을 다시 읽습니다.</p>
 */
public class LogPolicyCache {

    private static final int DEFAULT_TTL_SECONDS = 30;

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
        if (cached != null && cached.expiresAt().isAfter(now)) {
            return cached.decision();
        }
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

    public void evict(LogPolicyTargetType targetType, String targetId) {
        cache.remove(new CacheKey(targetType, LogPolicyDecision.normalizeTargetId(targetId)));
    }

    public void clear() {
        cache.clear();
    }

    int size() {
        return cache.size();
    }

    private LogPolicyDecision resolveFresh(LogPolicyTargetType targetType, String targetId) {
        LogPolicyDecision base = repository
                .findActivePolicy(targetType, targetId)
                .map(row -> fromRow(targetType, targetId, row, null))
                .orElseGet(() -> applicationDefault(targetType, targetId));
        return repository
                .findActiveOverride(targetType, targetId, LocalDateTime.now())
                .map(row -> fromRow(targetType, targetId, row, base))
                .orElse(base);
    }

    private LogPolicyDecision fromRow(
            LogPolicyTargetType targetType,
            String requestedTargetId,
            LogPolicyRow row,
            LogPolicyDecision base) {
        String level = firstText(row.logLevel(), base != null ? base.fileLogLevel() : null, "INFO");
        boolean fileEnabled = yn(row.fileLogEnabledYn(), base == null || !"OFF".equals(base.fileLogLevel()));
        String fileLevel = fileEnabled ? LogPolicyDecision.normalizeLevel(level, "INFO") : "OFF";
        return new LogPolicyDecision(
                targetType.code(),
                LogPolicyDecision.normalizeTargetId(requestedTargetId),
                fileLevel,
                yn(row.dbLogEnabledYn(), base == null || base.dbLogEnabled()),
                LogPolicyDecision.normalizeLevel(level, base != null ? base.dbLogLevel() : "INFO"),
                yn(row.requestBodyLogYn(), base != null && base.requestBodySave()),
                yn(row.responseBodyLogYn(), base != null && base.responseBodySave()),
                yn(row.errorStackLogYn(), base == null || base.errorStackSave()),
                firstText(row.maskingPolicyKey(), base != null ? base.maskingPolicyKey() : null, "DEFAULT"),
                row.source(),
                row.overrideId(),
                firstNonNull(row.policyId(), base != null ? base.policyId() : null));
    }

    private LogPolicyDecision applicationDefault(LogPolicyTargetType targetType, String targetId) {
        LogPolicyDecision cpfDefault = LogPolicyDecision.cpfDefault(targetType, targetId);
        boolean hasApplicationDefault = hasText(environment.getProperty("cpf.log-policy.default.file-log-level"))
                || hasText(environment.getProperty("cpf.log-policy.default.db-log-enabled"))
                || hasText(environment.getProperty("cpf.log-policy.default.request-body-save"))
                || hasText(environment.getProperty("cpf.log-policy.default.response-body-save"))
                || hasText(environment.getProperty("cpf.log-policy.default.error-stack-save"))
                || hasText(environment.getProperty("cpf.log-policy.default.masking-policy-key"));
        if (!hasApplicationDefault) {
            return cpfDefault;
        }
        String level = LogPolicyDecision.normalizeLevel(
                environment.getProperty("cpf.log-policy.default.file-log-level"),
                cpfDefault.fileLogLevel());
        return new LogPolicyDecision(
                targetType.code(),
                LogPolicyDecision.normalizeTargetId(targetId),
                level,
                booleanProperty("cpf.log-policy.default.db-log-enabled", cpfDefault.dbLogEnabled()),
                level,
                booleanProperty("cpf.log-policy.default.request-body-save", cpfDefault.requestBodySave()),
                booleanProperty("cpf.log-policy.default.response-body-save", cpfDefault.responseBodySave()),
                booleanProperty("cpf.log-policy.default.error-stack-save", cpfDefault.errorStackSave()),
                firstText(environment.getProperty("cpf.log-policy.default.masking-policy-key"), cpfDefault.maskingPolicyKey(), "DEFAULT"),
                "APPLICATION_DEFAULT",
                null,
                null);
    }

    private boolean booleanProperty(String key, boolean fallback) {
        String value = environment.getProperty(key);
        if (!hasText(value)) {
            return fallback;
        }
        return yn(value, fallback);
    }

    private boolean yn(String value, boolean fallback) {
        if (!hasText(value)) {
            return fallback;
        }
        return "Y".equalsIgnoreCase(value)
                || "TRUE".equalsIgnoreCase(value)
                || "ON".equalsIgnoreCase(value)
                || "1".equals(value);
    }

    private String firstText(String first, String second, String fallback) {
        if (hasText(first)) {
            return first.trim();
        }
        if (hasText(second)) {
            return second.trim();
        }
        return fallback;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private Long firstNonNull(Long first, Long second) {
        return first != null ? first : second;
    }

    private record CacheKey(LogPolicyTargetType targetType, String targetId) {
        private CacheKey {
            Objects.requireNonNull(targetType, "targetType");
            targetId = LogPolicyDecision.normalizeTargetId(targetId);
        }
    }

    private record CacheEntry(LogPolicyDecision decision, Instant expiresAt) {
    }
}
