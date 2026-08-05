package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.core.env.Environment;

/** Verifies cache TTL and override evaluation against the injected Clock. */
public final class LogPolicyCacheClockHarness {
    private LogPolicyCacheClockHarness() {}

    public static void main(String[] args) {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        AtomicInteger policyQueries = new AtomicInteger();
        AtomicInteger overrideQueries = new AtomicInteger();
        AtomicReference<LocalDateTime> overrideNow = new AtomicReference<>();
        LogPolicyRepository repository = new LogPolicyRepository() {
            @Override
            public Optional<LogPolicyRow> findActiveOverride(
                    LogPolicyTargetType targetType, String targetId, LocalDateTime now) {
                overrideQueries.incrementAndGet();
                overrideNow.set(now);
                return Optional.empty();
            }

            @Override
            public Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType targetType, String targetId) {
                policyQueries.incrementAndGet();
                return Optional.empty();
            }
        };
        Environment environment = new Environment() {
            @Override public String getProperty(String key) { return null; }
            @SuppressWarnings("unchecked")
            @Override public <T> T getProperty(String key, Class<T> type) {
                if ("cpf.log-policy.cache.ttl-seconds".equals(key) && type == Integer.class) {
                    return (T) Integer.valueOf(5);
                }
                if ("cpf.log-policy.cache.max-entries".equals(key) && type == Integer.class) {
                    return (T) Integer.valueOf(2);
                }
                return null;
            }
            @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
                T value = getProperty(key, type);
                return value == null ? defaultValue : value;
            }
        };

        LogPolicyCache cache = new LogPolicyCache(repository, environment, clock);
        LogPolicyDecision first = cache.resolve(LogPolicyTargetType.MODULE, " core ");
        LogPolicyDecision second = cache.resolve(LogPolicyTargetType.MODULE, "core");
        if (first != second || policyQueries.get() != 1 || overrideQueries.get() != 1) {
            throw new AssertionError("cache did not reuse the same normalized target");
        }
        if (!overrideNow.get().equals(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC))) {
            throw new AssertionError("override lookup did not use injected Clock");
        }

        clock.advance(Duration.ofSeconds(4));
        cache.resolve(LogPolicyTargetType.MODULE, "core");
        if (policyQueries.get() != 1) throw new AssertionError("cache expired before TTL");
        clock.advance(Duration.ofSeconds(2));
        cache.resolve(LogPolicyTargetType.MODULE, "core");
        if (policyQueries.get() != 2 || overrideQueries.get() != 2) {
            throw new AssertionError("cache did not refresh after injected-clock TTL");
        }
        if (cache.size() != 1) throw new AssertionError("unexpected cache size");

        cache.resolve(LogPolicyTargetType.MODULE, "second");
        cache.resolve(LogPolicyTargetType.MODULE, "third");
        if (cache.size() != 2) throw new AssertionError("cache capacity was not enforced");
        cache.evict(LogPolicyTargetType.MODULE, " third ");
        if (cache.size() != 1) throw new AssertionError("normalized eviction failed");
        com.cpf.core.api.logging.CpfLogPolicyCacheRuntimeStatus.RuntimeSnapshot runtime =
                cache.logPolicyCacheRuntimeSnapshot();
        if (runtime.maximumEntries() != 2 || runtime.hitCount() < 2L
                || runtime.missCount() < 4L || runtime.evictionCount() < 2L) {
            throw new AssertionError("cache runtime counters are incomplete: " + runtime);
        }
        try {
            new LogPolicyCache(repository, invalidEnvironment(0, 2), clock);
            throw new AssertionError("invalid TTL must fail startup");
        } catch (IllegalArgumentException expected) { }
        try {
            new LogPolicyCache(repository, invalidEnvironment(5, 0), clock);
            throw new AssertionError("invalid capacity must fail startup");
        } catch (IllegalArgumentException expected) { }
        System.out.println("CPF_LOG_POLICY_CLOCK_HARNESS_PASS");
    }


    private static Environment invalidEnvironment(int ttlSeconds, int maximumEntries) {
        return new Environment() {
            @Override public String getProperty(String key) { return null; }
            @SuppressWarnings("unchecked")
            @Override public <T> T getProperty(String key, Class<T> type) {
                if ("cpf.log-policy.cache.ttl-seconds".equals(key) && type == Integer.class) {
                    return (T) Integer.valueOf(ttlSeconds);
                }
                if ("cpf.log-policy.cache.max-entries".equals(key) && type == Integer.class) {
                    return (T) Integer.valueOf(maximumEntries);
                }
                return null;
            }
            @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
                T value = getProperty(key, type);
                return value == null ? defaultValue : value;
            }
        };
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;
        private MutableClock(Instant instant) { this.instant = new AtomicReference<>(instant); }
        void advance(Duration duration) { instant.updateAndGet(value -> value.plus(duration)); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant.get(); }
    }
}
