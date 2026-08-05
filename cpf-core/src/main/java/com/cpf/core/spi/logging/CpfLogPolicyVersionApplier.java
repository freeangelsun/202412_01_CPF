package com.cpf.core.spi.logging;

import com.cpf.core.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import java.time.Instant;

/** Runtime consumer bridge for baseline resolution and committed policy application. */
public interface CpfLogPolicyVersionApplier {
    CpfLogPolicyVersionSnapshot baseline(LogPolicyTargetType targetType, String targetId, Instant observedAt);
    void apply(CpfLogPolicyVersionSnapshot snapshot);
}
