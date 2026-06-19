package cpf.bizadm.sample.service;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BIZADM 인증 기본 구현체입니다.
 *
 * <p>업무 관리자 모듈이 독립 기동될 수 있도록 로컬 기본 저장소를 사용하되, JWT 생성과 비밀번호/토큰 hash는
 * CMN 보안 유틸을 재사용합니다. 실제 프로젝트에서는 이 클래스의 저장소 부분만 bizadmDB mapper로 교체하면 됩니다.</p>
 */
@Service
public class BizAdmAuthService {
    private static final String LOGIN_DOMAIN = "BIZADM";
    private static final String ISSUER = "CPF-BIZADM";
    private static final String AUDIENCE = "CPF-BIZADM";

    private final CmnJwtService jwtService;
    private final CmnCryptoService cryptoService;
    private final String jwtSecret;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String moduleId;
    private final String wasId;
    private final Map<String, BizAdmOperator> operators = new ConcurrentHashMap<>();
    private final Map<String, RefreshTokenState> refreshTokensByHash = new ConcurrentHashMap<>();
    private final List<LoginHistoryRow> loginHistories = new CopyOnWriteArrayList<>();

    public BizAdmAuthService(
            CmnJwtService jwtService,
            CmnCryptoService cryptoService,
            @Value("${cpf.bizadm.security.jwt-secret:${CPF_BIZADM_JWT_SECRET:local-bizadm-education-secret-change-me}}") String jwtSecret,
            @Value("${cpf.bizadm.security.access-token-ttl-seconds:600}") long accessTokenTtlSeconds,
            @Value("${cpf.bizadm.security.refresh-token-ttl-seconds:7200}") long refreshTokenTtlSeconds,
            @Value("${cpf.framework.module-id:BIZ}") String moduleId,
            @Value("${cpf.framework.was-id:bizAP01}") String wasId) {
        this.jwtService = jwtService;
        this.cryptoService = cryptoService;
        this.jwtSecret = jwtSecret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.moduleId = moduleId;
        this.wasId = wasId;
        seedLocalOperator();
    }

    /**
     * 업무 관리자 로그인을 처리하고 access token과 refresh token을 발급합니다.
     */
    public LoginResult login(LoginRequest request, String clientIp, String userAgent) {
        String loginId = TextUtils.requireText(request.loginId(), "loginId");
        String password = TextUtils.requireText(request.password(), "password");
        BizAdmOperator operator = operators.get(loginId);
        if (operator == null) {
            recordLogin(loginId, null, "FAIL", "등록되지 않은 업무 관리자", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정을 확인할 수 없습니다.");
        }
        if (!"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            recordLogin(loginId, operator.operatorId(), "FAIL", "사용 중지 또는 잠금 상태", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정이 사용할 수 없는 상태입니다.");
        }
        if (!cryptoService.pbkdf2Matches(password, operator.passwordHash())) {
            operator.increaseFailCount();
            recordLogin(loginId, operator.operatorId(), "FAIL", "비밀번호 불일치", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 인증에 실패했습니다.");
        }

        operator.resetFailCount();
        operator.updateLastLoginAt(Instant.now());
        recordLogin(loginId, operator.operatorId(), "SUCCESS", null, clientIp, userAgent);

        String accessToken = createAccessToken(operator);
        String refreshToken = cryptoService.secureRandomToken(48);
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        Instant refreshExpireAt = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        refreshTokensByHash.put(refreshHash, new RefreshTokenState(
                refreshHash,
                operator.operatorId(),
                loginId,
                LOGIN_DOMAIN,
                refreshExpireAt,
                false,
                TransactionContext.getOrCreateTransactionId()));

        return new LoginResult(accessToken, refreshToken, "Bearer", accessTokenTtlSeconds, refreshExpireAt, operator.toResponse());
    }

    /**
     * refresh token hash를 기준으로 access token을 재발급합니다.
     */
    public LoginResult refresh(RefreshRequest request) {
        String refreshToken = TextUtils.requireText(request.refreshToken(), "refreshToken");
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        RefreshTokenState state = refreshTokensByHash.get(refreshHash);
        if (state == null || state.revoked() || state.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
        }
        BizAdmOperator operator = operators.get(state.loginId());
        if (operator == null || !"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다.");
        }
        String accessToken = createAccessToken(operator);
        return new LoginResult(accessToken, null, "Bearer", accessTokenTtlSeconds, state.expiresAt(), operator.toResponse());
    }

    /**
     * 전달받은 refresh token을 폐기합니다. 서버에는 hash만 저장되어 원문 token은 비교 직전에만 사용합니다.
     */
    public Map<String, Object> logout(RefreshRequest request) {
        if (request != null && TextUtils.hasText(request.refreshToken())) {
            String refreshHash = cryptoService.sha256Base64Url(request.refreshToken());
            RefreshTokenState state = refreshTokensByHash.get(refreshHash);
            if (state != null) {
                refreshTokensByHash.put(refreshHash, state.revoke());
            }
        }
        return Map.of("logoutYn", "Y", "loginDomain", LOGIN_DOMAIN);
    }

    /**
     * Authorization header의 access token을 검증하고 현재 업무 관리자 정보를 반환합니다.
     */
    public Map<String, Object> currentOperator(String authorizationHeader) {
        CmnJwtValidationResult result = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(result.claims().get("loginId"));
        BizAdmOperator operator = operators.get(loginId);
        if (operator == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다.");
        }
        Map<String, Object> response = new LinkedHashMap<>(operator.toResponse());
        response.put("loginDomain", LOGIN_DOMAIN);
        response.put("tokenExpiresAt", result.expiresAt());
        return response;
    }

