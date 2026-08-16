package com.cpf.security.api;

import java.util.List;

/** Public masking-policy control plane; adapters must enforce caller authentication and authorization. */
/** CpfMaskingPolicyOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfMaskingPolicyOperations {
    CpfMaskingPolicySnapshot current();
    List<CpfMaskingPolicySnapshot> history(int limit);
    CpfMaskingPolicyResult update(CpfMaskingPolicyUpdateCommand command);
    CpfMaskingPolicyResult rollback(CpfMaskingPolicyRollbackCommand command);
    CpfMaskingPolicyRuntimeStatus runtimeStatus();
}
