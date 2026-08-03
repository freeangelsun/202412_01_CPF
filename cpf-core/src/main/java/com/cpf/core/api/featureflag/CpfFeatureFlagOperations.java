package com.cpf.core.api.featureflag;

import java.time.Instant;
import java.util.List;

/** Evaluation and owner command/query boundary for controlled overrides and kill switches. */
public interface CpfFeatureFlagOperations {
    CpfFeatureFlagResult<CpfFeatureFlagValue> evaluate(
            String flagKey, CpfFeatureFlagValue fallback, CpfFeatureFlagContext context);
    List<CpfFeatureFlagResult<CpfFeatureFlagValue>> search(String flagKeyContains, int page, int size);
    CpfFeatureFlagResult<CpfFeatureFlagValue> find(String flagKey);
    String requestOverride(String flagKey, CpfFeatureFlagValue value, Instant expiresAt, String requesterId, String reason);
    CpfFeatureFlagResult<CpfFeatureFlagValue> approveOverride(String requestId, String approverId, String reason);
    void revokeOverride(String requestId, String operatorId, String reason);
    void setKillSwitch(String flagKey, boolean enabled, String operatorId, String reason);
}
