package com.cpf.backoffice.online.auth.dto;

import java.time.Instant;

/** 민감한 인증정보를 제외한 MBW 로그인 시도 이력입니다. */
public record BackofficeLoginHistoryResponse(
        long historyId,
        Long operatorId,
        String loginId,
        String successYn,
        String failureReason,
        String clientIp,
        String userAgent,
        String transactionId,
        String systemCode,
        String application,
        String instanceId,
        Instant createdAt) {
}
