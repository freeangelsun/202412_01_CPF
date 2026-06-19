package cpf.bizadm.auth.repository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * BIZADM 인증 정보를 bizadmDB에 영속화하는 저장소입니다.
 *
 * <p>이 저장소는 업무 관리자 계정, 로그인 이력, refresh token hash를 DB 기준으로 처리합니다.
 * datasource가 비활성화된 환경에서는 임시 메모리 대체 저장소를 만들지 않고 명확히 실패시킵니다.</p>
 */
@Repository
public class BizAdmAuthRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;

    public BizAdmAuthRepository(
            @Qualifier("bizAdmJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    /**
     * 로그인 ID로 업무 관리자 계정을 조회합니다.
     */
    public Optional<BizAdmOperatorRow> findOperatorByLoginId(String loginId) {
        List<BizAdmOperatorRow> rows = jdbc().query("""
                SELECT admin_user_id,
                       admin_login_id,
                       admin_name,
                       password_hash,
                       role_code,
                       use_yn,
                       lock_yn,
                       login_fail_count,
                       password_change_required_yn,
                       password_expire_at,
                       last_login_at
                  FROM bizadm_admin_user
                 WHERE admin_login_id = :loginId
                """, new MapSqlParameterSource("loginId", loginId), this::mapOperator);
        return rows.stream().findFirst().map(row -> row.withPermissions(
                findMenus(row.roleCode()),
                findButtons(row.roleCode())));
    }

    /**
     * 비밀번호 실패 횟수를 증가시킵니다.
     */
    public void increaseLoginFailCount(long adminUserId) {
        jdbc().update("""
                UPDATE bizadm_admin_user
                   SET login_fail_count = login_fail_count + 1,
                       updated_by = 'BIZADM_AUTH',
                       updated_at = NOW()
                 WHERE admin_user_id = :adminUserId
                """, new MapSqlParameterSource("adminUserId", adminUserId));
    }

    /**
     * 로그인 성공 시 실패 횟수와 최근 로그인 일시를 갱신합니다.
     */
    public void markLoginSuccess(long adminUserId) {
        jdbc().update("""
                UPDATE bizadm_admin_user
                   SET login_fail_count = 0,
                       last_login_at = NOW(),
                       updated_by = 'BIZADM_AUTH',
                       updated_at = NOW()
                 WHERE admin_user_id = :adminUserId
                """, new MapSqlParameterSource("adminUserId", adminUserId));
    }

    /**
     * 업무 관리자 로그인 이력을 저장합니다.
     */
    public void insertLoginHistory(LoginHistoryWrite row) {
        jdbc().update("""
                INSERT INTO bizadm_login_history (
                    admin_user_id,
                    login_domain,
                    admin_login_id,
                    login_result,
                    failure_reason,
                    client_ip,
                    user_agent,
                    transaction_global_id,
                    module_id,
                    was_id,
                    server_instance_id,
                    created_by,
                    updated_by
                )
                VALUES (
                    :adminUserId,
                    :loginDomain,
                    :adminLoginId,
                    :loginResult,
                    :failureReason,
                    :clientIp,
                    :userAgent,
                    :transactionGlobalId,
                    :moduleId,
                    :wasId,
                    :serverInstanceId,
                    'BIZADM_AUTH',
                    'BIZADM_AUTH'
                )
                """, new MapSqlParameterSource()
                .addValue("adminUserId", row.adminUserId())
                .addValue("loginDomain", row.loginDomain())
                .addValue("adminLoginId", row.adminLoginId())
                .addValue("loginResult", row.loginResult())
                .addValue("failureReason", row.failureReason())
                .addValue("clientIp", row.clientIp())
                .addValue("userAgent", row.userAgent())
                .addValue("transactionGlobalId", row.transactionGlobalId())
                .addValue("moduleId", row.moduleId())
                .addValue("wasId", row.wasId())
                .addValue("serverInstanceId", row.serverInstanceId()));
    }

    /**
     * refresh token hash를 저장합니다.
     */
    public void insertRefreshToken(RefreshTokenWrite row) {
        jdbc().update("""
                INSERT INTO bizadm_refresh_token (
                    admin_user_id,
                    login_domain,
                    refresh_token_hash,
                    transaction_global_id,
                    expire_at,
                    revoked_yn,
                    created_by,
                    updated_by
                )
                VALUES (
                    :adminUserId,
                    :loginDomain,
                    :refreshTokenHash,
                    :transactionGlobalId,
                    :expireAt,
                    'N',
                    'BIZADM_AUTH',
                    'BIZADM_AUTH'
                )
                """, new MapSqlParameterSource()
                .addValue("adminUserId", row.adminUserId())
                .addValue("loginDomain", row.loginDomain())
                .addValue("refreshTokenHash", row.refreshTokenHash())
                .addValue("transactionGlobalId", row.transactionGlobalId())
                .addValue("expireAt", Timestamp.from(row.expireAt())));
    }

    /**
     * refresh token hash로 저장된 token 상태를 조회합니다.
     */
    public Optional<RefreshTokenRow> findRefreshToken(String refreshTokenHash) {
        List<RefreshTokenRow> rows = jdbc().query("""
                SELECT rt.refresh_token_hash,
                       rt.admin_user_id,
                       rt.login_domain,
                       rt.expire_at,
                       rt.revoked_yn,
                       rt.transaction_global_id,
                       u.admin_login_id
                  FROM bizadm_refresh_token rt
                  JOIN bizadm_admin_user u
                    ON u.admin_user_id = rt.admin_user_id
                 WHERE rt.refresh_token_hash = :refreshTokenHash
                """, new MapSqlParameterSource("refreshTokenHash", refreshTokenHash), this::mapRefreshToken);
        return rows.stream().findFirst();
    }

    /**
     * refresh token을 폐기합니다.
     */
    public void revokeRefreshToken(String refreshTokenHash) {
        jdbc().update("""
                UPDATE bizadm_refresh_token
                   SET revoked_yn = 'Y',
                       revoked_at = NOW(),
                       updated_by = 'BIZADM_AUTH',
                       updated_at = NOW()
                 WHERE refresh_token_hash = :refreshTokenHash
                """, new MapSqlParameterSource("refreshTokenHash", refreshTokenHash));
    }

    /**
     * 최근 로그인 이력을 조회합니다.
     */
    public List<Map<String, Object>> findLoginHistories(int limit) {
        return jdbc().queryForList("""
                SELECT login_history_id AS historyId,
                       login_domain AS loginDomain,
                       admin_user_id AS adminUserId,
                       admin_login_id AS adminLoginId,
                       login_result AS loginResult,
                       failure_reason AS failureReason,
                       client_ip AS clientIp,
                       user_agent AS userAgent,
                       transaction_global_id AS transactionGlobalId,
                       module_id AS moduleId,
                       was_id AS wasId,
                       server_instance_id AS serverInstanceId,
                       created_at AS occurredAt
                  FROM bizadm_login_history
                 ORDER BY login_history_id DESC
                 LIMIT :limit
                """, new MapSqlParameterSource("limit", limit));
    }

    private List<String> findMenus(String roleCode) {
        return jdbc().queryForList("""
                SELECT DISTINCT menu_code
                  FROM bizadm_permission_sample
                 WHERE role_code = :roleCode
                   AND use_yn = 'Y'
                 ORDER BY menu_code
                """, new MapSqlParameterSource("roleCode", roleCode), String.class);
    }

    private List<String> findButtons(String roleCode) {
        return jdbc().queryForList("""
                SELECT CONCAT(menu_code, ':', button_code) AS button_key
                  FROM bizadm_permission_sample
                 WHERE role_code = :roleCode
                   AND use_yn = 'Y'
                 ORDER BY menu_code, button_code
                """, new MapSqlParameterSource("roleCode", roleCode), String.class);
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BIZADM DB datasource가 비활성화되어 인증 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }

    private BizAdmOperatorRow mapOperator(ResultSet rs, int rowNum) throws SQLException {
        return new BizAdmOperatorRow(
                rs.getLong("admin_user_id"),
                rs.getString("admin_login_id"),
                rs.getString("admin_name"),
                rs.getString("password_hash"),
                rs.getString("role_code"),
                rs.getString("use_yn"),
                rs.getString("lock_yn"),
                rs.getInt("login_fail_count"),
                rs.getString("password_change_required_yn"),
                toInstant(rs.getTimestamp("password_expire_at")),
                toInstant(rs.getTimestamp("last_login_at")),
                List.of(),
                List.of());
    }

    private RefreshTokenRow mapRefreshToken(ResultSet rs, int rowNum) throws SQLException {
        return new RefreshTokenRow(
                rs.getString("refresh_token_hash"),
                rs.getLong("admin_user_id"),
                rs.getString("admin_login_id"),
                rs.getString("login_domain"),
                toInstant(rs.getTimestamp("expire_at")),
                "Y".equals(rs.getString("revoked_yn")),
                rs.getString("transaction_global_id"));
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    public record BizAdmOperatorRow(
            long adminUserId,
            String loginId,
            String adminName,
            String passwordHash,
            String roleCode,
            String useYn,
            String lockYn,
            int loginFailCount,
            String passwordChangeRequiredYn,
            Instant passwordExpireAt,
            Instant lastLoginAt,
            List<String> menus,
            List<String> buttons) {
        private BizAdmOperatorRow withPermissions(List<String> resolvedMenus, List<String> resolvedButtons) {
            return new BizAdmOperatorRow(
                    adminUserId,
                    loginId,
                    adminName,
                    passwordHash,
                    roleCode,
                    useYn,
                    lockYn,
                    loginFailCount,
                    passwordChangeRequiredYn,
                    passwordExpireAt,
                    lastLoginAt,
                    List.copyOf(resolvedMenus),
                    List.copyOf(resolvedButtons));
        }
    }

    public record RefreshTokenRow(
            String refreshTokenHash,
            long adminUserId,
            String loginId,
            String loginDomain,
            Instant expiresAt,
            boolean revoked,
            String transactionGlobalId) {
    }

    public record LoginHistoryWrite(
            Long adminUserId,
            String loginDomain,
            String adminLoginId,
            String loginResult,
            String failureReason,
            String clientIp,
            String userAgent,
            String transactionGlobalId,
            String moduleId,
            String wasId,
            String serverInstanceId) {
    }

    public record RefreshTokenWrite(
            long adminUserId,
            String loginDomain,
            String refreshTokenHash,
            String transactionGlobalId,
            Instant expireAt) {
    }
}
