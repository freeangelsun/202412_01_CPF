package cpf.cmn.sec.token;

import cpf.cmn.utils.TextUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Service
public class CmnOAuthBearerTokenService extends cpf.cmn.common.base.CmnBaseService {
    private final CmnJwtService jwtService;

    public CmnOAuthBearerTokenService(CmnJwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public String extractBearerToken(String authorizationHeader) {
        if (!TextUtils.hasText(authorizationHeader)) {
            return "";
        }
        String prefix = "Bearer ";
        if (!authorizationHeader.regionMatches(true, 0, prefix, 0, prefix.length())) {
            return "";
        }
        return authorizationHeader.substring(prefix.length()).trim();
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public CmnOAuthTokenIntrospectionResult introspectJwtBearer(
            String authorizationHeader,
            String secret,
            String expectedIssuer,
            String expectedAudience) {
        String token = extractBearerToken(authorizationHeader);
        if (!TextUtils.hasText(token)) {
            return new CmnOAuthTokenIntrospectionResult(false, "Bearer", null, null, null, null, Map.of(),
                    "CPF 처리 기준입니다.");
        }
        CmnJwtValidationResult validation = jwtService.validateHs256Token(token, secret, expectedIssuer, expectedAudience);
        return new CmnOAuthTokenIntrospectionResult(
                validation.valid(),
                "Bearer",
                validation.subject(),
                validation.issuer(),
                validation.audience(),
                validation.expiresAt(),
                validation.claims(),
                validation.reason());
    }
}

