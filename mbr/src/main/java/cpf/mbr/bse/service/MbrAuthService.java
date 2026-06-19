package cpf.mbr.bse.service;

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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MBR 회원 인증 기본 구현체입니다.
 *
 * <p>현재 단계에서는 로컬 기본 회원 저장소로 로그인 흐름을 제공하고, JWT/refresh token/hash/로그인 이력의
 * 표준 흐름을 먼저 고정합니다. 실제 회원 DB 인증으로 확장할 때도 token domain과 이력 필드는 그대로 유지합니다.</p>
 */
@Service
public class MbrAuthService {
    private static final String LOGIN_DOMAIN = "MBR";
    private static final String ISSUER = "CPF-MBR";
    private static final String AUDIENCE = "CPF-MBR";

    private final CmnJwtService jwtService;
    private final CmnCryptoService cryptoService;
    private final String jwtSecret;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String moduleId;
    private final String wasId;
    private final Map<String, MemberAccount> members = new ConcurrentHashMap<>();
    private final Map<String, RefreshTokenState> refreshTokensByHash = new ConcurrentHashMap<>();
    private final List<LoginHistoryRow> loginHistories = new CopyOnWriteArrayList<>();

    public MbrAuthService(
            CmnJwtService jwtService,
            CmnCryptoService cryptoService,
            @Value("${cpf.mbr.security.jwt-secret:${CPF_MBR_JWT_SECRET:local-mbr-education-secret-change-me}}") String jwtSecret,
            @Value("${cpf.mbr.security.access-token-ttl-seconds:600}") long accessTokenTtlSeconds,
            @Value("${cpf.mbr.security.refresh-token-ttl-seconds:7200}") long refreshTokenTtlSeconds,
            @Value("${cpf.framework.module-id:MBR}") String moduleId,
            @Value("${cpf.framework.was-id:local01}") String wasId) {
        this.jwtService = jwtService;
        this.cryptoService = cryptoService;
        this.jwtSecret = jwtSecret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.moduleId = moduleId;
        this.wasId = wasId;
        seedLocalMember();
    }

