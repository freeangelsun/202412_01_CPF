package com.cpf.admin.opr.service;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.config.AdmSecurityProperties;
import com.cpf.admin.opr.dto.AdmLoginResponse;
import com.cpf.admin.opr.dto.AdmMenu;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.admin.opr.dto.AdmSession;
import com.cpf.admin.opr.dto.AdmSessionSummaryResponse;
import com.cpf.common.sec.crypto.CmnCryptoService;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.reliability.CpfReliabilityOperationsPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ADM Session의 발급·검증·폐기를 담당합니다.
 *
 * <p><b>DATABASE 정책:</b> DB가 유일한 인증 정본입니다. 발급은 DB commit 이후 token을 반환하고, 조회·폐기·정리
 * 중 DB 장애는 미인증/성공으로 축소하지 않고 503 계열 운영 오류로 fail-closed 합니다. 명시적인 MEMORY 정책은
 * test/local 격리 환경에서만 별도 사용합니다.</p>
 * <p><b>다중 인스턴스:</b> 요청마다 DB의 현재 계정상태/Role/폐기 상태를 확인하므로 WAS 로컬 메모리 cache로
 * 권한을 유지하지 않습니다. 상태·Role·비밀번호 변경은 같은 DB Transaction 책임에서 관련 Session을 무효화합니다.</p>
 * <p><b>복구:</b> Session 폐기 결과불명은 token 원문 없이 CPF Unknown Result에 기록하고 기존 Reliability 운영 API의
 * RETRY_PENDING으로 재폐기할 수 있습니다. Readiness는 sessionStore 장애를 별도 reasonCode로 노출합니다.</p>
 * <p><b>Thread Safety/보안:</b> Service는 공유 mutable 인증 정본을 보유하지 않으며 원문 token을 로그/Evidence/재처리
 * payload에 저장하지 않습니다.</p>
 */
