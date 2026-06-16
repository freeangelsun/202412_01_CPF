package cpf.cmn.sec.token;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.FpsExternalServiceException;
import cpf.pfw.common.exception.FpsValidationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CMN JWT 怨듯넻 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?몃? OAuth ?쒕쾭???щ궡 ?몄쬆 ?쒕쾭?먯꽌 諛쒓툒??JWT瑜?寃利앺븯嫄곕굹,
 * 援먯쑁/?대? ?곌퀎??HMAC JWT瑜??앹꽦?????ъ슜?⑸땲??</p>
 */
@Service
public class CmnJwtService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final CmnCryptoService cryptoService;

    public CmnJwtService(ObjectMapper objectMapper, CmnCryptoService cryptoService) {
        this.objectMapper = objectMapper;
        this.cryptoService = cryptoService;
    }

    /**
     * HMAC-SHA256 JWT瑜??앹꽦?⑸땲??
     *
     * @param request ?앹꽦 ?붿껌
     * @return JWT 臾몄옄??     */
    public String createHs256Token(CmnJwtCreateRequest request) {
        if (request == null) {
            throw new FpsValidationException("JWT ?앹꽦 ?붿껌? ?꾩닔?낅땲??");
        }
        long now = Instant.now().getEpochSecond();
        long ttl = request.ttlSeconds() <= 0 ? 300 : request.ttlSeconds();

        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        if (request.claims() != null) {
            payload.putAll(request.claims());
        }
        payload.put("iss", TextUtils.defaultIfBlank(request.issuer(), "CPF"));
        payload.put("sub", TextUtils.defaultIfBlank(request.subject(), "anonymous"));
        payload.put("aud", TextUtils.defaultIfBlank(request.audience(), "CPF"));
        payload.put("iat", now);
        payload.put("exp", now + ttl);

        String signingInput = encodeJson(header) + "." + encodeJson(payload);
        return signingInput + "." + cryptoService.hmacSha256Base64Url(signingInput, TextUtils.requireText(request.secret(), "secret"));
    }

    /**
     * HMAC-SHA256 JWT瑜?寃利앺빀?덈떎.
     *
     * @param token            JWT
     * @param secret           HMAC ?쒗겕由?     * @param expectedIssuer   湲곕? 諛쒓툒?? 鍮꾩뼱 ?덉쑝硫?寃利앺븯吏 ?딆뒿?덈떎.
     * @param expectedAudience 湲곕? ????쒖뒪?? 鍮꾩뼱 ?덉쑝硫?寃利앺븯吏 ?딆뒿?덈떎.
     * @return 寃利?寃곌낵
     */
    public CmnJwtValidationResult validateHs256Token(
            String token,
            String secret,
            String expectedIssuer,
            String expectedAudience) {
        try {
            String[] parts = TextUtils.requireText(token, "token").split("\\.");
            if (parts.length != 3) {
                return invalid("JWT ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.");
            }
            Map<String, Object> header = decodeJson(parts[0]);
            if (!"HS256".equals(String.valueOf(header.get("alg")))) {
                return invalid("吏?먰븯吏 ?딅뒗 JWT ?뚭퀬由ъ쬁?낅땲?? alg=" + header.get("alg"));
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = cryptoService.hmacSha256Base64Url(signingInput, TextUtils.requireText(secret, "secret"));
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return invalid("JWT ?쒕챸???좏슚?섏? ?딆뒿?덈떎.");
            }

            Map<String, Object> claims = decodeJson(parts[1]);
            String issuer = stringClaim(claims, "iss");
            String subject = stringClaim(claims, "sub");
            String audience = stringClaim(claims, "aud");
            Instant expiresAt = Instant.ofEpochSecond(longClaim(claims, "exp"));

            if (expiresAt.isBefore(Instant.now())) {
                return new CmnJwtValidationResult(false, "JWT媛 留뚮즺?섏뿀?듬땲??", subject, issuer, audience, expiresAt, claims);
            }
            if (TextUtils.hasText(expectedIssuer) && !expectedIssuer.equals(issuer)) {
                return new CmnJwtValidationResult(false, "JWT 諛쒓툒?먭? ?쇱튂?섏? ?딆뒿?덈떎.", subject, issuer, audience, expiresAt, claims);
            }
            if (TextUtils.hasText(expectedAudience) && !expectedAudience.equals(audience)) {
                return new CmnJwtValidationResult(false, "JWT ????쒖뒪?쒖씠 ?쇱튂?섏? ?딆뒿?덈떎.", subject, issuer, audience, expiresAt, claims);
            }
            return new CmnJwtValidationResult(true, "JWT 寃利앹뿉 ?깃났?덉뒿?덈떎.", subject, issuer, audience, expiresAt, claims);
        } catch (FpsValidationException ex) {
            return invalid(ex.getMessage());
        } catch (Exception ex) {
            throw new FpsExternalServiceException("JWT 寃利?以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.", ex);
        }
    }

    /**
     * JWT 留뚮즺 ?щ?留??뺤씤?⑸땲??
     *
     * @param token JWT
     * @return 留뚮즺 ?щ?
     */
    public boolean isExpired(String token) {
        try {
            String[] parts = TextUtils.requireText(token, "token").split("\\.");
            if (parts.length != 3) {
                return true;
            }
            return Instant.ofEpochSecond(longClaim(decodeJson(parts[1]), "exp")).isBefore(Instant.now());
        } catch (Exception ex) {
            return true;
        }
    }

    /**
     * ?쒕챸 寃利??놁씠 ?대젅?꾩쓣 議고쉶?⑸땲??
     *
     * <p>?붾쾭源??⑸룄?낅땲?? ?몄쬆/?멸? ?먮떒?먮뒗 諛섎뱶??validate 硫붿꽌?쒕? ?ъ슜?⑸땲??</p>
     *
     * @param token JWT
     * @return ?대젅??     */
    public Map<String, Object> readClaimsWithoutVerification(String token) {
        String[] parts = TextUtils.requireText(token, "token").split("\\.");
        if (parts.length != 3) {
            throw new FpsValidationException("JWT ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.");
        }
        return decodeJson(parts[1]);
    }

    private String encodeJson(Map<String, Object> source) {
        try {
            return cryptoService.base64UrlEncode(objectMapper.writeValueAsBytes(source));
        } catch (Exception ex) {
            throw new FpsExternalServiceException("JWT JSON ?몄퐫?⑹뿉 ?ㅽ뙣?덉뒿?덈떎.", ex);
        }
    }

    private Map<String, Object> decodeJson(String encoded) {
        try {
            return objectMapper.readValue(cryptoService.base64UrlDecode(encoded), MAP_TYPE);
        } catch (Exception ex) {
            throw new FpsValidationException("JWT JSON ?붿퐫?⑹뿉 ?ㅽ뙣?덉뒿?덈떎.");
        }
    }

    private CmnJwtValidationResult invalid(String reason) {
        return new CmnJwtValidationResult(false, reason, null, null, null, null, Map.of());
    }

    private String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private long longClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }
}

