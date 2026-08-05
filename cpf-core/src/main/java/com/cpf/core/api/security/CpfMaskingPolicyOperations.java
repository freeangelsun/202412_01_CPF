package com.cpf.core.api.security;

import java.util.List;

/** Public masking-policy control plane; adapters must enforce caller authentication and authorization. */
public interface CpfMaskingPolicyOperations {
    CpfMaskingPolicySnapshot current();
    List<CpfMaskingPolicySnapshot> history(int limit);
    CpfMaskingPolicyResult update(CpfMaskingPolicyUpdateCommand command);
    CpfMaskingPolicyResult rollback(CpfMaskingPolicyRollbackCommand command);
    CpfMaskingPolicyRuntimeStatus runtimeStatus();
}