    /**
     * 회원 로그인을 처리합니다. 잠금/탈퇴/중지 상태는 token 발급 전에 차단합니다.
     */
    public LoginResult login(LoginRequest request, String clientIp, String userAgent) {
        String loginId = TextUtils.requireText(request.loginId(), "loginId");
        String password = TextUtils.requireText(request.password(), "password");
        MemberAccount member = members.get(loginId);
        if (member == null) {
            recordLogin(null, null, loginId, "FAIL", "회원 없음", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 확인할 수 없습니다.");
        }
        if (!member.canLogin()) {
            recordLogin(member.memberNo(), member.customerNo(), loginId, "FAIL", "로그인 불가 상태", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 상태상 로그인이 불가합니다.");
        }
        if (!cryptoService.pbkdf2Matches(password, member.passwordHash())) {
            member.increaseFailCount();
            recordLogin(member.memberNo(), member.customerNo(), loginId, "FAIL", "비밀번호 불일치", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 인증에 실패했습니다.");
        }

        member.resetFailCount();
        member.updateLastLoginAt(Instant.now());
        recordLogin(member.memberNo(), member.customerNo(), loginId, "SUCCESS", null, clientIp, userAgent);

        String accessToken = createAccessToken(member);
        String refreshToken = cryptoService.secureRandomToken(48);
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        Instant refreshExpireAt = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        refreshTokensByHash.put(refreshHash, new RefreshTokenState(
                refreshHash,
                member.memberNo(),
                member.customerNo(),
                loginId,
                LOGIN_DOMAIN,
                refreshExpireAt,
                false,
                TransactionContext.getOrCreateTransactionId()));
        return new LoginResult(accessToken, refreshToken, "Bearer", accessTokenTtlSeconds, refreshExpireAt, member.toResponse());
    }

    /**
     * 회원 refresh token을 hash로 검증한 뒤 access token을 재발급합니다.
     */
    public LoginResult refresh(RefreshRequest request) {
        String refreshToken = TextUtils.requireText(request.refreshToken(), "refreshToken");
        RefreshTokenState state = refreshTokensByHash.get(cryptoService.sha256Base64Url(refreshToken));
        if (state == null || state.revoked() || state.expiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
        }
        MemberAccount member = members.get(state.loginId());
        if (member == null || !member.canLogin()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 상태가 유효하지 않습니다.");
        }
        return new LoginResult(createAccessToken(member), null, "Bearer", accessTokenTtlSeconds, state.expiresAt(), member.toResponse());
    }

    /**
     * refresh token을 폐기합니다.
     */
    public Map<String, Object> logout(RefreshRequest request) {
        if (request != null && TextUtils.hasText(request.refreshToken())) {
            String hash = cryptoService.sha256Base64Url(request.refreshToken());
            RefreshTokenState state = refreshTokensByHash.get(hash);
            if (state != null) {
                refreshTokensByHash.put(hash, state.revoke());
            }
        }
        return Map.of("logoutYn", "Y", "loginDomain", LOGIN_DOMAIN);
    }

    /**
     * MBR access token을 검증하고 현재 회원 정보를 반환합니다.
     */
    public Map<String, Object> currentMember(String authorizationHeader) {
        CmnJwtValidationResult result = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(result.claims().get("loginId"));
        MemberAccount member = members.get(loginId);
        if (member == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다.");
        }
        Map<String, Object> response = new LinkedHashMap<>(member.toResponse());
        response.put("loginDomain", LOGIN_DOMAIN);
        response.put("tokenExpiresAt", result.expiresAt());
        return response;
    }

    public List<Map<String, Object>> loginHistories(int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        return loginHistories.stream()
                .sorted(Comparator.comparing(LoginHistoryRow::occurredAt).reversed())
                .limit(resolvedLimit)
                .map(LoginHistoryRow::toResponse)
                .toList();
    }

    private void seedLocalMember() {
        MemberAccount member = new MemberAccount(
                "M000000001",
                "C000000001",
                "mbr001",
                "샘플 회원",
                "NORMAL",
                "ACTIVE",
                "N",
                "N",
                cryptoService.pbkdf2Hash("Member!2345"),
                0,
                null);
        members.put(member.loginId(), member);
    }

    private String createAccessToken(MemberAccount member) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("loginDomain", LOGIN_DOMAIN);
        claims.put("memberNo", member.memberNo());
        claims.put("customerNo", member.customerNo());
        claims.put("loginId", member.loginId());
        claims.put("memberGrade", member.memberGrade());
        claims.put("memberStatus", member.memberStatus());
        claims.put("moduleId", moduleId);
        claims.put("wasId", wasId);
        claims.put("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        return jwtService.createHs256Token(new CmnJwtCreateRequest(
                ISSUER,
                member.memberNo(),
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
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "MBR token이 아닙니다.");
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
            String memberNo,
            String customerNo,
            String loginId,
            String result,
            String failureReason,
            String clientIp,
            String userAgent) {
        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        loginHistories.add(new LoginHistoryRow(
                loginHistories.size() + 1L,
                LOGIN_DOMAIN,
                memberNo,
                customerNo,
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
            Map<String, Object> member) {
    }

    private record RefreshTokenState(
            String refreshTokenHash,
            String memberNo,
            String customerNo,
            String loginId,
            String loginDomain,
            Instant expiresAt,
            boolean revoked,
            String transactionGlobalId) {
        private RefreshTokenState revoke() {
            return new RefreshTokenState(refreshTokenHash, memberNo, customerNo, loginId, loginDomain, expiresAt, true, transactionGlobalId);
        }
    }

    private static final class MemberAccount {
        private final String memberNo;
        private final String customerNo;
        private final String loginId;
        private final String memberName;
        private final String memberGrade;
        private final String memberStatus;
        private final String lockYn;
        private final String withdrawYn;
        private final String passwordHash;
        private int failCount;
        private Instant lastLoginAt;

        private MemberAccount(
                String memberNo,
                String customerNo,
                String loginId,
                String memberName,
                String memberGrade,
                String memberStatus,
                String lockYn,
                String withdrawYn,
                String passwordHash,
                int failCount,
                Instant lastLoginAt) {
            this.memberNo = memberNo;
            this.customerNo = customerNo;
            this.loginId = loginId;
            this.memberName = memberName;
            this.memberGrade = memberGrade;
            this.memberStatus = memberStatus;
            this.lockYn = lockYn;
            this.withdrawYn = withdrawYn;
            this.passwordHash = passwordHash;
            this.failCount = failCount;
            this.lastLoginAt = lastLoginAt;
        }

        private boolean canLogin() {
            return ("ACTIVE".equals(memberStatus) || "DORMANT".equals(memberStatus))
                    && !"Y".equals(lockYn)
                    && !"Y".equals(withdrawYn);
        }

        private String memberNo() {
            return memberNo;
        }

        private String customerNo() {
            return customerNo;
        }

        private String loginId() {
            return loginId;
        }

        private String memberGrade() {
            return memberGrade;
        }

        private String memberStatus() {
            return memberStatus;
        }

        private String passwordHash() {
            return passwordHash;
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
            response.put("memberNo", memberNo);
            response.put("customerNo", customerNo);
            response.put("loginId", loginId);
            response.put("memberName", memberName);
            response.put("memberGrade", memberGrade);
            response.put("memberStatus", memberStatus);
            response.put("lockYn", lockYn);
            response.put("withdrawYn", withdrawYn);
            response.put("failCount", failCount);
            response.put("lastLoginAt", lastLoginAt);
            return response;
        }
    }

    private record LoginHistoryRow(
            long historyId,
            String loginDomain,
            String memberNo,
            String customerNo,
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
            response.put("memberNo", memberNo);
            response.put("customerNo", customerNo);
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
