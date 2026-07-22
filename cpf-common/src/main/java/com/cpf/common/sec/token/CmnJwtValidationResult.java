package cpf.cmn.sec.token;

import java.time.Instant;
import java.util.Map;

/**
 * JWT 검증 결과입니다.
 *
 * @param valid 검증 성공 여부
 * @param reason 검증 실패 또는 성공 사유
 * @param subject 토큰 주체
 * @param issuer 토큰 발급자
 * @param audience 토큰 대상
 * @param expiresAt 토큰 만료 시각
 * @param claims 토큰 claim
 */
public record CmnJwtValidationResult(
        boolean valid,
        String reason,
        String subject,
        String issuer,
        String audience,
        Instant expiresAt,
        Map<String, Object> claims) {
}
