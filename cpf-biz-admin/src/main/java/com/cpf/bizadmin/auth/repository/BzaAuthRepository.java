package com.cpf.bizadmin.auth.repository;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
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
 * BZA 인증 정보를 bzaDB에 영속화하는 저장소입니다.
 *
 * <p>이 저장소는 업무 관리자 계정, 로그인 이력, refresh token hash를 DB 기준으로 처리합니다.
 * datasource가 비활성화된 환경에서는 임시 메모리 대체 저장소를 만들지 않고 명확히 실패시킵니다.</p>
 */
@Repository
public class BzaAuthRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    private final CpfVendorSqlCatalog sql;

    public BzaAuthRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            Environment environment) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.sql = CpfVendorSqlCatalog.create(environment, "bza");
    }

    /**
     * 로그인 ID로 업무 관리자 계정을 조회합니다.
     */
    public Optional<BzaOperatorRow> findOperatorByLoginId(String loginId) {
        List<BzaOperatorRow> rows = jdbc().query("""
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
                  FROM bza_admin_user
                 WHERE admin_login_id = :loginId
                """, new MapSqlParameterSource("loginId", loginId), this::mapOperator);
        return rows.stream().findFirst().map(row -> row.withPermissions(
                findMenus(row.roleCode()),
                findButtons(row.roleCode())));
    }

    /** 환경변수로 승인된 최초 BZA 운영자를 기존 계정이 없을 때만 생성합니다. */
    public int bootstrapOperator(String loginId, String operatorName, String passwordHash, String roleCode) {
        return jdbc().update(sql.required("auth-bootstrap-operator"), new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("operatorName", operatorName)
                .addValue("passwordHash", passwordHash)
                .addValue("roleCode", roleCode));
    }

    /**
     * 비밀번호 실패 횟수를 증가시킵니다.
     */
    public void increaseLoginFailCount(long adminUserId) {
        jdbc().update(sql.required("auth-increase-login-fail-count"),
                new MapSqlParameterSource("adminUserId", adminUserId));
    }

    /**
     * 로그인 성공 시 실패 횟수와 최근 로그인 일시를 갱신합니다.
     */
    public void markLoginSuccess(long adminUserId) {
        jdbc().update(sql.required("auth-mark-login-success"),
                new MapSqlParameterSource("adminUserId", adminUserId));
    }

    /**
     * 업무 관리자 로그인 이력을 저장합니다.
     */
    public void insertLoginHistory(LoginHistoryWrite row) {
        jdbc().update("""
                INSERT INTO bza_login_history (
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
                    'BZA_AUTH',
                    'BZA_AUTH'
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
                INSERT INTO bza_refresh_token (
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
                    'BZA_AUTH',
                    'BZA_AUTH'
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
                  FROM bza_refresh_token rt
                  JOIN bza_admin_user u
                    ON u.admin_user_id = rt.admin_user_id
                 WHERE rt.refresh_token_hash = :refreshTokenHash
                """, new MapSqlParameterSource("refreshTokenHash", refreshTokenHash), this::mapRefreshToken);
        return rows.stream().findFirst();
    }

    /**
     * refresh token을 폐기합니다.
     */
    public int revokeRefreshToken(String refreshTokenHash) {
        return jdbc().update(sql.required("auth-revoke-refresh-token"),
                new MapSqlParameterSource("refreshTokenHash", refreshTokenHash));
    }

    /** refresh token 원문과 hash를 제외한 현재 사용자 세션 메타만 조회합니다. */
    public List<Map<String, Object>> findRefreshSessions(long adminUserId, int limit) {
        return jdbc().queryForList(sql.required("auth-find-refresh-sessions"), new MapSqlParameterSource()
                .addValue("adminUserId", adminUserId)
                .addValue("limit", limit));
    }

    /** 본인에게 속한 활성 refresh session만 조건부 폐기합니다. */
    public int revokeRefreshSession(long sessionId, long adminUserId, String updatedBy) {
        return jdbc().update(sql.required("auth-revoke-refresh-session"), new MapSqlParameterSource()
                .addValue("sessionId", sessionId)
                .addValue("adminUserId", adminUserId)
                .addValue("updatedBy", updatedBy));
    }

    public void insertBusinessAudit(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_business_audit (
                    transaction_global_id, actor_id, action_type, target_type, target_id,
                    reason, before_data, after_data, created_by, updated_by
                ) VALUES (
                    :transactionGlobalId, :actorId, :actionType, :targetType, :targetId,
                    :reason, :beforeData, :afterData, :actorId, :actorId
                )
                """, values);
    }

    /** 기존 저장값이 그대로일 때만 강화된 비밀번호 hash로 교체합니다. */
    public int updatePasswordHashIfUnchanged(
            long adminUserId,
            String previousHash,
            String newHash,
            String updatedBy) {
        return jdbc().update(sql.required("auth-update-password-hash-if-unchanged"), new MapSqlParameterSource()
                .addValue("adminUserId", adminUserId)
                .addValue("previousHash", previousHash)
                .addValue("newHash", newHash)
                .addValue("updatedBy", updatedBy));
    }

    /** 본인 비밀번호 변경과 강제 변경 상태 해제를 원자적으로 처리합니다. */
    public int changePassword(long adminUserId, String previousHash, String newHash, String updatedBy) {
        return jdbc().update(sql.required("auth-change-password"), new MapSqlParameterSource()
                .addValue("adminUserId", adminUserId)
                .addValue("previousHash", previousHash)
                .addValue("newHash", newHash)
                .addValue("updatedBy", updatedBy));
    }

    /** 비밀번호 변경 후 해당 사용자의 모든 refresh token을 폐기합니다. */
    public void revokeAllRefreshTokens(long adminUserId) {
        jdbc().update(sql.required("auth-revoke-all-refresh-tokens"),
                new MapSqlParameterSource("adminUserId", adminUserId));
    }

    /**
     * 최근 로그인 이력을 조회합니다.
     */
    public List<Map<String, Object>> findLoginHistories(int limit) {
        return jdbc().queryForList(sql.required("auth-find-login-histories"),
                new MapSqlParameterSource("limit", limit));
    }

    private List<String> findMenus(String roleCode) {
        return jdbc().queryForList("""
                SELECT DISTINCT menu_code
                  FROM bza_permission
                 WHERE role_code = :roleCode
                   AND allow_yn = 'Y'
                   AND use_yn = 'Y'
                 ORDER BY menu_code
                """, new MapSqlParameterSource("roleCode", roleCode), String.class);
    }

    private List<String> findButtons(String roleCode) {
        return jdbc().queryForList(sql.required("auth-find-buttons"),
                new MapSqlParameterSource("roleCode", roleCode), String.class);
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BZA DB datasource가 비활성화되어 인증 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }

    private BzaOperatorRow mapOperator(ResultSet rs, int rowNum) throws SQLException {
        return new BzaOperatorRow(
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

    public record BzaOperatorRow(
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
        private BzaOperatorRow withPermissions(List<String> resolvedMenus, List<String> resolvedButtons) {
            return new BzaOperatorRow(
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
