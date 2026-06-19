package cpf.adm.opr.service;

import cpf.adm.config.AdmSecurityProperties;
import cpf.adm.opr.dto.AdmLoginResponse;
import cpf.adm.opr.dto.AdmMenu;
import cpf.adm.opr.dto.AdmOperator;
import cpf.adm.opr.dto.AdmSession;
import cpf.cmn.sec.crypto.CmnCryptoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ADM 운영자 세션을 발급, 조회, 폐기, 정리하는 서비스입니다.
 *
 * <p>세션 token 원문은 운영 화면이나 DB에 노출하지 않고 hash로 저장합니다. DB가 준비된 환경에서는
 * adm_operator_session을 기준으로 조회하고, 로컬 초기 기동처럼 DB가 아직 준비되지 않은 경우에는
 * 메모리 fallback으로 ADM 화면 확인이 가능하도록 합니다.</p>
 */
@Service
public class AdmSessionService {
    private static final Logger log = LoggerFactory.getLogger(AdmSessionService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AdmSecurityProperties properties;
    private final JdbcTemplate admJdbcTemplate;
    private final CmnCryptoService cryptoService;
    private final ConcurrentMap<String, AdmSession> sessions = new ConcurrentHashMap<>();

    public AdmSessionService(
            AdmSecurityProperties properties,
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            CmnCryptoService cryptoService) {
        this.properties = properties;
        this.admJdbcTemplate = admJdbcTemplate;
        this.cryptoService = cryptoService;
    }

    /**
     * 인증된 운영자에게 ADM bearer 세션을 발급합니다.
     *
     * <p>발급된 token은 응답으로 한 번만 전달하고, 저장소에는 hash와 역할 목록, 만료 시각을 기록합니다.</p>
     *
     * @param operator 인증된 운영자
     * @param menus 운영자 역할에 허용된 ADM 메뉴 목록
     * @return 로그인 응답
     */
    public AdmLoginResponse issue(AdmOperator operator, List<AdmMenu> menus) {
        String token = newToken();
        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiresAt = issuedAt.plusSeconds(properties.getSessionTtlSeconds());
        AdmSession session = new AdmSession(token, operator.operatorId(), operator.roleIds(), issuedAt, expiresAt);
        sessions.put(token, session);
        persistSession(session);
        return new AdmLoginResponse(token, "Bearer", properties.getSessionTtlSeconds(), operator, menus);
    }

    /**
     * 요청 token이 유효한 ADM 세션인지 확인합니다.
     *
     * <p>메모리 캐시에서 먼저 찾고, 없으면 token hash로 DB 세션을 조회합니다. 만료된 세션은 즉시 폐기
     * 처리하여 같은 token이 다시 사용되지 않게 합니다.</p>
     *
     * @param token ADM bearer token 원문
     * @return 유효 세션
     */
    public Optional<AdmSession> findValidSession(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        AdmSession session = sessions.get(token);
        if (session == null) {
            return findDbSession(token);
        }
        if (session.expiresAt().isBefore(LocalDateTime.now())) {
            sessions.remove(token);
            revokeDbSession(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    /**
     * 로그아웃 또는 강제 폐기 요청으로 세션을 폐기합니다.
     *
     * @param token 폐기할 ADM bearer token 원문
     */
    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
            revokeDbSession(token);
        }
    }

    public List<Map<String, Object>> findSessions(String operatorId) {
        try {
            if (operatorId != null && !operatorId.isBlank()) {
                return admJdbcTemplate.queryForList("""
                        SELECT SESSION_ID, OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT,
                               REVOKED_YN, CLIENT_IP, USER_AGENT, CREATED_AT, UPDATED_AT
                        FROM adm_operator_session
                        WHERE OPERATOR_ID = ?
                        ORDER BY EXPIRE_AT DESC
                        LIMIT 500
                        """, operatorId.trim());
            }
            return admJdbcTemplate.queryForList("""
                    SELECT SESSION_ID, OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT,
                           REVOKED_YN, CLIENT_IP, USER_AGENT, CREATED_AT, UPDATED_AT
                    FROM adm_operator_session
                    ORDER BY EXPIRE_AT DESC
                    LIMIT 500
                    """);
        } catch (DataAccessException ex) {
            log.debug("ADM session DB list skipped. reason={}", ex.getMessage());
            return sessions.values().stream()
                    .filter(session -> operatorId == null || operatorId.isBlank() || session.operatorId().equals(operatorId))
                    .map(session -> Map.<String, Object>of(
                            "SESSION_ID", "IN_MEMORY",
                            "OPERATOR_ID", session.operatorId(),
                            "ROLE_IDS", String.join(",", session.roleIds()),
                            "ISSUED_AT", session.issuedAt(),
                            "EXPIRE_AT", session.expiresAt(),
                            "REVOKED_YN", "N"))
                    .toList();
        }
    }

    public int revokeSession(String sessionId) {
        try {
            return admJdbcTemplate.update("""
                    UPDATE adm_operator_session
                    SET REVOKED_YN = 'Y',
                        UPDATED_BY = 'ADM',
                        UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE SESSION_ID = ?
                    """, sessionId);
        } catch (DataAccessException ex) {
            log.debug("ADM session DB revoke by id skipped. sessionId={}, reason={}", sessionId, ex.getMessage());
            return 0;
        }
    }

    public int cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(LocalDateTime.now()));
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
            log.debug("ADM expired session cleanup skipped. reason={}", ex.getMessage());
            return 0;
        }
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void persistSession(AdmSession session) {
        try {
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator_session (
                        SESSION_ID, TOKEN_HASH, OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT,
                        REVOKED_YN, CREATED_BY, UPDATED_BY
                    ) VALUES (?, ?, ?, ?, ?, ?, 'N', ?, ?)
                    ON DUPLICATE KEY UPDATE
                        TOKEN_HASH = VALUES(TOKEN_HASH),
                        OPERATOR_ID = VALUES(OPERATOR_ID),
                        ROLE_IDS = VALUES(ROLE_IDS),
                        ISSUED_AT = VALUES(ISSUED_AT),
                        EXPIRE_AT = VALUES(EXPIRE_AT),
                        REVOKED_YN = 'N',
                        UPDATED_BY = VALUES(UPDATED_BY),
                        UPDATED_AT = CURRENT_TIMESTAMP
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
            log.debug("ADM session DB persistence skipped. operatorId={}, reason={}", session.operatorId(), ex.getMessage());
        }
    }

