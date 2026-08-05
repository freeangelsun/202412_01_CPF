package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.CpfLogPolicyCacheRuntimeStatus;
import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.core.env.Environment;

/** Executable contract harness for the actual versioned-policy runtime consumer. */
public final class CpfLogPolicyVersionConsumerHarness {
    private CpfLogPolicyVersionConsumerHarness() { }

    public static void main(String[] args) {
        AtomicInteger repositoryReads = new AtomicInteger();
        LogPolicyRepository repository = new LogPolicyRepository() {
            @Override public Optional<LogPolicyRow> findActiveOverride(
                    LogPolicyTargetType type, String id, LocalDateTime now) {
                repositoryReads.incrementAndGet();
                return Optional.empty();
            }
            @Override public Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType type, String id) {
                repositoryReads.incrementAndGet();
                return Optional.empty();
            }
        };
        Environment environment = new MapEnvironment(Map.of(
                "cpf.log-policy.cache.ttl-seconds", 30,
                "cpf.log-policy.cache.max-entries", 2));
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T06:00:00Z"), ZoneOffset.UTC);
        LogPolicyCache cache = new LogPolicyCache(repository, environment, clock);
        CpfLogPolicyCacheVersionApplier applier = new CpfLogPolicyCacheVersionApplier(cache);

        LogPolicyDecision unmanaged = cache.resolve(LogPolicyTargetType.MODULE, "EPHEMERAL");
        require("CPF_DEFAULT".equals(unmanaged.resolvedSource()), "unmanaged baseline must resolve");
        require(repositoryReads.get() == 2, "repository path must be consumed before managed policy");

        CpfLogPolicyVersionSnapshot wildcard = active(LogPolicyTargetType.MODULE, "*", 2L,
                decision(LogPolicyTargetType.MODULE, "*", "WARN", "MANAGED_WILDCARD"), clock.instant());
        applier.apply(wildcard);
        LogPolicyDecision wildcardResolved = cache.resolve(LogPolicyTargetType.MODULE, "PAY");
        require("WARN".equals(wildcardResolved.fileLogLevel()), "wildcard managed policy must apply");
        require(repositoryReads.get() == 2, "managed wildcard must bypass repository");

        CpfLogPolicyVersionSnapshot exact = active(LogPolicyTargetType.MODULE, "PAY", 3L,
                decision(LogPolicyTargetType.MODULE, "PAY", "ERROR", "MANAGED_EXACT"), clock.instant());
        applier.apply(exact);
        require("ERROR".equals(cache.resolve(LogPolicyTargetType.MODULE, "PAY").fileLogLevel()),
                "exact managed policy must override wildcard");
        require("WARN".equals(cache.resolve(LogPolicyTargetType.MODULE, "ACCOUNT").fileLogLevel()),
                "wildcard must remain active for other targets");
        require("ERROR".equals(cache.refresh(LogPolicyTargetType.MODULE, "PAY").fileLogLevel()),
                "refresh must not bypass managed policy");
        require(repositoryReads.get() == 2, "refresh of managed policy must not query repository");

