package com.cpf.common.sec.token;

import java.time.Instant;
import java.util.Map;

/** Bearer 토큰 검증 결과와 표준 클레임을 외부 호출자에게 제공합니다. */
public record CmnOAuthTokenIntrospectionResult(
        boolean active,
        String tokenType,
        String subject,
        String issuer,
        String audience,
        Instant expiresAt,
        Map<String, Object> claims,
        String reason) {
}

