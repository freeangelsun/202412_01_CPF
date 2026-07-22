package com.cpf.member.bse.service;

import com.cpf.common.sec.crypto.CmnCryptoService;
import com.cpf.common.sec.token.CmnJwtCreateRequest;
import com.cpf.common.sec.token.CmnJwtService;
import com.cpf.common.sec.token.CmnJwtValidationResult;
import com.cpf.common.utils.TextUtils;
import com.cpf.member.bse.entity.Member;
import com.cpf.member.bse.mapper.MemberMapper;
import com.cpf.core.common.logging.ServerInstanceIdentity;
import com.cpf.core.common.logging.TransactionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MBR 회원 인증 서비스입니다.
 *
 * <p>회원 계정, 로그인 실패 횟수, 로그인 이력, refresh token hash를 mbrDB에 영속화합니다.
 * 이전 임시 메모리 인증 저장소를 사용하지 않기 때문에 다중 WAS와 재기동 상황에서도 token 폐기 상태를 추적할 수 있습니다.</p>
 */
@Service
@Transactional(transactionManager = "mbrTransactionManager")
public class MbrAuthService extends com.cpf.member.common.base.MbrBaseService {
    private static final String LOGIN_DOMAIN = "MBR";
    private static final String ISSUER = "CPF-MBR";
    private static final String AUDIENCE = "CPF-MBR";

    private final CmnJwtService jwtService;
    private final CmnCryptoService cryptoService;
    private final MemberMapper memberMapper;
    private final String jwtSecret;
    private final long accessTokenTtlSeconds;
    private final long refreshTokenTtlSeconds;
    private final String moduleId;
    private final String wasId;

    public MbrAuthService(
            CmnJwtService jwtService,
            CmnCryptoService cryptoService,
            MemberMapper memberMapper,
            @Value("${cpf.mbr.security.jwt-secret:${CPF_MBR_JWT_SECRET:}}") String jwtSecret,
            @Value("${cpf.mbr.security.access-token-ttl-seconds:600}") long accessTokenTtlSeconds,
            @Value("${cpf.mbr.security.refresh-token-ttl-seconds:7200}") long refreshTokenTtlSeconds,
            @Value("${cpf.framework.module-id:MBR}") String moduleId,
            @Value("${cpf.framework.was-id:local01}") String wasId) {
        this.jwtService = jwtService;
        this.cryptoService = cryptoService;
        this.memberMapper = memberMapper;
        this.jwtSecret = jwtSecret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
        this.moduleId = moduleId;
        this.wasId = wasId;
    }

