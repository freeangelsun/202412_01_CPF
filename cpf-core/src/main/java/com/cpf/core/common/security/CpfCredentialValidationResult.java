package com.cpf.core.common.security;

import java.time.Instant;

/**
 * credential 참조값 검증 결과입니다.
 */
public record CpfCredentialValidationResult(
        boolean valid,
        String credentialId,
        String status,
        Instant checkedAt,
        String detail) {

    public CpfCredentialValidationResult {
        checkedAt = checkedAt == null ? Instant.now() : checkedAt;
    }
}