    private Optional<AdmSession> findDbSession(String token) {
        try {
            return admJdbcTemplate.query("""
                            SELECT OPERATOR_ID, ROLE_IDS, ISSUED_AT, EXPIRE_AT
                            FROM adm_operator_session
                            WHERE TOKEN_HASH = ?
                              AND REVOKED_YN = 'N'
                              AND EXPIRE_AT > CURRENT_TIMESTAMP
                            ORDER BY EXPIRE_AT DESC
                            LIMIT 1
                            """,
                    rs -> {
                        if (!rs.next()) {
                            return Optional.<AdmSession>empty();
                        }
                        AdmSession session = new AdmSession(
                                token,
                                rs.getString("OPERATOR_ID"),
                                parseRoleIds(rs.getString("ROLE_IDS")),
                                rs.getTimestamp("ISSUED_AT").toLocalDateTime(),
                                rs.getTimestamp("EXPIRE_AT").toLocalDateTime());
                        sessions.put(token, session);
                        return Optional.of(session);
                    },
                    tokenHash(token));
        } catch (DataAccessException ex) {
            log.debug("ADM session DB lookup skipped. reason={}", ex.getMessage());
            return Optional.empty();
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
                    """, tokenHash(token));
        } catch (DataAccessException ex) {
            log.debug("ADM session DB revoke skipped. reason={}", ex.getMessage());
        }
    }

    private List<String> parseRoleIds(String roleIds) {
        if (roleIds == null || roleIds.isBlank()) {
            return List.of();
        }
        return Arrays.stream(roleIds.split(","))
                .map(String::trim)
                .filter(roleId -> !roleId.isBlank())
                .toList();
    }

    private String tokenHash(String token) {
        return cryptoService.sha256Hex(token);
    }
}
