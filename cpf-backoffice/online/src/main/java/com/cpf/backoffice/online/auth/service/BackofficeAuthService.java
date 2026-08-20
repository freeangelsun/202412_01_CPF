package com.cpf.backoffice.online.auth.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.backoffice.online.auth.model.BackofficeAdminAccountStatus;
import com.cpf.backoffice.online.auth.dto.*;

import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.BackofficeOperatorRow;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.RefreshTokenRow;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository.RefreshTokenWrite;
import com.cpf.security.common.crypto.CmnCryptoService;
import com.cpf.security.common.token.CmnJwtCreateRequest;
import com.cpf.security.common.token.CmnJwtService;
import com.cpf.security.common.token.CmnJwtValidationResult;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.security.api.password.CpfPasswordEncoder;
import com.cpf.security.api.password.CpfPasswordVerification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import com.cpf.foundation.runtime.CpfRuntimeSystemCode;
import org.springframework.http.HttpStatus;
import com.cpf.foundation.annotation.CpfService;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MBW 업무 관리자 인증 서비스입니다.
 *
 * <p>계정, 로그인 이력, refresh token은 MBW_DB 저장소를 기준으로 처리합니다. 임시 메모리 저장소를
 * 사용하지 않기 때문에 다중 WAS와 재기동 상황에서도 token 폐기/이력 추적 기준을 유지할 수 있습니다.</p>
 */
@CpfService
public class BackofficeAuthService extends com.cpf.backoffice.online.base.BackofficeBaseService {
    private static final String LOGIN_DOMAIN = "MBW";
    private static final String ISSUER = "CPF-MBW";
    private static final String AUDIENCE = "CPF-MBW";

    private final CmnJwtService jwtService;
    private final CmnCryptoService cryptoService;
    private final CpfPasswordEncoder passwordHashingPort;
    private final BackofficeAuthRepository authRepository;
    private final BackofficeBusinessAuditService auditService;
    private final BackofficeLoginTransactionService loginTransactionService;
    private final String jwtSecret;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String systemCode;
    private final String applicationName;

    public BackofficeAuthService(
            CmnJwtService jwtService,
            CmnCryptoService cryptoService,
            CpfPasswordEncoder passwordHashingPort,
            BackofficeAuthRepository authRepository,
            BackofficeBusinessAuditService auditService,
            BackofficeLoginTransactionService loginTransactionService,
            @Value("${cpf.backoffice.security.jwt-secret:${CPF_MBW_JWT_SECRET:}}") String jwtSecret,
            @Value("${cpf.backoffice.security.access-token-ttl-seconds:600}") long accessTokenTtlSeconds,
            @Value("${cpf.backoffice.security.refresh-token-ttl-seconds:7200}") long refreshTokenTtlSeconds,
            Environment environment) {
        this.jwtService = jwtService;
        this.cryptoService = cryptoService;
        this.passwordHashingPort = passwordHashingPort;
        this.authRepository = authRepository;
        this.auditService = auditService;
        this.loginTransactionService = loginTransactionService;
        this.jwtSecret = jwtSecret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.systemCode = CpfRuntimeSystemCode.resolve(environment);
        this.applicationName = environment.getProperty("spring.application.name", "cpf-backoffice").trim();
        if (this.applicationName.isBlank()) throw new IllegalArgumentException("spring.application.name is required");
    }

    /**
     * 업무 관리자 로그인을 처리하고 DB에 로그인 이력과 refresh token hash를 저장합니다.
     */
    public LoginResult login(LoginRequest request, String clientIp, String userAgent) {
        String loginId = CpfStrings.requireText(request.loginId(), "loginId");
        String password = CpfStrings.requireText(request.password(), "password");
        String idempotencyKey = CpfStrings.requireText(request.idempotencyKey(), "idempotencyKey");
        if (idempotencyKey.length() > 128) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Idempotency-Key는 128자를 초과할 수 없습니다.");
        }
        BackofficeOperatorRow operator = authRepository.findOperatorByLoginId(loginId).orElse(null);

