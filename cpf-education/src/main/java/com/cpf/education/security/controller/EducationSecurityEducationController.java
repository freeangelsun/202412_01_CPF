package com.cpf.education.security.controller;
import com.cpf.security.common.crypto.CmnCryptoService;
import com.cpf.security.common.token.CmnJwtCreateRequest;
import com.cpf.security.common.token.CmnJwtService;
import com.cpf.security.common.token.CmnJwtValidationResult;
import com.cpf.security.common.token.CmnOAuthBearerTokenService;
import com.cpf.security.common.token.CmnOAuthTokenIntrospectionResult;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 11. Security", description = "Crypto, JWT, and OAuth bearer token samples")
/** EducationSecurityEducationController 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class EducationSecurityEducationController extends com.cpf.education.base.EducationBaseController {
    private final CmnCryptoService cryptoService;
    private final CmnJwtService jwtService;
    private final CmnOAuthBearerTokenService bearerTokenService;
    private final String issuer;
    private final String audience;
    private final String secret;
    private final long ttlSeconds;

    /** EducationSecurityEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationSecurityEducationController(
            CmnCryptoService cryptoService,
            CmnJwtService jwtService,
            CmnOAuthBearerTokenService bearerTokenService,
            @Value("${cpf.common.security.jwt.issuer:CPF-LOCAL}") String issuer,
            @Value("${cpf.common.security.jwt.audience:CPF-LOCAL-API}") String audience,
            @Value("${cpf.common.security.jwt.secret:local-education-secret-change-me}") String secret,
            @Value("${cpf.common.security.jwt.ttl-seconds:300}") long ttlSeconds) {
        this.cryptoService = cryptoService;
        this.jwtService = jwtService;
        this.bearerTokenService = bearerTokenService;
        this.issuer = issuer;
        this.audience = audience;
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    @GetMapping("/security/crypto/basic")
    @CpfOnlineTransaction(id = "OEDUAA0036", name = "EDUBasicCrypto", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationBasicCrypto", summary = "Basic crypto sample", description = "Shows Base64, SHA-256, HMAC, and random token utilities.")
    /** basicCrypto 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> basicCrypto(@RequestParam(defaultValue = "CPF CoreFlow Platform Framework") String text) {
        String encoded = cryptoService.base64Encode(text);
        String base64Url = cryptoService.base64UrlEncode(text);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("plainText", text);
        response.put("base64", encoded);
        response.put("base64Decoded", cryptoService.base64Decode(encoded));
        response.put("base64Url", base64Url);
        response.put("base64UrlDecoded", cryptoService.base64UrlDecodeToString(base64Url));
        response.put("sha256Hex", cryptoService.sha256Hex(text));
        response.put("sha256Base64Url", cryptoService.sha256Base64Url(text));
        response.put("hmacSha256Base64Url", cryptoService.hmacSha256Base64Url(text, secret));
        response.put("hmacSha256Hex", cryptoService.hmacSha256Hex(text, secret));
        response.put("secureRandomToken", cryptoService.secureRandomToken(32));
        response.put("secureRandomHex", cryptoService.secureRandomHex(32));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/security/crypto/aes-gcm")
    @CpfOnlineTransaction(id = "OEDUAA0037", name = "EDUAesGcm", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationAesGcm", summary = "AES-GCM sample", description = "Encrypts and decrypts text with the CMN crypto service.")
    /** aesGcm 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> aesGcm(@RequestParam(defaultValue = "sample plain text") String plainText) {
        String cipherText = cryptoService.aesGcmEncrypt(plainText, secret);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("plainText", plainText);
        response.put("cipherText", cipherText);
        response.put("decryptedText", cryptoService.aesGcmDecrypt(cipherText, secret));
        response.put("guide", "Production secrets should be managed through Vault, KMS, or equivalent secret storage.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/security/password/hash")
    @CpfOnlineTransaction(id = "OEDUAA0038", name = "EDUPasswordHash", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationPasswordHash", summary = "Password hash sample", description = "Creates and verifies a PBKDF2 password hash.")
    /** passwordHash 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> passwordHash(@RequestParam(defaultValue = "Sample!2345") String password) {
        String hash = cryptoService.pbkdf2Hash(password);
        return ResponseEntity.ok(Map.of(
                "passwordHash", hash,
                "matches", cryptoService.pbkdf2Matches(password, hash),
                "wrongPasswordMatches", cryptoService.pbkdf2Matches("wrong-password", hash)));
    }

    @PostMapping("/security/jwt/create")
    @CpfOnlineTransaction(id = "OEDUAA0039", name = "EDUJwtCreate", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationCreateJwt", summary = "JWT create sample", description = "Creates an HS256 JWT with CPF defaults.")
    /** createJwt 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> createJwt(@RequestParam(defaultValue = "M000000001") String subject) {
        Map<String, Object> claims = Map.of("memberNo", subject, "scope", "member:read account:read", "channel", "EDU Education");
        String token = jwtService.createHs256Token(new CmnJwtCreateRequest(issuer, subject, audience, ttlSeconds, secret, claims));
        return ResponseEntity.ok(Map.of(
                "tokenType", "Bearer",
                "accessToken", token,
                "authorizationHeader", "Bearer " + token,
                "claims", jwtService.readClaimsWithoutVerification(token)));
    }

    @PostMapping("/security/jwt/validate")
    @CpfOnlineTransaction(id = "OEDUAA0040", name = "EDUJwtValidate", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationValidateJwt", summary = "JWT validate sample", description = "Validates signature, expiry, issuer, and audience.")
    /** validateJwt 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CmnJwtValidationResult> validateJwt(@RequestParam String token) {
        return ResponseEntity.ok(jwtService.validateHs256Token(token, secret, issuer, audience));
    }

    @GetMapping("/security/jwt/claims")
    @CpfOnlineTransaction(id = "OEDUAA0041", name = "EDUJwtClaims", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationReadJwtClaims", summary = "JWT claims sample", description = "Reads claims without signature validation for education only.")
    /** readJwtClaims 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> readJwtClaims(@RequestParam String token) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("expired", jwtService.isExpired(token));
        response.put("claims", jwtService.readClaimsWithoutVerification(token));
        response.put("warning", "Use validate API for authentication decisions.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/security/oauth/introspect")
    @CpfOnlineTransaction(id = "OEDUAA0042", name = "EDUOAuthBearerIntrospect", ownerDomain="EDU")
    @Operation(operationId = "refSecurityEducationIntrospectBearer", summary = "OAuth bearer introspection sample", description = "Extracts and validates a Bearer JWT from Authorization header.")
    /** introspectBearer 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<CmnOAuthTokenIntrospectionResult> introspectBearer(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(bearerTokenService.introspectJwtBearer(authorization, secret, issuer, audience));
    }
}
