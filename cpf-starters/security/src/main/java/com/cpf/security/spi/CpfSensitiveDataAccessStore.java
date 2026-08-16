package com.cpf.security.spi;

import com.cpf.security.api.CpfSensitiveDataAccessOperations.AccessGrant;

import java.util.Optional;

/** 원문 조회 승인 상태를 DB, distributed KV 또는 다른 CAS 저장소에 연결하는 SPI입니다. */
public interface CpfSensitiveDataAccessStore {
    CreateResult createIfAbsent(AccessGrant grant);

    Optional<AccessGrant> find(String requestId);

    boolean compareAndSet(String requestId, long expectedVersion, AccessGrant next);

    /**
     * {@code resourceExhausted=true}이면 신규 상태가 저장되지 않았고 기존 상태도 없습니다.
     * 2-argument constructor는 기존 Provider Source 호환을 위해 유지합니다.
     */
    record CreateResult(boolean created, AccessGrant existing, boolean resourceExhausted) {
        public CreateResult(boolean created, AccessGrant existing) {
            this(created, existing, false);
        }

        public CreateResult {
            if (created && (existing != null || resourceExhausted)) {
                throw new IllegalArgumentException("created result cannot contain existing or exhaustion");
            }
            if (resourceExhausted && existing != null) {
                throw new IllegalArgumentException("resource exhausted result cannot contain existing");
            }
        }

        /** exhausted 작업을 CPF 표준 계약에 따라 수행한다. */
        public static CreateResult exhausted() {
            return new CreateResult(false, null, true);
        }
    }
}