        if (operator == null) {
            loginTransactionService.recordFailure(failureCommand(null, loginId, "등록되지 않은 업무 관리자", clientIp, userAgent, false));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정을 확인할 수 없습니다.");
        }
        if (!BackofficeAdminAccountStatus.ACTIVE.name().equals(operator.accountStatus()) || !"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            loginTransactionService.recordFailure(failureCommand(operator.adminUserId(), loginId, "사용 중지 또는 잠금 상태", clientIp, userAgent, false));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정이 사용할 수 없는 상태입니다.");
        }
        if (!CpfStrings.hasText(operator.passwordHash())) {
            loginTransactionService.recordFailure(failureCommand(operator.adminUserId(), loginId, "비밀번호 hash 미등록", clientIp, userAgent, false));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 비밀번호가 초기화되지 않았습니다.");
        }
        CpfPasswordVerification verification = verifyPassword(password, operator.passwordHash());
        if (!verification.matched()) {
            loginTransactionService.recordFailure(failureCommand(operator.adminUserId(), loginId, "비밀번호 불일치", clientIp, userAgent, true));
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 인증에 실패했습니다.");
        }

        requireJwtSecret();
        String requestHash = cryptoService.hmacSha256Hex(
                "MBW_LOGIN|" + loginId + "|" + cryptoService.sha256Hex(password), jwtSecret);
        String refreshToken = cryptoService.secureRandomToken(48);
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        Instant now = Instant.now();
        Instant refreshExpireAt = now.plusSeconds(refreshTokenTtlSeconds);
        Instant replayExpireAt = now.plusSeconds(accessTokenTtlSeconds);
        String accessToken = createAccessToken(operator);
        String resultSecret = jwtSecret + ":MBW_LOGIN_RESULT";
        String accessTokenEnc = cryptoService.aesGcmEncrypt(accessToken, resultSecret);
        String refreshTokenEnc = cryptoService.aesGcmEncrypt(refreshToken, resultSecret);
        String upgradedHash = verification.rehashRequired() ? hashPassword(password) : null;
        CpfInstanceIdentity.Identity identity = CpfInstanceIdentity.current();
        BackofficeLoginTransactionService.LoginCommitResult commit = loginTransactionService.commitSuccess(
                new BackofficeLoginTransactionService.LoginSuccessCommand(
                        idempotencyKey,
                        requestHash,
                        operator,
                        operator.passwordHash(),
                        upgradedHash,
                        refreshHash,
                        refreshExpireAt,
                        accessTokenEnc,
                        refreshTokenEnc,
                        replayExpireAt,
                        clientIp,
                        userAgent,
                        CpfContexts.transactionId(),
                        systemCode,
                        applicationName,
                        identity.instanceId()));

        // response-loss 재시도는 최초 성공 결과 암호문을 그대로 복호화하여 새 refresh session을 만들지 않습니다.
        String committedAccessToken = cryptoService.aesGcmDecrypt(commit.resultAccessTokenEnc(), resultSecret);
        String committedRefreshToken = cryptoService.aesGcmDecrypt(commit.resultRefreshTokenEnc(), resultSecret);
        BackofficeOperatorRow committedOperator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "로그인 commit 이후 업무 관리자 상태를 다시 확인할 수 없습니다."));
        requireActiveOperator(committedOperator);
        return new LoginResult(committedAccessToken, committedRefreshToken, "Bearer", accessTokenTtlSeconds,
                commit.refreshExpireAt(), toOperatorResponse(committedOperator));
    }

    /**
     * refresh token hash를 DB에서 검증한 뒤 access token을 재발급합니다.
     */
    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public LoginResult refresh(RefreshRequest request) {
        String refreshToken = CpfStrings.requireText(request.refreshToken(), "refreshToken");
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        RefreshTokenRow state = authRepository.findRefreshToken(refreshHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다."));
        if (state.revoked() || state.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
        }
        BackofficeOperatorRow operator = authRepository.findOperatorByLoginId(state.loginId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다."));
        if (!BackofficeAdminAccountStatus.ACTIVE.name().equals(operator.accountStatus()) || !"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
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
                CpfContexts.transactionId(), null, rotatedExpireAt));
        return new LoginResult(createAccessToken(operator), rotatedToken, "Bearer", accessTokenTtlSeconds,
                rotatedExpireAt, toOperatorResponse(operator));
    }

    /**
     * 전달받은 refresh token hash를 폐기합니다.
     */
    public BackofficeLogoutResponse logout(RefreshRequest request) {
        if (request != null && CpfStrings.hasText(request.refreshToken())) {
            authRepository.revokeRefreshToken(cryptoService.sha256Base64Url(request.refreshToken()));
        }
        return new BackofficeLogoutResponse(true, LOGIN_DOMAIN);
    }

    /**
     * MBW access token을 검증하고 현재 업무 관리자 정보를 반환합니다.
     */
    public BackofficeCurrentOperatorResponse currentOperator(String authorizationHeader) {
        CmnJwtValidationResult result = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(result.claims().get("loginId"));
        BackofficeOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        return new BackofficeCurrentOperatorResponse(toOperatorResponse(operator), LOGIN_DOMAIN, result.expiresAt());
    }

    /** MBW API가 요구하는 메뉴·행위 권한을 access token과 현재 DB 권한 기준으로 검사합니다. */
    public BackofficeAuthorizationResult authorize(String authorizationHeader, String menuCode, String actionCode) {
        CmnJwtValidationResult token = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(token.claims().get("loginId"));
        BackofficeOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        requirePasswordPolicySatisfied(operator);
        String required = menuCode + ":" + actionCode;
        boolean allowed = operator.buttons().stream().anyMatch(required::equalsIgnoreCase)
                || operator.buttons().stream().anyMatch(value -> (menuCode + ":ALL").equalsIgnoreCase(value));
        if (!allowed) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "MBW API 권한이 없습니다. permission=" + required);
        }
        return new BackofficeAuthorizationResult(toOperatorResponse(operator), menuCode, actionCode);
    }

    /**
     * 최신 로그인 이력을 DB에서 조회합니다.
     */
    public List<BackofficeLoginHistoryResponse> loginHistories(String authorizationHeader, int limit) {
        authorize(authorizationHeader, "AUTHORIZATION", "READ");
        return authRepository.findLoginHistories(Math.max(1, Math.min(limit, 500))).stream()
                .map(this::toLoginHistoryResponse)
                .toList();
    }

    /** 현재 로그인 사용자의 refresh session 메타를 원문 token 없이 조회합니다. */
    public List<BackofficeSessionResponse> sessions(String authorizationHeader, int limit) {
        BackofficeOperatorRow operator = currentOperatorRow(authorizationHeader);
        return authRepository.findRefreshSessions(operator.adminUserId(), Math.max(1, Math.min(limit, 100))).stream()
                .map(this::toSessionResponse)
                .toList();
    }

    /** 현재 사용자 소유의 refresh session을 사유와 함께 폐기합니다. */
    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public BackofficeSessionRevokeResponse revokeSession(
            String authorizationHeader,
            long sessionId,
            String reason) {
        BackofficeOperatorRow operator = currentOperatorRow(authorizationHeader);
        String requiredReason = CpfStrings.requireText(reason, "reason");
        int updated = authRepository.revokeRefreshSession(sessionId, operator.adminUserId(), operator.loginId());
        if (updated != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "폐기할 활성 세션을 찾을 수 없습니다.");
        }