    /**
     * 최신 로그인 이력을 조회합니다.
     */
    public List<Map<String, Object>> loginHistories(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return loginHistories.stream()
                .sorted(Comparator.comparing(LoginHistoryRow::occurredAt).reversed())
                .limit(resolvedLimit)
                .map(LoginHistoryRow::toResponse)
                .toList();
    }

    private void seedLocalOperator() {
        BizAdmOperator manager = new BizAdmOperator(
                "BIZ-OPR-0001",
                "biz-admin",
                "업무 관리자",
                "BIZ_MANAGER",
                cryptoService.pbkdf2Hash("BizAdm!2345"),
                "Y",
                "N",
                0,
                null,
                List.of("BIZ_CUSTOMER", "BIZ_PRODUCT", "BIZ_ORDER", "BIZ_SETTING"),
                List.of("BIZ_READ", "BIZ_WRITE", "BIZ_DOWNLOAD", "BIZ_UNMASK"));
        operators.put(manager.loginId(), manager);
    }

    private String createAccessToken(BizAdmOperator operator) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("loginDomain", LOGIN_DOMAIN);
        claims.put("operatorId", operator.operatorId());
        claims.put("loginId", operator.loginId());
        claims.put("roleCode", operator.roleCode());
        claims.put("moduleId", moduleId);
        claims.put("wasId", wasId);
        claims.put("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        claims.put("menus", operator.menus());
        claims.put("buttons", operator.buttons());
        return jwtService.createHs256Token(new CmnJwtCreateRequest(
                ISSUER,
                operator.operatorId(),
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

    private void recordLogin(String loginId, String operatorId, String result, String failureReason, String clientIp, String userAgent) {
        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        loginHistories.add(new LoginHistoryRow(
                loginHistories.size() + 1L,
                LOGIN_DOMAIN,
                operatorId,
                loginId,
                result,
                failureReason,
                clientIp,
                userAgent,
                TransactionContext.getOrCreateTransactionId(),
                moduleId,
                wasId,
                identity.serverInstanceId(),
                Instant.now()));
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

    private record RefreshTokenState(
            String refreshTokenHash,
            String operatorId,
            String loginId,
            String loginDomain,
            Instant expiresAt,
            boolean revoked,
            String transactionGlobalId) {
        private RefreshTokenState revoke() {
            return new RefreshTokenState(refreshTokenHash, operatorId, loginId, loginDomain, expiresAt, true, transactionGlobalId);
        }
    }

    private static final class BizAdmOperator {
        private final String operatorId;
        private final String loginId;
        private final String operatorName;
        private final String roleCode;
        private final String passwordHash;
        private final String useYn;
        private final String lockYn;
        private final List<String> menus;
        private final List<String> buttons;
        private int failCount;
        private Instant lastLoginAt;

        private BizAdmOperator(
                String operatorId,
                String loginId,
                String operatorName,
                String roleCode,
                String passwordHash,
                String useYn,
                String lockYn,
                int failCount,
                Instant lastLoginAt,
                List<String> menus,
                List<String> buttons) {
            this.operatorId = operatorId;
            this.loginId = loginId;
            this.operatorName = operatorName;
            this.roleCode = roleCode;
            this.passwordHash = passwordHash;
            this.useYn = useYn;
            this.lockYn = lockYn;
            this.failCount = failCount;
            this.lastLoginAt = lastLoginAt;
            this.menus = new ArrayList<>(menus);
            this.buttons = new ArrayList<>(buttons);
        }

        private String operatorId() {
            return operatorId;
        }

        private String loginId() {
            return loginId;
        }

        private String roleCode() {
            return roleCode;
        }

        private String passwordHash() {
            return passwordHash;
        }

        private String useYn() {
            return useYn;
        }

        private String lockYn() {
            return lockYn;
        }

        private List<String> menus() {
            return List.copyOf(menus);
        }

        private List<String> buttons() {
            return List.copyOf(buttons);
        }

        private void increaseFailCount() {
            failCount++;
        }

        private void resetFailCount() {
            failCount = 0;
        }

        private void updateLastLoginAt(Instant lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
        }

        private Map<String, Object> toResponse() {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("operatorId", operatorId);
            response.put("loginId", loginId);
            response.put("operatorName", operatorName);
            response.put("roleCode", roleCode);
            response.put("useYn", useYn);
            response.put("lockYn", lockYn);
            response.put("failCount", failCount);
            response.put("lastLoginAt", lastLoginAt);
            response.put("menus", menus);
            response.put("buttons", buttons);
            return response;
        }
    }

    private record LoginHistoryRow(
            long historyId,
            String loginDomain,
            String operatorId,
            String loginId,
            String loginResult,
            String failureReason,
            String clientIp,
            String userAgent,
            String transactionGlobalId,
            String moduleId,
            String wasId,
            String serverInstanceId,
            Instant occurredAt) {
        private Map<String, Object> toResponse() {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("historyId", historyId);
            response.put("loginDomain", loginDomain);
            response.put("operatorId", operatorId);
            response.put("loginId", loginId);
            response.put("loginResult", loginResult);
            response.put("failureReason", failureReason);
            response.put("clientIp", clientIp);
            response.put("userAgent", userAgent);
            response.put("transactionGlobalId", transactionGlobalId);
            response.put("moduleId", moduleId);
            response.put("wasId", wasId);
            response.put("serverInstanceId", serverInstanceId);
            response.put("occurredAt", occurredAt);
            return response;
        }
    }
}
