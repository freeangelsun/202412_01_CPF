package com.cpf.common.sec.token;

import java.util.Map;

/**
 * HS256 JWT 생성 요청 값입니다.
 *
 * @param issuer 토큰 발급자
 * @param subject 토큰 주체
 * @param audience 토큰 대상
 * @param ttlSeconds 토큰 만료 초
 * @param secret HS256 서명 secret
 * @param claims 추가 claim
 */
public record CmnJwtCreateRequest(
        String issuer,
        String subject,
        String audience,
        long ttlSeconds,
        String secret,
        Map<String, Object> claims) {
}