    /**
     * 회원 로그인을 처리하고 로그인 이력과 refresh token hash를 DB에 저장합니다.
     */
    public LoginResult login(LoginRequest request, String clientIp, String userAgent) {
        String loginId = TextUtils.requireText(request.loginId(), "loginId");
        String password = TextUtils.requireText(request.password(), "password");
        Member member = memberMapper.selectMemberByLoginId(loginId).orElse(null);

        if (member == null) {
            recordLogin((Integer) null, loginId, "FAIL", "회원 없음", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 확인할 수 없습니다.");
        }
        if (!canLogin(member)) {
            recordLogin(member, loginId, "FAIL", "로그인 불가 상태", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 상태상 로그인이 불가합니다.");
        }
        if (!TextUtils.hasText(member.getPasswordHash())) {
            recordLogin(member, loginId, "FAIL", "비밀번호 hash 미등록", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 비밀번호가 초기화되지 않았습니다.");
        }
        if (!cryptoService.pbkdf2Matches(password, member.getPasswordHash())) {
            memberMapper.increaseLoginFailCount(member.getId());
            recordLogin(member, loginId, "FAIL", "비밀번호 불일치", clientIp, userAgent);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 인증에 실패했습니다.");
        }

        memberMapper.markLoginSuccess(member.getId());
        recordLogin(member, loginId, "SUCCESS", null, clientIp, userAgent);

        String accessToken = createAccessToken(member);
        String refreshToken = cryptoService.secureRandomToken(48);
        String refreshHash = cryptoService.sha256Base64Url(refreshToken);
        Instant refreshExpireAt = Instant.now().plusSeconds(refreshTokenTtlSeconds);
        memberMapper.insertRefreshToken(refreshTokenRow(member, refreshHash, refreshExpireAt));
        return new LoginResult(accessToken, refreshToken, "Bearer", accessTokenTtlSeconds, refreshExpireAt, toMemberResponse(member));
    }

    /**
     * DB에 저장된 refresh token hash를 검증한 뒤 access token을 재발급합니다.
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public LoginResult refresh(RefreshRequest request) {
        String refreshToken = TextUtils.requireText(request.refreshToken(), "refreshToken");
        Map<String, Object> state = memberMapper.selectRefreshTokenByHash(cryptoService.sha256Base64Url(refreshToken));
        if (state == null || isRevoked(state) || expireAt(state).isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token이 유효하지 않습니다.");
        }
        String loginId = String.valueOf(state.get("loginId"));
        Member member = memberMapper.selectMemberByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 상태가 유효하지 않습니다."));
        if (!canLogin(member)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 상태가 유효하지 않습니다.");
        }
        return new LoginResult(createAccessToken(member), null, "Bearer", accessTokenTtlSeconds, expireAt(state), toMemberResponse(member));
    }

    /**
     * refresh token을 DB에서 폐기합니다.
     */
    public Map<String, Object> logout(RefreshRequest request) {
        if (request != null && TextUtils.hasText(request.refreshToken())) {
            memberMapper.revokeRefreshTokenByHash(cryptoService.sha256Base64Url(request.refreshToken()));
        }
        return Map.of("logoutYn", "Y", "loginDomain", LOGIN_DOMAIN);
    }

    /**
     * MBR access token을 검증하고 현재 회원 정보를 DB에서 다시 조회합니다.
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public Map<String, Object> currentMember(String authorizationHeader) {
        CmnJwtValidationResult result = validateAccessToken(authorizationHeader);
        String loginId = String.valueOf(result.claims().get("loginId"));
        Member member = memberMapper.selectMemberByLoginId(loginId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "회원 정보를 찾을 수 없습니다."));
        Map<String, Object> response = new LinkedHashMap<>(toMemberResponse(member));
        response.put("loginDomain", LOGIN_DOMAIN);
        response.put("tokenExpiresAt", result.expiresAt());
        return response;
    }

    /**
     * 최신 로그인 이력을 DB에서 조회합니다.
     */
    @Transactional(transactionManager = "mbrTransactionManager", readOnly = true)
    public List<Map<String, Object>> loginHistories(int limit) {
        return memberMapper.selectLoginHistories(Math.max(1, Math.min(limit, 500)));
    }

    private String createAccessToken(Member member) {
        requireJwtSecret();
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("loginDomain", LOGIN_DOMAIN);
        claims.put("memberNo", member.getMemberNo());
        claims.put("customerNo", member.getCustomerNo());
        claims.put("loginId", member.getLoginId());
        claims.put("memberStatus", member.getMemberStatus());
        claims.put("moduleId", moduleId);
        claims.put("wasId", wasId);
        claims.put("serverInstanceId", ServerInstanceIdentity.current().serverInstanceId());
        return jwtService.createHs256Token(new CmnJwtCreateRequest(
                ISSUER,
                member.getMemberNo(),
                AUDIENCE,
                accessTokenTtlSeconds,
                jwtSecret,
                claims));
    }

    private CmnJwtValidationResult validateAccessToken(String authorizationHeader) {
        requireJwtSecret();
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

    /** 운영 환경에서 짧거나 누락된 JWT 비밀키로 토큰이 발급되는 것을 차단합니다. */
    private void requireJwtSecret() {
        if (jwtSecret == null || jwtSecret.length() < 32 || jwtSecret.startsWith("__REPLACE_")) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "MBR JWT secret은 32자 이상 운영 환경변수로 설정해야 합니다.");
        }
    }

    private boolean canLogin(Member member) {
        return ("ACTIVE".equals(member.getMemberStatus()) || "DORMANT".equals(member.getMemberStatus()))
                && !"Y".equals(member.getLockYn())
                && !"Y".equals(member.getWithdrawYn());
    }

    private void recordLogin(
            Member member,
            String loginId,
            String result,
            String failureReason,
            String clientIp,
            String userAgent) {
        Integer memberId = member == null ? null : member.getId();
        String memberNo = member == null ? null : member.getMemberNo();
        String customerNo = member == null ? null : member.getCustomerNo();
        recordLogin(memberId, memberNo, customerNo, loginId, result, failureReason, clientIp, userAgent);
    }

    private void recordLogin(
            Integer memberId,
            String loginId,
            String result,
            String failureReason,
            String clientIp,
            String userAgent) {
        recordLogin(memberId, null, null, loginId, result, failureReason, clientIp, userAgent);
    }

    private void recordLogin(
            Integer memberId,
            String memberNo,
            String customerNo,
            String loginId,
            String result,
            String failureReason,
            String clientIp,
            String userAgent) {
        ServerInstanceIdentity.Identity identity = ServerInstanceIdentity.current();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("memberId", memberId);
        row.put("loginDomain", LOGIN_DOMAIN);
        row.put("memberNo", memberNo);
        row.put("customerNo", customerNo);
        row.put("loginId", loginId);
        row.put("loginResult", result);
        row.put("loginIp", clientIp);
        row.put("userAgent", userAgent);
        row.put("failureReason", failureReason);
        row.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        row.put("moduleId", moduleId);
        row.put("wasId", wasId);
        row.put("serverInstanceId", identity.serverInstanceId());
        memberMapper.insertMemberLoginHistory(row);
    }

    private Map<String, Object> refreshTokenRow(Member member, String refreshHash, Instant refreshExpireAt) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("memberId", member.getId());
        row.put("memberNo", member.getMemberNo());
        row.put("loginDomain", LOGIN_DOMAIN);
        row.put("refreshTokenHash", refreshHash);
        row.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        row.put("expireAt", Timestamp.from(refreshExpireAt));
        return row;
    }

    private boolean isRevoked(Map<String, Object> state) {
        return "Y".equals(String.valueOf(state.get("revokedYn")));
    }

    private Instant expireAt(Map<String, Object> state) {
        Object value = state.get("expireAt");
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "refresh token 만료 일시를 확인할 수 없습니다.");
    }

    private Map<String, Object> toMemberResponse(Member member) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("memberId", member.getId());
        response.put("memberNo", member.getMemberNo());
        response.put("customerNo", member.getCustomerNo());
        response.put("loginId", member.getLoginId());
        response.put("memberName", member.getName());
        response.put("memberStatus", member.getMemberStatus());
        response.put("lockYn", member.getLockYn());
        response.put("withdrawYn", member.getWithdrawYn());
        response.put("failCount", member.getLoginFailCount());
        response.put("passwordChangeRequiredYn", member.getPasswordChangeRequiredYn());
        response.put("passwordExpireAt", member.getPasswordExpireAt());
        response.put("lastLoginAt", member.getLastLoginAt());
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
            Map<String, Object> member) {
    }
}
