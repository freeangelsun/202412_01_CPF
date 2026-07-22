package com.cpf.common.sec.token;

import com.cpf.common.utils.TextUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/** Authorization 헤더의 Bearer JWT를 추출하고 표준 검증 결과로 변환합니다. */
@Service
public class CmnOAuthBearerTokenService extends com.cpf.common.common.base.CmnBaseService {
    private final CmnJwtService jwtService;

    public CmnOAuthBearerTokenService(CmnJwtService jwtService) {
        this.jwtService = jwtService;
    }

    /** 대소문자를 구분하지 않고 Bearer scheme의 토큰 값만 추출합니다. */
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

    /** HS256 JWT의 서명·issuer·audience·만료를 검증하고 클레임을 반환합니다. */
    public CmnOAuthTokenIntrospectionResult introspectJwtBearer(
            String authorizationHeader,
            String secret,
            String expectedIssuer,
            String expectedAudience) {
        String token = extractBearerToken(authorizationHeader);
        if (!TextUtils.hasText(token)) {
            return new CmnOAuthTokenIntrospectionResult(false, "Bearer", null, null, null, null, Map.of(),
                    "Authorization 헤더에 Bearer 토큰이 없습니다.");
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