auditService.record(operator.loginId(), "SESSION_REVOKE", "mbw_refresh_token", String.valueOf(sessionId), requiredReason, null, Map.of("revokedYn", "Y"));
        return new BackofficeSessionRevokeResponse(sessionId, true);
    }

    /** 현재 비밀번호를 확인한 뒤 CPF 공통 형식으로 비밀번호를 교체합니다. */
    @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
    public BackofficePasswordChangeResponse changePassword(String authorizationHeader, PasswordChangeRequest request) {
        CmnJwtValidationResult token = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(token.claims().get("loginId"));
        BackofficeOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 정보를 찾을 수 없습니다."));
        requireActiveOperator(operator);
        String currentPassword = CpfStrings.requireText(request.currentPassword(), "currentPassword");
        String newPassword = CpfStrings.requireText(request.newPassword(), "newPassword");
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
        return new BackofficePasswordChangeResponse(true, loginId, true);
    }

    private String createAccessToken(BackofficeOperatorRow operator) {
        requireJwtSecret();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("loginDomain", LOGIN_DOMAIN);
        claims.put("operatorId", operator.adminUserId());
        claims.put("loginId", operator.loginId());
        claims.put("roleCode", operator.roleCode());
        claims.put("systemCode", systemCode);
        claims.put("application", applicationName);
        claims.put("instanceId", CpfInstanceIdentity.current().instanceId());
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

    private BackofficeOperatorRow currentOperatorRow(String authorizationHeader) {
        CmnJwtValidationResult token = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(token.claims().get("loginId"));
        BackofficeOperatorRow operator = authRepository.findOperatorByLoginId(loginId)
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MBW token이 아닙니다.");
        }
        return result;
    }

    private String bearerToken(String authorizationHeader) {
        if (!CpfStrings.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
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
        if (jwtSecret == null || jwtSecret.length() < 32 || jwtSecret.startsWith("__REPLACE_")) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "MBW JWT secret은 32자 이상 운영 환경변수로 설정해야 합니다.");
        }
    }

    private void requireActiveOperator(BackofficeOperatorRow operator) {
        if (!BackofficeAdminAccountStatus.ACTIVE.name().equals(operator.accountStatus()) || !"Y".equals(operator.useYn()) || "Y".equals(operator.lockYn())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "업무 관리자 계정 상태가 유효하지 않습니다.");
        }
    }

    private boolean passwordChangeRequired(BackofficeOperatorRow operator) {
        return "Y".equals(operator.passwordChangeRequiredYn())
                || (operator.passwordExpireAt() != null && !operator.passwordExpireAt().isAfter(Instant.now()));
    }

    private void requirePasswordPolicySatisfied(BackofficeOperatorRow operator) {
        if (passwordChangeRequired(operator)) {
            throw new ResponseStatusException(
                    HttpStatus.PRECONDITION_REQUIRED,
                    "비밀번호 변경이 필요합니다. 비밀번호 변경 API 외 업무 API는 사용할 수 없습니다.");
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

    private BackofficeLoginTransactionService.LoginFailureCommand failureCommand(
            Long adminUserId, String loginId, String reason, String clientIp, String userAgent, boolean increaseFailCount) {
        CpfInstanceIdentity.Identity identity = CpfInstanceIdentity.current();
        return new BackofficeLoginTransactionService.LoginFailureCommand(
                adminUserId, loginId, reason, clientIp, userAgent, increaseFailCount,
                CpfContexts.transactionId(), systemCode, applicationName, identity.instanceId());
    }

    private BackofficeOperatorResponse toOperatorResponse(BackofficeOperatorRow operator) {
        return new BackofficeOperatorResponse(
                operator.adminUserId(), operator.loginId(), operator.adminName(), operator.roleCode(),
                operator.accountStatus(), operator.useYn(), operator.lockYn(), operator.loginFailCount(),
                passwordChangeRequired(operator) ? "Y" : "N", operator.passwordExpireAt(), operator.lastLoginAt(),
                operator.menus(), operator.buttons());
    }

    private BackofficeLoginHistoryResponse toLoginHistoryResponse(Map<String, Object> row) {
        return new BackofficeLoginHistoryResponse(
                longValue(row, "historyId", "history_id", "LOGIN_HISTORY_ID"),
                nullableLong(row, "operatorId", "admin_user_id", "ADMIN_USER_ID"),
                text(row, "loginId", "admin_login_id", "ADMIN_LOGIN_ID"),
                text(row, "successYn", "success_yn", "SUCCESS_YN"),
                text(row, "failureReason", "failure_reason", "FAILURE_REASON"),
                text(row, "clientIp", "client_ip", "CLIENT_IP"),
                text(row, "userAgent", "user_agent", "USER_AGENT"),
                text(row, "transactionId", "transaction_id", "TRANSACTION_ID"),
                text(row, "systemCode", "system_code", "SYSTEM_CODE", "module_id", "MODULE_ID"),
                text(row, "application", "application_name", "APPLICATION_NAME", "was_id", "WAS_ID"),
                text(row, "instanceId", "instance_id", "INSTANCE_ID"),
                instant(row, "createdAt", "created_at", "CREATED_AT"));
    }

    private BackofficeSessionResponse toSessionResponse(Map<String, Object> row) {
        return new BackofficeSessionResponse(
                longValue(row, "sessionId", "refresh_token_id", "REFRESH_TOKEN_ID"),
                longValue(row, "operatorId", "admin_user_id", "ADMIN_USER_ID"),
                text(row, "loginId", "admin_login_id", "ADMIN_LOGIN_ID"),
                text(row, "loginDomain", "login_domain", "LOGIN_DOMAIN"),
                text(row, "transactionId", "transaction_id", "TRANSACTION_ID"),
                text(row, "revokedYn", "revoked_yn", "REVOKED_YN"),
                instant(row, "expiresAt", "expire_at", "EXPIRE_AT"),
                instant(row, "createdAt", "created_at", "CREATED_AT"),
                instant(row, "updatedAt", "updated_at", "UPDATED_AT"));
    }

    private Object value(Map<String, Object> row, String... keys) {
        for (String key : keys) {
            if (row.containsKey(key)) return row.get(key);
            for (Map.Entry<String, Object> entry : row.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) return entry.getValue();
            }
        }
        return null;
    }

    private String text(Map<String, Object> row, String... keys) {
        Object value = value(row, keys);
        return value == null ? null : String.valueOf(value);
    }

    private long longValue(Map<String, Object> row, String... keys) {
        Long value = nullableLong(row, keys);
        return value == null ? 0L : value;
    }

    private Long nullableLong(Map<String, Object> row, String... keys) {
        Object value = value(row, keys);
        if (value == null) return null;
        if (value instanceof Number number) return number.longValue();
        return Long.parseLong(String.valueOf(value));
    }

    private Instant instant(Map<String, Object> row, String... keys) {
        Object value = value(row, keys);
        if (value == null) return null;
        if (value instanceof Instant instant) return instant;
        if (value instanceof java.sql.Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.time.OffsetDateTime offsetDateTime) return offsetDateTime.toInstant();
        if (value instanceof java.time.LocalDateTime localDateTime) return localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant();
        return Instant.parse(String.valueOf(value));
    }

    public record LoginRequest(String loginId, String password, String idempotencyKey) {
        public LoginRequest withIdempotencyKey(String key) { return new LoginRequest(loginId, password, key); }
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
            BackofficeOperatorResponse operator) {
    }
}
