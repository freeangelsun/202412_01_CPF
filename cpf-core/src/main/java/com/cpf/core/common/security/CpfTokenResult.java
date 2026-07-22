package com.cpf.core.common.security;

import java.time.Instant;

/**
 * 원문 token을 로그에 노출하지 않기 위한 token 결과 DTO입니다.
 */
public record CpfTokenResult(
        String tokenRef,
        String tokenType,
        Instant expiresAt,
        String maskedToken) {

    public CpfTokenResult {
        tokenType = tokenType == null || tokenType.isBlank() ? "Bearer" : tokenType;
        maskedToken = mask(maskedToken);
    }

    private static String mask(String value) {
        if (value == null || value.isBlank()) {
            return "***";
        }
        if (value.startsWith("***")) {
            return value;
        }
        return "***" + value.substring(Math.max(0, value.length() - Math.min(4, value.length())));
    }
}
