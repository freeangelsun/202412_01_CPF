package com.cpf.bizadmin.auth.dto;

import java.time.Instant;

/** refresh token 원문과 hash를 제외한 BZA session 메타데이터입니다. */
public record BzaSessionResponse(
        long sessionId,
        long operatorId,
        String loginId,
        String loginDomain,
        String transactionId,
        String revokedYn,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt) {
}
