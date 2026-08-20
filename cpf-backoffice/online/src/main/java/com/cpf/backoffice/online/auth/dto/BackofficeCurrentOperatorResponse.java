package com.cpf.backoffice.online.auth.dto;

import java.time.Instant;

/** 현재 MBW access token과 DB 계정 상태를 함께 반환하는 응답입니다. */
public record BackofficeCurrentOperatorResponse(
        BackofficeOperatorResponse operator,
        String loginDomain,
        Instant tokenExpiresAt) {
}
