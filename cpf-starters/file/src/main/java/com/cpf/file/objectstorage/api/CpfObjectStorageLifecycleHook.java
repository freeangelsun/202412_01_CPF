package com.cpf.file.objectstorage.api;

import java.time.Instant;

/**
 * Object delete/retention lifecycle hook. Implementations must fail closed when retention or legal-hold
 * policy forbids deletion. The hook is provider-neutral and receives metadata only, never credentials.
 */
@FunctionalInterface
/** CpfObjectStorageLifecycleHook 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfObjectStorageLifecycleHook {
    void beforeDelete(CpfObjectStorageMetadata metadata, Instant now);

    /** Default CPF retention policy: metadata legal-hold and retain-until are enforced. */
    CpfObjectStorageLifecycleHook METADATA_RETENTION = (metadata, now) -> {
        String legalHold = metadata.metadata().get("cpf-legal-hold");
        if ("true".equalsIgnoreCase(legalHold)) {
            throw new IllegalStateException("object is under legal hold");
        }
        String retainUntil = metadata.metadata().get("cpf-retain-until");
        if (retainUntil != null && !retainUntil.isBlank()) {
            Instant deadline;
            try { deadline = Instant.parse(retainUntil.trim()); }
            // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
            catch (RuntimeException invalid) { throw new IllegalStateException("invalid cpf-retain-until metadata", invalid); }
            if (now.isBefore(deadline)) throw new IllegalStateException("object retention period has not expired");
        }
    };
}
