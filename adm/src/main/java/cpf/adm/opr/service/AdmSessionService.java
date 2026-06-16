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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ADM API Bearer ?좏겙 ?몄뀡 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?섑뵆 ?꾨젅?꾩썙?ъ뿉?쒕뒗 ?몃찓紐⑤━ ??μ냼瑜??ъ슜?⑸땲?? ?댁쁺?먯꽌?????대옒?ㅼ쓽 ??μ냼瑜? * DB/Redis/JWT 寃利?諛⑹떇?쇰줈 援먯껜?섎㈃ 而⑦듃濡ㅻ윭? Vue ?붾㈃ 怨꾩빟? 洹몃?濡??좎??⑸땲??</p>
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
     * 濡쒓렇???깃났 ?댁쁺?먯뿉寃?API ?좏겙??諛쒓툒?⑸땲??
     *
     * @param operator ?몄쬆???댁쁺??     * @param menus    沅뚰븳 湲곗? 硫붾돱 紐⑸줉
     * @return 濡쒓렇???묐떟
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
     * ?좏겙?쇰줈 ?좏슚???몄뀡??議고쉶?⑸땲??
     *
     * @param token Bearer ?좏겙
     * @return ?좏슚???몄뀡
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
     * 濡쒓렇?꾩썐 ???몄뀡???쒓굅?⑸땲??
     *
     * @param token Bearer ?좏겙
     */
    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
            revokeDbSession(token);
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
                    INSERT INTO operator_session (
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
                            FROM operator_session
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
                    UPDATE operator_session
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

