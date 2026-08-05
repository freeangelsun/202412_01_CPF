package com.cpf.core.spi.security;

import com.cpf.core.api.security.CpfMaskingPolicyRuntimeStatus;
import com.cpf.core.api.security.CpfMaskingPolicySnapshot;
import java.util.List;
import java.util.Optional;

/** Durable optimistic store. Implementations must atomically deduplicate commandId+commandHash. */
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
