package cpf.cmn.sec.token;

import java.time.Instant;
import java.util.Map;

/**
 * OAuth Bearer ?좏겙 寃利??명듃濡쒖뒪?숈뀡 寃곌낵?낅땲??
 *
 * @param active    ?좏겙 ?쒖꽦 ?щ?
 * @param tokenType ?좏겙 ?좏삎
 * @param subject   ?좏겙 二쇱껜
 * @param issuer    諛쒓툒?? * @param audience  ????쒖뒪?? * @param expiresAt 留뚮즺 ?쒓컖
 * @param claims    ?좏겙 ?대젅?? * @param reason    ?ㅽ뙣 ?먮뒗 李멸퀬 硫붿떆吏
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

