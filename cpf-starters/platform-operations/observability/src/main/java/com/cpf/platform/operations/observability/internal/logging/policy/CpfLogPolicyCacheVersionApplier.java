package com.cpf.platform.operations.observability.internal.logging.policy;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import com.cpf.platform.operations.observability.spi.logging.CpfLogPolicyVersionApplier;
import java.time.Instant;
import java.util.Objects;

/** Actual runtime consumer applying ACTIVE managed versions to the policy evaluator cache. */
public final class CpfLogPolicyCacheVersionApplier implements CpfLogPolicyVersionApplier {
    private final LogPolicyCache cache;
    public CpfLogPolicyCacheVersionApplier(LogPolicyCache cache) { this.cache = Objects.requireNonNull(cache, "cache"); }

    @Override public CpfLogPolicyVersionSnapshot baseline(
            LogPolicyTargetType type, String targetId, Instant observedAt) {
        return new CpfLogPolicyVersionSnapshot(type, targetId, 1L,
                CpfLogPolicyVersionSnapshot.Status.ACTIVE, cache.resolve(type, targetId), observedAt,
                "CPF_RUNTIME", "resolved unmanaged baseline");
    }
    @Override public void apply(CpfLogPolicyVersionSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (snapshot.status() == CpfLogPolicyVersionSnapshot.Status.INACTIVE
                || snapshot.status() == CpfLogPolicyVersionSnapshot.Status.FAILED) {
            throw new IllegalArgumentException("inactive or failed log policy cannot be applied");
        }
        CpfLogPolicyVersionSnapshot runtime = snapshot.status() == CpfLogPolicyVersionSnapshot.Status.ACTIVE
                ? snapshot
                : new CpfLogPolicyVersionSnapshot(snapshot.targetType(), snapshot.targetId(), snapshot.version(),
                        CpfLogPolicyVersionSnapshot.Status.ACTIVE, snapshot.decision(), snapshot.updatedAt(),
                        snapshot.updatedBy(), snapshot.reason());
        cache.applyVersionedPolicy(runtime);
    }
}
