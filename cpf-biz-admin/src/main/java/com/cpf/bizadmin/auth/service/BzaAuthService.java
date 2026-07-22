package cpf.bza.auth.service;

import cpf.bza.auth.repository.BzaAuthRepository;
import cpf.bza.auth.repository.BzaAuthRepository.BzaOperatorRow;
import cpf.bza.auth.repository.BzaAuthRepository.LoginHistoryWrite;
import cpf.bza.auth.repository.BzaAuthRepository.RefreshTokenRow;
import cpf.bza.auth.repository.BzaAuthRepository.RefreshTokenWrite;
import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.sec.token.CmnJwtCreateRequest;
import cpf.cmn.sec.token.CmnJwtService;
import cpf.cmn.sec.token.CmnJwtValidationResult;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.security.password.CpfPasswordHashingPort;
import cpf.pfw.common.security.password.CpfPasswordVerification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BZA 업무 관리자 인증 서비스입니다.
 *
 * <p>계정, 로그인 이력, refresh token은 bzaDB 저장소를 기준으로 처리합니다. 임시 메모리 저장소를
 * 사용하지 않기 때문에 다중 WAS와 재기동 상황에서도 token 폐기/이력 추적 기준을 유지할 수 있습니다.</p>
 */
@Service
public class BzaAuthService extends cpf.bza.common.base.BzaBaseService {
    private static final String LOGIN_DOMAIN = "BZA";
    private static final String ISSUER = "CPF-BZA";
    private static final String AUDIENCE = "CPF-BZA";

    private final CmnJwtService jwtService;
    private final CmnCryptoService cryptoService;
    private final CpfPasswordHashingPort passwordHashingPort;
    private final BzaAuthRepository authRepository;
    private final String jwtSecret;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String moduleId;
    private final String wasId;

    public BzaAuthService(
            CmnJwtService jwtService,
            CmnCryptoService cryptoService,
            CpfPasswordHashingPort passwordHashingPort,
            BzaAuthRepository authRepository,
            @Value("${cpf.bza.security.jwt-secret:${CPF_BZA_JWT_SECRET:}}") String jwtSecret,
            @Value("${cpf.bza.security.access-token-ttl-seconds:600}") long accessTokenTtlSeconds,
            @Value("${cpf.bza.security.refresh-token-ttl-seconds:7200}") long refreshTokenTtlSeconds,
            @Value("${cpf.framework.module-id:BZA}") String moduleId,
            @Value("${cpf.framework.was-id:bzaAP01}") String wasId) {
        this.jwtService = jwtService;
        this.cryptoService = cryptoService;
        this.passwordHashingPort = passwordHashingPort;
        this.authRepository = authRepository;
        this.jwtSecret = jwtSecret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.moduleId = moduleId;
        this.wasId = wasId;
    }

