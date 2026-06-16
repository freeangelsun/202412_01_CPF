package cpf.cmn.sec.token;

import java.time.Instant;
import java.util.Map;

/**
 * JWT 寃利?寃곌낵?낅땲??
 *
 * @param valid     ?좏슚 ?щ?
 * @param reason    ?ㅽ뙣 ?ъ쑀 ?먮뒗 ?깃났 硫붿떆吏
 * @param subject   ?좏겙 二쇱껜
 * @param issuer    諛쒓툒?? * @param audience  ????쒖뒪?? * @param expiresAt 留뚮즺 ?쒓컖
 * @param claims    ?꾩껜 ?대젅?? */
public record CmnJwtValidationResult(
        boolean valid,
        String reason,
        String subject,
        String issuer,
        String audience,
        Instant expiresAt,
        Map<String, Object> claims) {
}

