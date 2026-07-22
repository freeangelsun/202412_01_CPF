package com.cpf.common.sec.token;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.common.sec.crypto.CmnCryptoService;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfExternalServiceException;
import com.cpf.core.common.exception.CpfValidationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CMN JWT 생성과 검증 서비스입니다.
 * 현재 프레임워크 기본 구현은 HS256 서명 방식을 제공하며, 운영 환경에서는 secret을 환경변수나 Vault/KMS로 주입합니다.
 */
@Service
public class CmnJwtService extends com.cpf.common.common.base.CmnBaseService {
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final CmnCryptoService cryptoService;

    public CmnJwtService(ObjectMapper objectMapper, CmnCryptoService cryptoService) {
        this.objectMapper = objectMapper;
        this.cryptoService = cryptoService;
    }

    /**
     * HS256 JWT를 생성합니다.
     *
     * @param request JWT 생성 요청
     * @return JWT 문자열
     */
    public String createHs256Token(CmnJwtCreateRequest request) {
        if (request == null) {
            throw new CpfValidationException("JWT 생성 요청이 필요합니다.");
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
     * HS256 JWT를 검증합니다.
     *
     * @param token JWT 문자열
     * @param secret HS256 서명 secret
     * @param expectedIssuer 기대 발급자
     * @param expectedAudience 기대 대상
     * @return JWT 검증 결과
     */
    public CmnJwtValidationResult validateHs256Token(
            String token,
            String secret,
            String expectedIssuer,
            String expectedAudience) {
        try {
            String[] parts = TextUtils.requireText(token, "token").split("\\.");
            if (parts.length != 3) {
                return invalid("JWT 형식이 올바르지 않습니다.");
            }
            Map<String, Object> header = decodeJson(parts[0]);
            if (!"HS256".equals(String.valueOf(header.get("alg")))) {
                return invalid("지원하지 않는 JWT 알고리즘입니다. alg=" + header.get("alg"));
            }
            String signingInput = parts[0] + "." + parts[1];
            String expectedSignature = cryptoService.hmacSha256Base64Url(signingInput, TextUtils.requireText(secret, "secret"));
            if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
                return invalid("JWT 서명이 일치하지 않습니다.");
            }

            Map<String, Object> claims = decodeJson(parts[1]);
            String issuer = stringClaim(claims, "iss");
            String subject = stringClaim(claims, "sub");
            String audience = stringClaim(claims, "aud");
            Instant expiresAt = Instant.ofEpochSecond(longClaim(claims, "exp"));

            if (expiresAt.isBefore(Instant.now())) {
                return new CmnJwtValidationResult(false, "JWT가 만료되었습니다.", subject, issuer, audience, expiresAt, claims);
            }
            if (TextUtils.hasText(expectedIssuer) && !expectedIssuer.equals(issuer)) {
                return new CmnJwtValidationResult(false, "JWT 발급자가 일치하지 않습니다.", subject, issuer, audience, expiresAt, claims);
            }
            if (TextUtils.hasText(expectedAudience) && !expectedAudience.equals(audience)) {
                return new CmnJwtValidationResult(false, "JWT 대상이 일치하지 않습니다.", subject, issuer, audience, expiresAt, claims);
            }
            return new CmnJwtValidationResult(true, "JWT 검증에 성공했습니다.", subject, issuer, audience, expiresAt, claims);
        } catch (CpfValidationException ex) {
            return invalid(ex.getMessage());
        } catch (Exception ex) {
            throw new CpfExternalServiceException("JWT 검증 처리에 실패했습니다.", ex);
        }
    }

    /**
     * JWT 만료 여부를 확인합니다.
     *
     * @param token JWT 문자열
     * @return 만료되었거나 해석할 수 없으면 true
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
     * 서명 검증 없이 claim만 읽습니다.
     * 운영 인증에는 사용하지 말고, 진단이나 교육 샘플처럼 신뢰 경계 밖에서만 사용합니다.
     *
     * @param token JWT 문자열
     * @return JWT claim
     */
    public Map<String, Object> readClaimsWithoutVerification(String token) {
        String[] parts = TextUtils.requireText(token, "token").split("\\.");
        if (parts.length != 3) {
            throw new CpfValidationException("JWT 형식이 올바르지 않습니다.");
        }
        return decodeJson(parts[1]);
    }

    private String encodeJson(Map<String, Object> source) {
        try {
            return cryptoService.base64UrlEncode(objectMapper.writeValueAsBytes(source));
        } catch (Exception ex) {
            throw new CpfExternalServiceException("JWT JSON 인코딩에 실패했습니다.", ex);
        }
    }

    private Map<String, Object> decodeJson(String encoded) {
        try {
            return objectMapper.readValue(cryptoService.base64UrlDecode(encoded), MAP_TYPE);
        } catch (Exception ex) {
            throw new CpfValidationException("JWT JSON 디코딩에 실패했습니다.");
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
