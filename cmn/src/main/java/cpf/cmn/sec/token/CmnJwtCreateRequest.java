package cpf.cmn.sec.token;

import java.util.Map;

/**
 * JWT ?앹꽦 ?붿껌?낅땲??
 *
 * @param issuer     諛쒓툒?? * @param subject    ?좏겙 二쇱껜
 * @param audience   ????쒖뒪?? * @param ttlSeconds ?좏슚 ?쒓컙(珥?
 * @param secret     HMAC ?쒕챸 ?쒗겕由? * @param claims     異붽? ?대젅?? */
public record CmnJwtCreateRequest(
        String issuer,
        String subject,
        String audience,
        long ttlSeconds,
        String secret,
        Map<String, Object> claims) {
}

