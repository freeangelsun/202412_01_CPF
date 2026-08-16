package com.cpf.file.objectstorage.api;

import java.time.Instant;

/**
 * Object Storage 삭제 전에 보존기간과 Legal Hold 정책을 검사하는 Provider 중립 Lifecycle Hook입니다.
 * 정책상 삭제할 수 없으면 fail-closed로 거부하며 구현에는 자격증명이 아니라 메타데이터만 전달합니다.
 */
@FunctionalInterface
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
