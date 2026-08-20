package com.cpf.backoffice.online.auth.dto;

import java.time.Instant;

/** refresh token 원문과 hash를 제외한 MBW session 메타데이터입니다. */
public record BackofficeSessionResponse(
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
