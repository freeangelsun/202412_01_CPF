package com.cpf.bizadmin.auth.dto;

import java.time.Instant;

/** 민감한 인증정보를 제외한 BZA 로그인 시도 이력입니다. */
public record BzaLoginHistoryResponse(
        long historyId,
        Long operatorId,
        String loginId,
        String successYn,
        String failureReason,
        String clientIp,
        String userAgent,
        String transactionId,
        String moduleId,
        String wasId,
        String serverInstanceId,
        Instant createdAt) {
}
