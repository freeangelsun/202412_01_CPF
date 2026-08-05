package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.core.api.logging.policy.LogCaptureMode;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.core.env.Environment;

public final class CpfLogPolicyCacheVersionApplierHarness {
    private CpfLogPolicyCacheVersionApplierHarness() { }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        LogPolicyRepository repository = new LogPolicyRepository() {
            @Override public Optional<LogPolicyRow> findActiveOverride(
                    LogPolicyTargetType type, String targetId, LocalDateTime now) {
                return Optional.empty();
            }
            @Override public Optional<LogPolicyRow> findActivePolicy(
                    LogPolicyTargetType type, String targetId) {
                return Optional.empty();
            }
        };
        Environment environment = new Environment() {
            @Override public String getProperty(String key) { return null; }
            @SuppressWarnings("unchecked")
            @Override public <T> T getProperty(String key, Class<T> type) {
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
        CpfLogPolicyCacheVersionApplier applier = new CpfLogPolicyCacheVersionApplier(cache);
        CpfLogPolicyVersionSnapshot baseline = applier.baseline(
                LogPolicyTargetType.MODULE, "PAY", clock.instant());
        require(baseline.version() == 1L && "INFO".equals(baseline.decision().fileLogLevel()),
                "baseline must reflect the actual evaluator");

        CpfLogPolicyVersionSnapshot draft = new CpfLogPolicyVersionSnapshot(
                LogPolicyTargetType.MODULE, "PAY", 2L, CpfLogPolicyVersionSnapshot.Status.DRAFT,
                decision("DEBUG"), clock.instant(), "operator-a", "draft");
        applier.apply(draft);
        require("DEBUG".equals(cache.resolve(LogPolicyTargetType.MODULE, "PAY").fileLogLevel()),
                "approved committed DRAFT must be activated by the manager-owned runtime consumer");
        require(cache.versionedPolicy(LogPolicyTargetType.MODULE, "PAY").status()
                        == CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                "runtime cache must never expose DRAFT status");

        CpfLogPolicyVersionSnapshot active = new CpfLogPolicyVersionSnapshot(
                LogPolicyTargetType.MODULE, "PAY", 2L, CpfLogPolicyVersionSnapshot.Status.ACTIVE,
                decision("DEBUG"), clock.instant(), "operator-a", "approved");
        applier.apply(active);
        require("DEBUG".equals(cache.resolve(LogPolicyTargetType.MODULE, "PAY").fileLogLevel()),
                "ACTIVE managed version must override the evaluator immediately");
        require(cache.versionedPolicy(LogPolicyTargetType.MODULE, "PAY").version() == 2L,
                "managed version must be queryable for reconciliation");

        applier.apply(new CpfLogPolicyVersionSnapshot(LogPolicyTargetType.MODULE, "ACC", 2L,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE, decision("WARN", "ACC"),
                clock.instant(), "operator-a", "approved"));
        try {
            applier.apply(new CpfLogPolicyVersionSnapshot(LogPolicyTargetType.MODULE, "MBR", 2L,
                    CpfLogPolicyVersionSnapshot.Status.ACTIVE, decision("ERROR", "MBR"),
                    clock.instant(), "operator-a", "approved"));
            throw new AssertionError("managed policy capacity must fail closed");
        } catch (IllegalStateException expected) { }
        System.out.println("CPF_LOG_POLICY_CACHE_VERSION_APPLIER_HARNESS_PASS");
    }

    private static LogPolicyDecision decision(String level) { return decision(level, "PAY"); }
    private static LogPolicyDecision decision(String level, String targetId) {
        return new LogPolicyDecision(LogPolicyDecision.CURRENT_SCHEMA_VERSION,
                LogPolicyTargetType.MODULE.code(), targetId, level, true, level,
                LogCaptureMode.NONE, LogCaptureMode.ALLOWLIST, LogCaptureMode.ALLOWLIST,
                LogCaptureMode.NONE, LogCaptureMode.NONE, LogCaptureMode.SUMMARY,
                List.of(), List.of("content-type"), List.of(), 1024, 2048,
                4096, 4096, 8192, "DEFAULT", null, "HARNESS", null, null);
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
