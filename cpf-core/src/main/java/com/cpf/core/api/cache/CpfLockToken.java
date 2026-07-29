package com.cpf.core.api.cache;

import java.time.Instant;

/** 소유권 검증과 stale owner 차단을 위한 fencing token입니다. */
public record CpfLockToken(String lockName, String ownerId, long fencingToken,
                           Instant acquiredAt, Instant expiresAt) {
    public CpfLockToken {
        lockName = required(lockName, "lockName");
        ownerId = required(ownerId, "ownerId");
        if (fencingToken <= 0) throw new IllegalArgumentException("fencingToken은 0보다 커야 합니다.");
        if (acquiredAt == null || expiresAt == null || !expiresAt.isAfter(acquiredAt)) {
            throw new IllegalArgumentException("Lock acquiredAt/expiresAt 계약이 올바르지 않습니다.");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + "는 필수입니다.");
        return value.trim();
    }
}