        CpfLogPolicyVersionSnapshot newer = active(LogPolicyTargetType.MODULE, "PAY", 4L,
                decision(LogPolicyTargetType.MODULE, "PAY", "DEBUG", "MANAGED_V4"), clock.instant());
        applier.apply(newer);
        require("DEBUG".equals(cache.resolve(LogPolicyTargetType.MODULE, "PAY").fileLogLevel()),
                "newer version must replace deterministically");
        applier.apply(newer); // idempotent same-version/same-checksum replay
        expectFailure(() -> applier.apply(exact), "stale version must be rejected");
        expectFailure(() -> applier.apply(active(LogPolicyTargetType.MODULE, "PAY", 4L,
                decision(LogPolicyTargetType.MODULE, "PAY", "TRACE", "CONFLICT"), clock.instant())),
                "same version with different checksum must be rejected");
        applier.apply(new CpfLogPolicyVersionSnapshot(
                LogPolicyTargetType.MODULE, "PAY", 5L, CpfLogPolicyVersionSnapshot.Status.DRAFT,
                decision(LogPolicyTargetType.MODULE, "PAY", "INFO", "DRAFT"), clock.instant(),
                "operator", "draft"));
        require("INFO".equals(cache.resolve(LogPolicyTargetType.MODULE, "PAY").fileLogLevel()),
                "staged DRAFT must be applied as the effective runtime ACTIVE decision");
        require(cache.versionedPolicy(LogPolicyTargetType.MODULE, "PAY").status()
                        == CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "runtime cache must never expose DRAFT as active evaluation state");
        expectFailure(() -> applier.apply(new CpfLogPolicyVersionSnapshot(
                LogPolicyTargetType.MODULE, "PAY", 6L, CpfLogPolicyVersionSnapshot.Status.FAILED,
                decision(LogPolicyTargetType.MODULE, "PAY", "TRACE", "FAILED"), clock.instant(),
                "operator", "failed")), "failed snapshot must be rejected");

        // Capacity 2 is occupied by wildcard + exact. Managed entries may not be silently evicted.
        expectFailure(() -> applier.apply(active(LogPolicyTargetType.BATCH_JOB, "JOB-A", 2L,
                decision(LogPolicyTargetType.BATCH_JOB, "JOB-A", "INFO", "OVER_CAPACITY"), clock.instant())),
                "managed-only capacity exhaustion must fail closed");
        CpfLogPolicyCacheRuntimeStatus.RuntimeSnapshot status = cache.logPolicyCacheRuntimeSnapshot();
        require(status.entryCount() == 2 && status.entryCount() <= status.maximumEntries(),
                "runtime capacity metrics must remain bounded");
        require(cache.versionedPolicy(LogPolicyTargetType.MODULE, "PAY").version() == 5L,
                "current managed version must be observable without policy content leakage");

        System.out.println("CPF_LOG_POLICY_VERSION_CONSUMER_HARNESS_PASS");
    }

    private static CpfLogPolicyVersionSnapshot active(LogPolicyTargetType type, String id, long version,
            LogPolicyDecision decision, Instant now) {
        return new CpfLogPolicyVersionSnapshot(type, id, version,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE, decision, now, "operator", "approved change");
    }

    private static LogPolicyDecision decision(LogPolicyTargetType type, String id, String level, String source) {
        LogPolicyDecision base = LogPolicyDecision.cpfDefault(type, id);
        return new LogPolicyDecision(base.schemaVersion(), base.targetType(), base.targetId(), level,
                base.dbLogEnabled(), level, base.queryCaptureMode(), base.requestHeaderCaptureMode(),
                base.responseHeaderCaptureMode(), base.requestBodyCaptureMode(), base.responseBodyCaptureMode(),
                base.errorStackCaptureMode(), base.queryAllowlist(), base.headerAllowlist(), base.fieldAllowlist(),
                base.maxQueryBytes(), base.maxHeaderBytes(), base.maxRequestBodyBytes(),
                base.maxResponseBodyBytes(), base.maxStackBytes(), base.maskingPolicyKey(), null,
                source, null, null);
    }

    private static void expectFailure(Runnable action, String message) {
        boolean failed = false;
        try { action.run(); } catch (IllegalArgumentException | IllegalStateException expected) { failed = true; }
        require(failed, message);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class MapEnvironment implements Environment {
        private final Map<String, Object> values;
        private MapEnvironment(Map<String, Object> values) { this.values = Map.copyOf(values); }
        @Override public String getProperty(String key) {
            Object value = values.get(key);
            return value == null ? null : String.valueOf(value);
        }
        @Override public <T> T getProperty(String key, Class<T> type) {
            return getProperty(key, type, null);
        }
        @Override public <T> T getProperty(String key, Class<T> type, T defaultValue) {
            Object value = values.get(key);
            if (value == null) return defaultValue;
            return type.cast(value);
        }
    }
}
