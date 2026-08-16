package com.cpf.security.spi;

import com.cpf.security.api.CpfMaskingPolicyRuntimeStatus;
import com.cpf.security.api.CpfMaskingPolicySnapshot;
import java.util.List;
import java.util.Optional;

/** Durable optimistic store. Implementations must atomically deduplicate commandId+commandHash. */
/** CpfMaskingPolicyStore 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfMaskingPolicyStore {
    Optional<CpfMaskingPolicySnapshot> current();
    Optional<CpfMaskingPolicySnapshot> findVersion(long version);
    List<CpfMaskingPolicySnapshot> history(int limit);
    WriteResult compareAndSet(
            long expectedVersion,
            String commandId,
            String commandHash,
            CpfMaskingPolicySnapshot next);
    CpfMaskingPolicyRuntimeStatus runtimeStatus();

    /** WriteResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    record WriteResult(Status status, CpfMaskingPolicySnapshot snapshot) {
        public WriteResult {
            if (status == null) throw new IllegalArgumentException("status is required");
        }
    }

    enum Status {
        APPLIED,
        IDEMPOTENT_REPLAY,
        VERSION_CONFLICT,
        COMMAND_CONFLICT,
        RESOURCE_EXHAUSTED,
        UNKNOWN
    }
}
