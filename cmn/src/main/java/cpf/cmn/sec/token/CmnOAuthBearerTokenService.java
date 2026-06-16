package cpf.cmn.sec.token;

import cpf.cmn.utils.TextUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * OAuth Bearer ?좏겙 怨듯넻 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?낅Т ?쒕퉬?ㅻ뒗 Authorization ?ㅻ뜑 ?뚯떛, Bearer ?좏겙 ?뺤떇 寃利? JWT 湲곕컲 ?좏겙 寃利앹쓣
 * ???쒕퉬?ㅻ? ?듯빐 ?섑뻾?⑸땲?? ?몃? OAuth Introspection Endpoint ?몄텧? ?꾩옣 ?몄쬆 ?쒕쾭
 * ?ㅽ럺??留욎떠 ?대뙌?곕? 異붽??섎뒗 諛⑹떇?쇰줈 ?뺤옣?⑸땲??</p>
 */
@Service
public class CmnOAuthBearerTokenService {
    private final CmnJwtService jwtService;

    public CmnOAuthBearerTokenService(CmnJwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Authorization ?ㅻ뜑?먯꽌 Bearer ?좏겙??異붿텧?⑸땲??
     *
     * @param authorizationHeader Authorization ?ㅻ뜑
     * @return Bearer ?좏겙
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
     * Authorization ?ㅻ뜑??Bearer JWT瑜?寃利앺빀?덈떎.
     *
     * @param authorizationHeader Authorization ?ㅻ뜑
     * @param secret              HMAC ?쒗겕由?     * @param expectedIssuer      湲곕? 諛쒓툒??     * @param expectedAudience    湲곕? ????쒖뒪??     * @return OAuth ?좏겙 寃利?寃곌낵
     */
    public CmnOAuthTokenIntrospectionResult introspectJwtBearer(
            String authorizationHeader,
            String secret,
            String expectedIssuer,
            String expectedAudience) {
        String token = extractBearerToken(authorizationHeader);
        if (!TextUtils.hasText(token)) {
            return new CmnOAuthTokenIntrospectionResult(false, "Bearer", null, null, null, null, Map.of(),
                    "Authorization ?ㅻ뜑??Bearer ?좏겙???놁뒿?덈떎.");
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

