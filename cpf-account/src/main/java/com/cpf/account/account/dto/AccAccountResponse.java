package com.cpf.account.account.dto;

import java.time.LocalDateTime;

/** 외부 응답에서 이메일을 마스킹한 ACC 계정 정보입니다. */
public record AccAccountResponse(
        long accountId,
        String accountNo,
        String accountName,
        String maskedEmail,
        String statusCode,
        long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