    /**
     * 업무 관리자 로그인을 처리하고 DB에 로그인 이력과 refresh token hash를 저장합니다.
     */
    public LoginResult login(LoginRequest request, String clientIp, String userAgent) {
        String loginId = TextUtils.requireText(request.loginId(), "loginId");
        String password = TextUtils.requireText(request.password(), "password");
        BzaOperatorRow operator = authRepository.findOperatorByLoginId(loginId).orElse(null);

        if (operator == null) {
            recordLogin(null, loginId, "FAIL", "등록되지 않은 업무 관리자", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정을 확인할 수 없습니다.");
        }
        if (!"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            recordLogin(operator.adminUserId(), loginId, "FAIL", "사용 중지 또는 잠금 상태", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정이 사용할 수 없는 상태입니다.");
        }
        if (!TextUtils.hasText(operator.passwordHash())) {
            recordLogin(operator.adminUserId(), loginId, "FAIL", "비밀번호 hash 미등록", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 비밀번호가 초기화되지 않았습니다.");
        }
        CpfPasswordVerification verification = verifyPassword(password, operator.passwordHash());
        if (!verification.matched()) {
            authRepository.increaseLoginFailCount(operator.adminUserId());
            recordLogin(operator.adminUserId(), loginId, "FAIL", "비밀번호 불일치", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 인증에 실패했습니다.");
        }

        authRepository.markLoginSuccess(operator.adminUserId());
        if (verification.rehashRequired()) {
            authRepository.updatePasswordHashIfUnchanged(
                    operator.adminUserId(), operator.passwordHash(), hashPassword(password), "BZA_PASSWORD_UPGRADE");
        }
        recordLogin(operator.adminUserId(), loginId, "SUCCESS", null, clientIp, userAgent);

        String accessToken = createAccessToken(operator);
        String refreshToken = cryptoService.secureRandomToken(48);
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        Instant refreshExpireAt = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        authRepository.insertRefreshToken(new RefreshTokenWrite(
                operator.adminUserId(),
                LOGIN_DOMAIN,
                refreshHash,
                TransactionContext.getOrCreateTransactionId(),
                refreshExpireAt));
        return new LoginResult(accessToken, refreshToken, "Bearer", accessTokenTtlSeconds, refreshExpireAt, toOperatorResponse(operator));
    }

    /**
     * refresh token hash를 DB에서 검증한 뒤 access token을 재발급합니다.
     */
    @Transactional(transactionManager = "bzaTransactionManager")
    public LoginResult refresh(RefreshRequest request) {
        String refreshToken = TextUtils.requireText(request.refreshToken(), "refreshToken");
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        RefreshTokenRow state = authRepository.findRefreshToken(refreshHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다."));
        if (state.revoked() || state.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
        }
        BzaOperatorRow operator = authRepository.findOperatorByLoginId(state.loginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다."));
        if (!"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다.");
        }
        if (authRepository.revokeRefreshToken(refreshHash) != 1) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 이미 사용되었거나 만료되었습니다.");
        }
        String rotatedToken = cryptoService.secureRandomToken(48);
        String rotatedHash = cryptoService.sha256Base64Url(rotatedToken);
        Instant rotatedExpireAt = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        authRepository.insertRefreshToken(new RefreshTokenWrite(
                operator.adminUserId(), LOGIN_DOMAIN, rotatedHash,
                TransactionContext.getOrCreateTransactionId(), rotatedExpireAt));
        return new LoginResult(createAccessToken(operator), rotatedToken, "Bearer", accessTokenTtlSeconds,
                rotatedExpireAt, toOperatorResponse(operator));
    }

    /**
     * 전달받은 refresh token hash를 폐기합니다.
     */
    public Map<String, Object> logout(RefreshRequest request) {
        if (request != null && TextUtils.hasText(request.refreshToken())) {
            authRepository.revokeRefreshToken(cryptoService.sha256Base64Url(request.refreshToken()));
        }
        return Map.of("logoutYn", "Y", "loginDomain", LOGIN_DOMAIN);
    }

    /**
     * BZA access token을 검증하고 현재 업무 관리자 정보를 반환합니다.
     */
    public Map<String, Object> currentOperator(String authorizationHeader) {
        CmnJwtValidationResult result = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(result.claims().get("loginId"));
        BzaOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        Map<String, Object> response = new LinkedHashMap<>(toOperatorResponse(operator));
        response.put("loginDomain", LOGIN_DOMAIN);
        response.put("tokenExpiresAt", result.expiresAt());
        return response;
    }

    /** BZA API가 요구하는 메뉴·행위 권한을 access token과 현재 DB 권한 기준으로 검사합니다. */
    public Map<String, Object> authorize(String authorizationHeader, String menuCode, String actionCode) {
        CmnJwtValidationResult token = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(token.claims().get("loginId"));
        BzaOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        String required = menuCode + ":" + actionCode;
        boolean allowed = operator.buttons().stream().anyMatch(required::equalsIgnoreCase)
                || operator.buttons().stream().anyMatch(value -> (menuCode + ":ALL").equalsIgnoreCase(value));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "BZA API 권한이 없습니다. permission=" + required);
        }
        return toOperatorResponse(operator);
    }

    /**
     * 최신 로그인 이력을 DB에서 조회합니다.
     */
    public List<Map<String, Object>> loginHistories(String authorizationHeader, int limit) {
        authorize(authorizationHeader, "USER", "READ");
        return authRepository.findLoginHistories(Math.max(1, Math.min(limit, 500)));
    }

    /** 현재 로그인 사용자의 refresh session 메타를 원문 token 없이 조회합니다. */
    public List<Map<String, Object>> sessions(String authorizationHeader, int limit) {
        BzaOperatorRow operator = currentOperatorRow(authorizationHeader);
        return authRepository.findRefreshSessions(operator.adminUserId(), Math.max(1, Math.min(limit, 100)));
    }

