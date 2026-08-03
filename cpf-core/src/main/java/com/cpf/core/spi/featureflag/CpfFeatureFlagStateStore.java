package com.cpf.core.spi.featureflag;

import com.cpf.core.api.featureflag.CpfFeatureFlagResult;
import com.cpf.core.api.featureflag.CpfFeatureFlagValue;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Shared DB state for secure override, expiry, kill switch and multi-instance revision. */
public interface CpfFeatureFlagStateStore {
    Optional<CpfFeatureFlagResult<CpfFeatureFlagValue>> findEffective(String flagKey, Instant now);
    List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(String flagKeyContains, int offset, int limit, Instant now);
    String requestOverride(String flagKey, CpfFeatureFlagValue value, Instant expiresAt, String requesterId, String reason);
    CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(String requestId, String approverId, String reason, Instant now);
    void revokeOverride(String requestId, String operatorId, String reason, Instant now);
    void setKillSwitch(String flagKey, boolean enabled, String operatorId, String reason, Instant now);
    long revision();
}
