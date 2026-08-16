package com.cpf.platform.operations.observability.spi.logging;

import com.cpf.platform.operations.observability.api.logging.policy.CpfLogPolicyVersionSnapshot;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyTargetType;
import java.time.Instant;

/** Runtime consumer bridge for baseline resolution and committed policy application. */
/** CpfLogPolicyVersionApplier 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfLogPolicyVersionApplier {
    CpfLogPolicyVersionSnapshot baseline(LogPolicyTargetType targetType, String targetId, Instant observedAt);
    void apply(CpfLogPolicyVersionSnapshot snapshot);
}
