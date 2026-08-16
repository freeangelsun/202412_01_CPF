package com.cpf.bizadmin.auth.dto;

import java.time.Instant;

/** 현재 BZA access token과 DB 계정 상태를 함께 반환하는 응답입니다. */
public record BzaCurrentOperatorResponse(
        BzaOperatorResponse operator,
        String loginDomain,
        Instant tokenExpiresAt) {
}