@Service
public class AdmSessionService extends com.cpf.admin.common.base.AdmBaseService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SESSION_LIST_LIMIT = 500;

    private final AdmSecurityProperties properties;
    private final JdbcTemplate admJdbcTemplate;
    private final CmnCryptoService cryptoService;
    private final AdmPersistencePolicy persistencePolicy;
    private final ConcurrentMap<String, AdmSession> memorySessions = new ConcurrentHashMap<>();
    private volatile CpfReliabilityOperationsPort reliabilityOperationsPort;

    public AdmSessionService(
            AdmSecurityProperties properties,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            CmnCryptoService cryptoService,
            AdmPersistencePolicy persistencePolicy) {
        this.properties = properties;
        this.admJdbcTemplate = admJdbcTemplate;
        this.cryptoService = cryptoService;
        this.persistencePolicy = persistencePolicy;
    }

    /** CPF reliability runtime이 존재할 때 Session revoke 결과불명 기록에 연결합니다. */
    @Autowired(required = false)
    void setReliabilityOperationsPort(CpfReliabilityOperationsPort port) {
        this.reliabilityOperationsPort = port;
    }

    /**
     * 인증된 운영자에게 bearer 세션을 발급합니다.
     *
     * @throws CpfBusinessException DATABASE 모드에서 세션 저장에 실패한 경우. 이 경우 token은 응답되지 않습니다.
     */
    public AdmLoginResponse issue(AdmOperator operator, List<AdmMenu> menus, List<String> buttonIds) {
        String token = newToken();
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusSeconds(properties.getSessionTtlSeconds());
        AdmSession session = new AdmSession(
                token,
                operator.operatorId(),
                operator.roleIds(),
                operator.passwordChangeRequired() || operator.passwordExpired(),
                issuedAt,
                expiresAt);

        if (persistencePolicy.memoryEnabled()) {
            memorySessions.put(token, session);
        } else {
            persistSession(session);
        }
        return new AdmLoginResponse(token, "Bearer", properties.getSessionTtlSeconds(), operator, menus, buttonIds);
    }

    /**
     * bearer token의 현재 유효성을 확인합니다.
     *
     * <p>DATABASE 모드에서는 세션 row뿐 아니라 현재 운영자 {@code USE_YN}, {@code ACCOUNT_STATUS},
     * {@code LOCKED_YN}과 현재 role mapping을 함께 확인합니다.</p>
     *
     * @throws CpfBusinessException DATABASE 모드에서 세션 저장소를 조회할 수 없는 경우
     */
    public Optional<AdmSession> findValidSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (persistencePolicy.memoryEnabled()) {
            AdmSession session = memorySessions.get(token);
            if (session == null) {
                return Optional.empty();
            }
            if (!session.expiresAt().isAfter(LocalDateTime.now())) {
                memorySessions.remove(token, session);
                return Optional.empty();
            }
            return Optional.of(session);
        }
        return findDbSession(token);
    }

    /**
     * 로그아웃 또는 강제 폐기 요청으로 세션을 폐기합니다.
     * DATABASE 모드의 DB 실패는 성공으로 반환하지 않습니다.
     */
    public void revoke(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        if (persistencePolicy.memoryEnabled()) {
            memorySessions.remove(token);
            return;
        }
        revokeDbSession(token);
    }

    /**
     * 운영자의 모든 세션을 폐기합니다. 비밀번호/role/계정 상태 변경 Transaction에서 호출됩니다.
     */
    public int revokeOperatorSessions(String operatorId) {
        if (operatorId == null || operatorId.isBlank()) {
            return 0;
        }
        if (persistencePolicy.memoryEnabled()) {
            int revoked = 0;
            for (Map.Entry<String, AdmSession> entry : memorySessions.entrySet()) {
                if (operatorId.equals(entry.getValue().operatorId())
                        && memorySessions.remove(entry.getKey(), entry.getValue())) {
                    revoked++;
                }
            }
            return revoked;
        }
        try {
            return admJdbcTemplate.update("""
                    UPDATE adm_operator_session
                       SET REVOKED_YN = 'Y',
                           UPDATED_BY = ?,
                           UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE OPERATOR_ID = ?
                       AND REVOKED_YN = 'N'
                    """, operatorId, operatorId);
        } catch (DataAccessException ex) {
            recordRevocationUnknown("OPERATOR_ID", operatorId, "adm_operator_session.revokeOperator", ex);
            throw unavailable("adm_operator_session.revokeOperator", ex);
        }
    }

    /** 운영자가 확인할 수 있는 세션 목록입니다. DB 장애는 빈 목록으로 위장하지 않습니다. */
    public List<AdmSessionSummaryResponse> findSessions(String operatorId) {
        if (persistencePolicy.memoryEnabled()) {
            return memorySessions.values().stream()
                    .filter(session -> operatorId == null || operatorId.isBlank() || session.operatorId().equals(operatorId))
                    .limit(SESSION_LIST_LIMIT)
                    .map(session -> new AdmSessionSummaryResponse(
                            "IN_MEMORY", session.operatorId(), session.roleIds(), session.issuedAt(), session.expiresAt(),
                            false, null, null, session.issuedAt(), session.issuedAt()))
                    .toList();
        }
        try {
            String sql = operatorId != null && !operatorId.isBlank() ? """
                    SELECT SESSION_ID, OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT,
                           REVOKED_YN, CLIENT_IP, USER_AGENT, CREATED_AT, UPDATED_AT
                      FROM adm_operator_session
                     WHERE OPERATOR_ID = ?
                     ORDER BY EXPIRE_AT DESC
                    """ : """
                    SELECT SESSION_ID, OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT,
                           REVOKED_YN, CLIENT_IP, USER_AGENT, CREATED_AT, UPDATED_AT
                      FROM adm_operator_session
                     ORDER BY EXPIRE_AT DESC
                    """;
            Object[] args = operatorId != null && !operatorId.isBlank() ? new Object[]{operatorId.trim()} : new Object[0];
            return admJdbcTemplate.query(sql, (rs, rowNum) -> new AdmSessionSummaryResponse(
                    rs.getString("SESSION_ID"), rs.getString("OPERATOR_ID"), splitRoles(rs.getString("ROLE_IDS")),
                    localDateTime(rs.getTimestamp("ISSUED_AT")), localDateTime(rs.getTimestamp("EXPIRE_AT")),
                    "Y".equals(rs.getString("REVOKED_YN")), rs.getString("CLIENT_IP"), rs.getString("USER_AGENT"),
                    localDateTime(rs.getTimestamp("CREATED_AT")), localDateTime(rs.getTimestamp("UPDATED_AT"))), args)
                    .stream().limit(SESSION_LIST_LIMIT).toList();
        } catch (DataAccessException ex) {
            throw unavailable("adm_operator_session.list", ex);
        }
    }

    public int revokeSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return 0;
        }
        if (persistencePolicy.memoryEnabled()) {
            int before = memorySessions.size();
            memorySessions.entrySet().removeIf(entry -> sessionId.equals(entry.getKey()));
            return before - memorySessions.size();
        }
        try {
            return admJdbcTemplate.update("""
                    UPDATE adm_operator_session
                       SET REVOKED_YN = 'Y',
                           UPDATED_BY = 'ADM',
                           UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE SESSION_ID = ?
                       AND REVOKED_YN = 'N'
                    """, sessionId);
        } catch (DataAccessException ex) {
            recordRevocationUnknown("SESSION_ID", sessionId, "adm_operator_session.revokeById", ex);
            throw unavailable("adm_operator_session.revokeById", ex);
        }
    }

    public int cleanupExpiredSessions() {
        if (persistencePolicy.memoryEnabled()) {
            int before = memorySessions.size();
            memorySessions.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(LocalDateTime.now()));
            return before - memorySessions.size();
        }
        try {
            return admJdbcTemplate.update("""
                    UPDATE adm_operator_session
                       SET REVOKED_YN = 'Y',
                           UPDATED_BY = 'ADM',
                           UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE REVOKED_YN = 'N'
                       AND EXPIRE_AT <= CURRENT_TIMESTAMP
                    """);
        } catch (DataAccessException ex) {
            recordRevocationUnknown("CLEANUP", "EXPIRED", "adm_operator_session.cleanup", ex);
            throw unavailable("adm_operator_session.cleanup", ex);
        }
    }

    private List<String> splitRoles(String value) {
        if (value == null || value.isBlank()) return List.of();
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim).filter(role -> !role.isBlank()).distinct().sorted().toList();
    }

    private LocalDateTime localDateTime(java.sql.Timestamp value) {
        return value == null ? null : value.toLocalDateTime();
    }

    private void persistSession(AdmSession session) {
        try {
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator_session (
                        SESSION_ID, TOKEN_HASH, OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT,
                        REVOKED_YN, CREATED_BY, UPDATED_BY
                    ) VALUES (?, ?, ?, ?, ?, ?, 'N', ?, ?)
                    """,
                    UUID.randomUUID().toString(),
                    tokenHash(session.token()),
                    session.operatorId(),
                    String.join(",", session.roleIds()),
                    session.issuedAt(),
                    session.expiresAt(),
                    session.operatorId(),
                    session.operatorId());
        } catch (DataAccessException ex) {
            throw unavailable("adm_operator_session.issue", ex);
        }
    }

    private Optional<AdmSession> findDbSession(String token) {
        try {
            Optional<SessionRow> stored = admJdbcTemplate.query("""
                            SELECT s.OPERATOR_ID, s.ISSUED_AT, s.EXPIRE_AT,
                                   o.PASSWORD_CHANGE_REQUIRED_YN
                              FROM adm_operator_session s
                              JOIN adm_operator o ON o.OPERATOR_ID = s.OPERATOR_ID
                             WHERE s.TOKEN_HASH = ?
                               AND s.REVOKED_YN = 'N'
                               AND s.EXPIRE_AT > CURRENT_TIMESTAMP
                               AND o.USE_YN = 'Y'
                               AND o.ACCOUNT_STATUS = 'ACTIVE'
                               AND o.LOCKED_YN = 'N'
                            """,
                    rs -> {
                        if (!rs.next()) {
                            return Optional.empty();
                        }
                        return Optional.of(new SessionRow(
                                rs.getString("OPERATOR_ID"),
                                "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                                rs.getTimestamp("ISSUED_AT").toLocalDateTime(),
                                rs.getTimestamp("EXPIRE_AT").toLocalDateTime()));
                    }, tokenHash(token));
            if (stored.isEmpty()) {
                return Optional.empty();
            }
            SessionRow row = stored.get();
            List<String> currentRoles = admJdbcTemplate.queryForList("""
                    SELECT r.ROLE_ID
                      FROM adm_operator_role r
                      JOIN adm_role role ON role.ROLE_ID = r.ROLE_ID
                     WHERE r.OPERATOR_ID = ?
                       AND role.USE_YN = 'Y'
                     ORDER BY r.ROLE_ID
                    """, String.class, row.operatorId());
            return Optional.of(new AdmSession(
                    token,
                    row.operatorId(),
                    List.copyOf(currentRoles),
                    row.passwordChangeRequired(),
                    row.issuedAt(),
                    row.expiresAt()));
        } catch (DataAccessException ex) {
            throw unavailable("adm_operator_session.lookup", ex);
        }
    }

    private void revokeDbSession(String token) {
        try {
            admJdbcTemplate.update("""
                    UPDATE adm_operator_session
                       SET REVOKED_YN = 'Y',
                           UPDATED_BY = 'ADM',
                           UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE TOKEN_HASH = ?
                       AND REVOKED_YN = 'N'
                    """, tokenHash(token));
        } catch (DataAccessException ex) {
            recordRevocationUnknown("TOKEN_HASH", tokenHash(token), "adm_operator_session.revoke", ex);
            throw unavailable("adm_operator_session.revoke", ex);
        }
    }

    /** UNKNOWN_RESULT 운영 재처리에서 token 원문 없이 Session 폐기를 다시 실행합니다. */
    public int retryPendingRevocation(String externalKey) {
        if (persistencePolicy.memoryEnabled()) throw new IllegalStateException("MEMORY 모드에는 DB Session revoke 재처리가 필요하지 않습니다.");
        if (externalKey == null || !externalKey.contains(":")) throw new IllegalArgumentException("Session revoke externalKey 형식이 올바르지 않습니다.");
        String[] parts = externalKey.split(":", 2);
        return switch (parts[0]) {
            case "TOKEN_HASH" -> admJdbcTemplate.update("""
                    UPDATE adm_operator_session SET REVOKED_YN='Y', UPDATED_BY='ADM_RETRY', UPDATED_AT=CURRENT_TIMESTAMP
                    WHERE TOKEN_HASH=? AND REVOKED_YN='N'
                    """, parts[1]);
            case "SESSION_ID" -> admJdbcTemplate.update("""
                    UPDATE adm_operator_session SET REVOKED_YN='Y', UPDATED_BY='ADM_RETRY', UPDATED_AT=CURRENT_TIMESTAMP
                    WHERE SESSION_ID=? AND REVOKED_YN='N'
                    """, parts[1]);
            case "OPERATOR_ID" -> admJdbcTemplate.update("""
                    UPDATE adm_operator_session SET REVOKED_YN='Y', UPDATED_BY='ADM_RETRY', UPDATED_AT=CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID=? AND REVOKED_YN='N'
                    """, parts[1]);
            case "CLEANUP" -> cleanupExpiredSessionsWithoutUnknown();
            default -> throw new IllegalArgumentException("지원하지 않는 Session revoke 재처리 유형입니다. type=" + parts[0]);
        };
    }

    private int cleanupExpiredSessionsWithoutUnknown() {
        return admJdbcTemplate.update("""
                UPDATE adm_operator_session SET REVOKED_YN='Y', UPDATED_BY='ADM_RETRY', UPDATED_AT=CURRENT_TIMESTAMP
                WHERE REVOKED_YN='N' AND EXPIRE_AT <= CURRENT_TIMESTAMP
                """);
    }

    private void recordRevocationUnknown(String keyType, String keyValue, String component, DataAccessException source) {
        CpfReliabilityOperationsPort port = reliabilityOperationsPort;
        if (port == null) return;
        try {
            String unknownId = "ADMSESS-" + UUID.randomUUID();
            port.recordUnknownResult(new CpfReliabilityOperationsPort.UnknownResultCommand(
                    unknownId, "ADM_SESSION_REVOKE", CpfTransactionContext.transactionId(), null,
                    keyType + ":" + keyValue, "SESSION_STORE_UNAVAILABLE",
                    component + " failed: " + source.getClass().getSimpleName(),
                    "RETRY_ADM_SESSION_REVOKE", "ADM"));
        } catch (RuntimeException reliabilityFailure) {
            source.addSuppressed(reliabilityFailure);
        }
    }

    private CpfBusinessException unavailable(String component, DataAccessException ex) {
        return new CpfBusinessException(
                CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "ADM 필수 Session Store를 사용할 수 없습니다.",
                Map.of("0", component, "1", ex.getClass().getSimpleName()));
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String tokenHash(String token) {
        return cryptoService.sha256Hex(token);
    }

    private record SessionRow(
            String operatorId,
            boolean passwordChangeRequired,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt) {
    }
}
