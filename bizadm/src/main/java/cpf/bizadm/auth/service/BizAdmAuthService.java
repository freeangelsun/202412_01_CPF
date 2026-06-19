package cpf.bizadm.auth.service;

import cpf.bizadm.auth.repository.BizAdmAuthRepository;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.BizAdmOperatorRow;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.LoginHistoryWrite;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.RefreshTokenRow;
import cpf.bizadm.auth.repository.BizAdmAuthRepository.RefreshTokenWrite;
import cpf.cmn.sec.crypto.CmnCryptoService;
import cpf.cmn.sec.token.CmnJwtCreateRequest;
import cpf.cmn.sec.token.CmnJwtService;
import cpf.cmn.sec.token.CmnJwtValidationResult;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.logging.ServerInstanceIdentity;
import cpf.pfw.common.logging.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BIZADM 업무 관리자 인증 서비스입니다.
 *
 * <p>계정, 로그인 이력, refresh token은 bizadmDB 저장소를 기준으로 처리합니다. 임시 메모리 저장소를
 * 사용하지 않기 때문에 다중 WAS와 재기동 상황에서도 token 폐기/이력 추적 기준을 유지할 수 있습니다.</p>
 */
@Service
public class BizAdmAuthService {
    private static final String LOGIN_DOMAIN = "BIZADM";
    private static final String ISSUER = "CPF-BIZADM";
    private static final String AUDIENCE = "CPF-BIZADM";

    private final CmnJwtService jwtService;
    private final CmnCryptoService cryptoService;
    private final BizAdmAuthRepository authRepository;
    private final String jwtSecret;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String moduleId;
    private final String wasId;

    public BizAdmAuthService(
            CmnJwtService jwtService,
            CmnCryptoService cryptoService,
            BizAdmAuthRepository authRepository,
            @Value("${cpf.bizadm.security.jwt-secret:${CPF_BIZADM_JWT_SECRET:local-bizadm-education-secret-change-me}}") String jwtSecret,
            @Value("${cpf.bizadm.security.access-token-ttl-seconds:600}") long accessTokenTtlSeconds,
            @Value("${cpf.bizadm.security.refresh-token-ttl-seconds:7200}") long refreshTokenTtlSeconds,
            @Value("${cpf.framework.module-id:BIZ}") String moduleId,
            @Value("${cpf.framework.was-id:bizAP01}") String wasId) {
        this.jwtService = jwtService;
        this.cryptoService = cryptoService;
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
        BizAdmOperatorRow operator = authRepository.findOperatorByLoginId(loginId).orElse(null);

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
        if (!cryptoService.pbkdf2Matches(password, operator.passwordHash())) {
            authRepository.increaseLoginFailCount(operator.adminUserId());
            recordLogin(operator.adminUserId(), loginId, "FAIL", "비밀번호 불일치", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 인증에 실패했습니다.");
        }

        authRepository.markLoginSuccess(operator.adminUserId());
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
    public LoginResult refresh(RefreshRequest request) {
        String refreshToken = TextUtils.requireText(request.refreshToken(), "refreshToken");
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        RefreshTokenRow state = authRepository.findRefreshToken(refreshHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다."));
        if (state.revoked() || state.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
        }
        BizAdmOperatorRow operator = authRepository.findOperatorByLoginId(state.loginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다."));
        if (!"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다.");
        }
        return new LoginResult(createAccessToken(operator), null, "Bearer", accessTokenTtlSeconds, state.expiresAt(), toOperatorResponse(operator));
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
     * BIZADM access token을 검증하고 현재 업무 관리자 정보를 반환합니다.
     */
    public Map<String, Object> currentOperator(String authorizationHeader) {
        CmnJwtValidationResult result = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(result.claims().get("loginId"));
        BizAdmOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        Map<String, Object> response = new LinkedHashMap<>(toOperatorResponse(operator));
        response.put("loginDomain", LOGIN_DOMAIN);
        response.put("tokenExpiresAt", result.expiresAt());
        return response;
    }

    /**
     * 최신 로그인 이력을 DB에서 조회합니다.
     */
    public List<Map<String, Object>> loginHistories(int limit) {
        return authRepository.findLoginHistories(Math.max(1, Math.min(limit, 500)));
    }

    private String createAccessToken(BizAdmOperatorRow operator) {
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

    private CmnJwtValidationResult validateAccessToken(String authorizationHeader) {
        String token = bearerToken(authorizationHeader);
        CmnJwtValidationResult result = jwtService.validateHs256Token(token, jwtSecret, ISSUER, AUDIENCE);
        if (!result.valid()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, result.reason());
        }
        if (!LOGIN_DOMAIN.equals(String.valueOf(result.claims().get("loginDomain")))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "BIZADM token이 아닙니다.");
        }
        return result;
    }

    private String bearerToken(String authorizationHeader) {
        if (!TextUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token이 필요합니다.");
        }
        return authorizationHeader.substring("Bearer ".length()).trim();
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

    private Map<String, Object> toOperatorResponse(BizAdmOperatorRow operator) {
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

    public record LoginResult(
            String accessToken,
            String refreshToken,
            String tokenType,
            long expiresIn,
            Instant refreshExpiresAt,
            Map<String, Object> operator) {
    }
}