    /** 현재 사용자 소유의 refresh session을 사유와 함께 폐기합니다. */
    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> revokeSession(
            String authorizationHeader,
            long sessionId,
            String reason) {
        BzaOperatorRow operator = currentOperatorRow(authorizationHeader);
        String requiredReason = TextUtils.requireText(reason, "reason");
        int updated = authRepository.revokeRefreshSession(sessionId, operator.adminUserId(), operator.loginId());
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "폐기할 활성 세션을 찾을 수 없습니다.");
        }
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        audit.put("actorId", operator.loginId());
        audit.put("actionType", "SESSION_REVOKE");
        audit.put("targetType", "bza_refresh_token");
        audit.put("targetId", String.valueOf(sessionId));
        audit.put("reason", requiredReason);
        audit.put("beforeData", null);
        audit.put("afterData", "{revokedYn=Y}");
        authRepository.insertBusinessAudit(audit);
        return Map.of("sessionId", sessionId, "revokedYn", "Y");
    }

    /** 현재 비밀번호를 확인한 뒤 PFW 공통 형식으로 비밀번호를 교체합니다. */
    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> changePassword(String authorizationHeader, PasswordChangeRequest request) {
        CmnJwtValidationResult token = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(token.claims().get("loginId"));
        BzaOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        String currentPassword = TextUtils.requireText(request.currentPassword(), "currentPassword");
        String newPassword = TextUtils.requireText(request.newPassword(), "newPassword");
        if (!newPassword.equals(request.newPasswordConfirm())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인값이 일치하지 않습니다.");
        }
        requireStrongPassword(loginId, newPassword);
        if (!verifyPassword(currentPassword, operator.passwordHash()).matched()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "현재 비밀번호가 일치하지 않습니다.");
        }
        if (verifyPassword(newPassword, operator.passwordHash()).matched()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 비밀번호는 다시 사용할 수 없습니다.");
        }
        int updated = authRepository.changePassword(
                operator.adminUserId(), operator.passwordHash(), hashPassword(newPassword), loginId);
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "비밀번호가 동시에 변경되었습니다. 다시 로그인하세요.");
        }
        authRepository.revokeAllRefreshTokens(operator.adminUserId());
        return Map.of("changed", true, "loginId", loginId, "refreshTokensRevoked", true);
    }

    private String createAccessToken(BzaOperatorRow operator) {
        requireJwtSecret();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("loginDomain", LOGIN_DOMAIN);
        claims.put("operatorId", operator.adminUserId());
        claims.put("loginId", operator.loginId());
        claims.put("roleCode", operator.roleCode());
        claims.put("moduleId", moduleId);
        claims.put("wasId", wasId);
        claims.put("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        claims.put("menus", operator.menus());
        claims.put("buttons", operator.buttons());
        return jwtService.createHs256Token(new CmnJwtCreateRequest(
                ISSUER,
                String.valueOf(operator.adminUserId()),
                AUDIENCE,
                accessTokenTtlSeconds,
                jwtSecret,
                claims));
    }

    private BzaOperatorRow currentOperatorRow(String authorizationHeader) {
        CmnJwtValidationResult token = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(token.claims().get("loginId"));
        BzaOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        return operator;
    }

    private CmnJwtValidationResult validateAccessToken(String authorizationHeader) {
        requireJwtSecret();
        String token = bearerToken(authorizationHeader);
        CmnJwtValidationResult result = jwtService.validateHs256Token(token, jwtSecret, ISSUER, AUDIENCE);
        if (!result.valid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, result.reason());
        }
        if (!LOGIN_DOMAIN.equals(String.valueOf(result.claims().get("loginDomain")))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BZA token이 아닙니다.");
        }
        return result;
    }

    private String bearerToken(String authorizationHeader) {
        if (!TextUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token이 필요합니다.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
    }

    private CpfPasswordVerification verifyPassword(String rawPassword, String encodedPassword) {
        char[] chars = rawPassword.toCharArray();
        try {
            return passwordHashingPort.verify(chars, encodedPassword);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private String hashPassword(String rawPassword) {
        char[] chars = rawPassword.toCharArray();
        try {
            return passwordHashingPort.hash(chars);
        } finally {
            Arrays.fill(chars, '\0');
        }
    }

    private void requireJwtSecret() {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BZA JWT secret은 32자 이상 운영 환경변수로 설정해야 합니다.");
        }
    }

    private void requireActiveOperator(BzaOperatorRow operator) {
        if (!"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다.");
        }
    }

    private void requireStrongPassword(String loginId, String password) {
        long categories = java.util.stream.Stream.of(
                password.matches(".*[A-Z].*"),
                password.matches(".*[a-z].*"),
                password.matches(".*[0-9].*"),
                password.matches(".*[^A-Za-z0-9].*"))
                .filter(Boolean::booleanValue)
                .count();
        if (password.length() < 12 || categories < 3 || password.toLowerCase().contains(loginId.toLowerCase())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "비밀번호는 12자 이상이며 영문 대·소문자, 숫자, 특수문자 중 3종 이상을 포함하고 로그인 ID를 포함하지 않아야 합니다.");
        }
    }

    private void recordLogin(
            Long adminUserId,
            String loginId,
            String result,
            String failureReason,
            String clientIp,
            String userAgent) {
        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        authRepository.insertLoginHistory(new LoginHistoryWrite(
                adminUserId,
                LOGIN_DOMAIN,
                loginId,
                result,
                failureReason,
                clientIp,
                userAgent,
                TransactionContext.getOrCreateTransactionId(),
                moduleId,
                wasId,
                identity.serverInstanceId()));
    }

    private Map<String, Object> toOperatorResponse(BzaOperatorRow operator) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("operatorId", operator.adminUserId());
        response.put("loginId", operator.loginId());
        response.put("operatorName", operator.adminName());
        response.put("roleCode", operator.roleCode());
        response.put("useYn", operator.useYn());
        response.put("lockYn", operator.lockYn());
        response.put("failCount", operator.loginFailCount());
        response.put("passwordChangeRequiredYn", operator.passwordChangeRequiredYn());
        response.put("passwordExpireAt", operator.passwordExpireAt());
        response.put("lastLoginAt", operator.lastLoginAt());
        response.put("menus", operator.menus());
        response.put("buttons", operator.buttons());
        return response;
    }

    public record LoginRequest(String loginId, String password) {
    }

    public record RefreshRequest(String refreshToken) {
    }

    public record PasswordChangeRequest(
            String currentPassword,
            String newPassword,
            String newPasswordConfirm) {
    }

    public record LoginResult(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            Instant refreshExpiresAt,
            Map<String, Object> operator) {
    }
}
