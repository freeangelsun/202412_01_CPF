package cpf.cmn.sec.token;

import java.time.Instant;
import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
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

