package com.cpf.core.common.security;

import java.time.Instant;
import java.util.Map;

/**
 * credential 관제 화면/API에서 사용하는 상태 DTO입니다.
 */
public record CpfCredentialStatus(
        String credentialId,
        String scope,
        String status,
        Instant checkedAt,
        Map<String, String> attributes) {

    public CpfCredentialStatus {
        checkedAt = checkedAt == null ? Instant.now() : checkedAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
