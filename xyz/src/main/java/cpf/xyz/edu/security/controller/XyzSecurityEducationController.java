package cpf.xyz.edu.security.controller;

import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.sec.token.CmnJwtCreateRequest;
import cpf.cmn.sec.token.CmnJwtService;
import cpf.cmn.sec.token.CmnJwtValidationResult;
import cpf.cmn.sec.token.CmnOAuthBearerTokenService;
import cpf.cmn.sec.token.CmnOAuthTokenIntrospectionResult;
import cpf.pfw.common.execution.CpfOnlineTransaction;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 11. Security", description = "Crypto, JWT, and OAuth bearer token samples")
public class XyzSecurityEducationController {
    private final CmnCryptoService cryptoService;
    private final CmnJwtService jwtService;
    private final CmnOAuthBearerTokenService bearerTokenService;
    private final String issuer;
    private final String audience;
    private final String secret;
    private final long ttlSeconds;

    public XyzSecurityEducationController(
            CmnCryptoService cryptoService,
            CmnJwtService jwtService,
            CmnOAuthBearerTokenService bearerTokenService,
            @Value("${cpf.cmn.security.jwt.issuer:CPF-LOCAL}") String issuer,
            @Value("${cpf.cmn.security.jwt.audience:CPF-LOCAL-API}") String audience,
            @Value("${cpf.cmn.security.jwt.secret:local-education-secret-change-me}") String secret,
            @Value("${cpf.cmn.security.jwt.ttl-seconds:300}") long ttlSeconds) {
        this.cryptoService = cryptoService;
        this.jwtService = jwtService;
        this.bearerTokenService = bearerTokenService;
        this.issuer = issuer;
        this.audience = audience;
        this.secret = secret;
        this.ttlSeconds = ttlSeconds;
    }

    @GetMapping("/security/crypto/basic")
    @CpfOnlineTransaction(id = "OXYZAA0036", name = "XYZBasicCrypto")
    @Operation(operationId = "xyzSecurityEducationBasicCrypto", summary = "Basic crypto sample", description = "Shows Base64, SHA-256, HMAC, and random token utilities.")
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
    @CpfOnlineTransaction(id = "OXYZAA0037", name = "XYZAesGcm")
    @Operation(operationId = "xyzSecurityEducationAesGcm", summary = "AES-GCM sample", description = "Encrypts and decrypts text with the CMN crypto service.")
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
    @CpfOnlineTransaction(id = "OXYZAA0038", name = "XYZPasswordHash")
    @Operation(operationId = "xyzSecurityEducationPasswordHash", summary = "Password hash sample", description = "Creates and verifies a PBKDF2 password hash.")
    public ResponseEntity<Map<String, Object>> passwordHash(@RequestParam(defaultValue = "Sample!2345") String password) {
        String hash = cryptoService.pbkdf2Hash(password);
        return ResponseEntity.ok(Map.of(
                "passwordHash", hash,
                "matches", cryptoService.pbkdf2Matches(password, hash),
                "wrongPasswordMatches", cryptoService.pbkdf2Matches("wrong-password", hash)));
    }

    @PostMapping("/security/jwt/create")
    @CpfOnlineTransaction(id = "OXYZAA0039", name = "XYZJwtCreate")
    @Operation(operationId = "xyzSecurityEducationCreateJwt", summary = "JWT create sample", description = "Creates an HS256 JWT with CPF defaults.")
    public ResponseEntity<Map<String, Object>> createJwt(@RequestParam(defaultValue = "M000000001") String subject) {
        Map<String, Object> claims = Map.of("memberNo", subject, "scope", "member:read account:read", "channel", "XYZ-EDU");
        String token = jwtService.createHs256Token(new CmnJwtCreateRequest(issuer, subject, audience, ttlSeconds, secret, claims));
        return ResponseEntity.ok(Map.of(
                "tokenType", "Bearer",
                "accessToken", token,
                "authorizationHeader", "Bearer " + token,
                "claims", jwtService.readClaimsWithoutVerification(token)));
    }

    @PostMapping("/security/jwt/validate")
    @CpfOnlineTransaction(id = "OXYZAA0040", name = "XYZJwtValidate")
    @Operation(operationId = "xyzSecurityEducationValidateJwt", summary = "JWT validate sample", description = "Validates signature, expiry, issuer, and audience.")
    public ResponseEntity<CmnJwtValidationResult> validateJwt(@RequestParam String token) {
        return ResponseEntity.ok(jwtService.validateHs256Token(token, secret, issuer, audience));
    }

    @GetMapping("/security/jwt/claims")
    @CpfOnlineTransaction(id = "OXYZAA0041", name = "XYZJwtClaims")
    @Operation(operationId = "xyzSecurityEducationReadJwtClaims", summary = "JWT claims sample", description = "Reads claims without signature validation for education only.")
    public ResponseEntity<Map<String, Object>> readJwtClaims(@RequestParam String token) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("expired", jwtService.isExpired(token));
        response.put("claims", jwtService.readClaimsWithoutVerification(token));
        response.put("warning", "Use validate API for authentication decisions.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/security/oauth/introspect")
    @CpfOnlineTransaction(id = "OXYZAA0042", name = "XYZOAuthBearerIntrospect")
    @Operation(operationId = "xyzSecurityEducationIntrospectBearer", summary = "OAuth bearer introspection sample", description = "Extracts and validates a Bearer JWT from Authorization header.")
    public ResponseEntity<CmnOAuthTokenIntrospectionResult> introspectBearer(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        return ResponseEntity.ok(bearerTokenService.introspectJwtBearer(authorization, secret, issuer, audience));
    }
}
