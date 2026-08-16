package com.cpf.platform.operations.spi.featureflag;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagResult;
import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagValue;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Shared DB state for secure override, expiry, kill switch and multi-instance revision. */
/** CpfFeatureFlagStateStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfFeatureFlagStateStore {
    Optional<CpfFeatureFlagResult<CpfFeatureFlagValue>> findEffective(String flagKey, Instant now);
    List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(String flagKeyContains, int offset, int limit, Instant now);
    String requestOverride(String flagKey, CpfFeatureFlagValue value, Instant expiresAt, String requesterId, String reason);
    CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(String requestId, String approverId, String reason, Instant now);
    void revokeOverride(String requestId, String operatorId, String reason, Instant now);
    void setKillSwitch(String flagKey, boolean enabled, String operatorId, String reason, Instant now);
    long revision();
}
