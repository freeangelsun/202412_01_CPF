package com.cpf.core.common.logging.policy;

import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.core.env.Environment;

/** Test-only factory kept in the repository package because LogPolicyRow is internal. */
public final class CpfLogPolicyVersionCacheTestFactory {
    private CpfLogPolicyVersionCacheTestFactory() { }
    public static LogPolicyCache create(Environment environment, Clock clock) {
        LogPolicyRepository repository = new LogPolicyRepository() {
            @Override public Optional<LogPolicyRow> findActiveOverride(
                    LogPolicyTargetType type, String id, LocalDateTime now) { return Optional.empty(); }
            @Override public Optional<LogPolicyRow> findActivePolicy(
                    LogPolicyTargetType type, String id) { return Optional.empty(); }
        };
        return new LogPolicyCache(repository, environment, clock);
    }
}
