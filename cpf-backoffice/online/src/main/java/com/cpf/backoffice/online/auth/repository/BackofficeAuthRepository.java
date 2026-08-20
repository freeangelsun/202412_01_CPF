package com.cpf.backoffice.online.auth.repository;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import com.cpf.data.persistence.api.CpfRepository;
import org.springframework.web.server.ResponseStatusException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MBW 인증 정보를 backofficeDB에 영속화하는 저장소입니다.
 *
 * <p>이 저장소는 업무 관리자 계정, 로그인 이력, refresh token hash를 DB 기준으로 처리합니다.
 * datasource가 비활성화된 환경에서는 임시 메모리 대체 저장소를 만들지 않고 명확히 실패시킵니다.</p>
 */
@CpfRepository
public class BackofficeAuthRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    private final CpfVendorSqlCatalog sql;
    private final String environmentCode;

    public BackofficeAuthRepository(
            @Qualifier("MBW_JDBC_TEMPLATE") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            Environment environment) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.sql = sqlCatalogProvider.forModule("backoffice");
        this.environmentCode = resolveEnvironmentCode(environment);
    }

    /**
     * 로그인 ID로 업무 관리자 계정을 조회합니다.
     */
    public Optional<BackofficeOperatorRow> findOperatorByLoginId(String loginId) {
        List<BackofficeOperatorRow> rows = jdbc().query(sql.required("auth-repository-find-operator-by-login-id-01"), new MapSqlParameterSource("loginId", loginId), this::mapOperator);
        return rows.stream().findFirst().map(row -> {
            List<String> roleCodes = findEffectiveRoleCodes(row.adminUserId(), row.roleCode());
            return row.withPermissions(findMenus(roleCodes), findButtons(roleCodes));
        });
    }

    /** 환경변수로 승인된 최초 MBW 운영자를 operationId 기준으로 멱등 생성합니다. */
    public BootstrapResult bootstrapOperator(String loginId, String operatorName, String passwordHash, String roleCode,
                                             String operationId, Instant passwordExpireAt) {
        String op = requireText(operationId, "operationId");
        Optional<BootstrapResult> existingOperation = findBootstrapOperation(op);
        if (existingOperation.isPresent()) {
            return requireSameLogin(existingOperation.get(), loginId);
        }
        Optional<BootstrapResult> existingLogin = findBootstrapLogin(loginId);
        if (existingLogin.isPresent()) {
            BootstrapResult current = existingLogin.get();
            if (current.operationId() == null || current.operationId().isBlank()) {
                try {
                    jdbc().update(sql.required("auth-bootstrap-operator-bind-operation"), new MapSqlParameterSource()
                            .addValue("loginId", loginId).addValue("operationId", op));
                } catch (DuplicateKeyException duplicate) {
                    return requireSameLogin(findBootstrapOperation(op).orElseThrow(() -> duplicate), loginId);
                }
                return requireSameLogin(findBootstrapOperation(op)
                        .orElseThrow(() -> new IllegalStateException("MBW bootstrap operation binding 결과를 찾을 수 없습니다.")), loginId);
            }
            if (op.equals(current.operationId())) return current;
            throw new IllegalStateException("동일 MBW loginId가 다른 operationId로 이미 생성되었습니다. loginId=" + loginId);
        }
        try {
            jdbc().update(sql.required("auth-bootstrap-operator"), new MapSqlParameterSource()
                    .addValue("loginId", loginId).addValue("operatorName", operatorName)
                    .addValue("passwordHash", passwordHash).addValue("roleCode", roleCode)
                    .addValue("operationId", op).addValue("passwordExpireAt", Timestamp.from(passwordExpireAt)));
        } catch (DuplicateKeyException duplicate) {
            Optional<BootstrapResult> raced = findBootstrapOperation(op);
            if (raced.isPresent()) return requireSameLogin(raced.get(), loginId);
            throw new IllegalStateException("MBW bootstrap 생성 충돌이 발생했습니다. loginId=" + loginId, duplicate);
        }
        BootstrapResult created = findBootstrapOperation(op)
                .orElseThrow(() -> new IllegalStateException("MBW bootstrap 생성 결과를 찾을 수 없습니다. operationId=" + op));
        return new BootstrapResult(created.adminUserId(), created.loginId(), created.operationId(), true);
    }

    /** 결과불명 재시도에서 operationId로 관리자 생성 결과를 조회합니다. */
    public Optional<BootstrapResult> findBootstrapOperation(String operationId) {
        List<Map<String,Object>> rows = jdbc().queryForList(sql.required("auth-bootstrap-operator-find-operation"),
                new MapSqlParameterSource("operationId", requireText(operationId, "operationId")));
        return rows.stream().findFirst().map(this::bootstrapResult);
    }

    private Optional<BootstrapResult> findBootstrapLogin(String loginId) {
        List<Map<String,Object>> rows = jdbc().queryForList(sql.required("auth-bootstrap-operator-find-login"),
                new MapSqlParameterSource("loginId", requireText(loginId, "loginId")));
        return rows.stream().findFirst().map(this::bootstrapResult);
    }

    private BootstrapResult bootstrapResult(Map<String,Object> row) {
        Number id=(Number)row.get("adminUserId");
        return new BootstrapResult(id.longValue(), String.valueOf(row.get("loginId")),
                row.get("operationId")==null?null:String.valueOf(row.get("operationId")), false);
    }

    private BootstrapResult requireSameLogin(BootstrapResult result,String loginId){
        if(!result.loginId().equals(loginId)) throw new IllegalStateException("operationId가 다른 MBW loginId에 이미 사용되었습니다. operationId="+result.operationId());
        return result;
    }

    private static String requireText(String value,String field){
        if(value==null||value.isBlank()) throw new IllegalArgumentException(field+"는 필수입니다.");
        return value.trim();
    }

    public record BootstrapResult(long adminUserId,String loginId,String operationId,boolean created) {}


    /** 로그인 operationId와 canonical request fingerprint를 먼저 등록해 동시 재시도를 직렬화합니다. */
    public boolean insertLoginOperation(String idempotencyKey, long adminUserId, String loginId, String requestHash) {
        try {
            jdbc().update(sql.required("auth-login-operation-insert"), new MapSqlParameterSource()
                    .addValue("operationId", requireText(idempotencyKey, "idempotencyKey"))
                    .addValue("adminUserId", adminUserId)
                    .addValue("loginId", requireText(loginId, "loginId"))
                    .addValue("requestHash", requireText(requestHash, "requestHash")));
            return true;
        } catch (DuplicateKeyException duplicate) {
            return false;
        }
    }

    /** 동일 operationId의 로그인 처리 상태와 암호화된 최초 결과를 row lock으로 조회합니다. */
    public Optional<LoginOperationState> lockLoginOperation(String idempotencyKey) {
        List<Map<String,Object>> rows = jdbc().queryForList(sql.required("auth-login-operation-lock"),
                new MapSqlParameterSource("operationId", requireText(idempotencyKey, "idempotencyKey")));
        return rows.stream().findFirst().map(row -> new LoginOperationState(
                String.valueOf(row.get("operationId")),
                ((Number) row.get("adminUserId")).longValue(),
                String.valueOf(row.get("loginId")),
                String.valueOf(row.get("requestHash")),
                String.valueOf(row.get("status")),
                nullableText(row.get("resultAccessTokenEnc")),
                nullableText(row.get("resultRefreshTokenEnc")),
                toInstant(row.get("resultRefreshExpiresAt")),
                toInstant(row.get("resultExpiresAt")),
                nullableText(row.get("failureCode")),
                nullableText(row.get("failureMessage"))));
    }

    /** 최초 성공 결과를 암호문으로 보존해 response-loss 재시도에서 동일 결과를 replay합니다. */
    public void markLoginOperationSuccess(
            String idempotencyKey,
            String resultAccessTokenEnc,
            String resultRefreshTokenEnc,
            Instant refreshExpireAt,
            Instant resultExpireAt) {
        int updated = jdbc().update(sql.required("auth-login-operation-success"), new MapSqlParameterSource()
                .addValue("operationId", requireText(idempotencyKey, "idempotencyKey"))
                .addValue("resultAccessTokenEnc", requireText(resultAccessTokenEnc, "resultAccessTokenEnc"))
                .addValue("resultRefreshTokenEnc", requireText(resultRefreshTokenEnc, "resultRefreshTokenEnc"))
                .addValue("resultRefreshExpiresAt", Timestamp.from(refreshExpireAt))
                .addValue("resultExpiresAt", Timestamp.from(resultExpireAt)));
        if (updated != 1) {
            throw new IllegalStateException("MBW 로그인 로그인 멱등 결과 저장에 실패했습니다. idempotencyKey=" + idempotencyKey);
        }
    }

    public record LoginOperationState(
            String operationId,
            long adminUserId,
            String loginId,
            String requestHash,
            String status,
            String resultAccessTokenEnc,
            String resultRefreshTokenEnc,
            Instant resultRefreshExpiresAt,
            Instant resultExpiresAt,
            String failureCode,
            String failureMessage) {}

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
        jdbc().update(sql.required("auth-repository-insert-login-history-01"), new MapSqlParameterSource()
                .addValue("adminUserId", row.adminUserId())
                .addValue("loginDomain", row.loginDomain())
                .addValue("adminLoginId", row.adminLoginId())
                .addValue("loginResult", row.loginResult())
                .addValue("failureReason", row.failureReason())
                .addValue("clientIp", row.clientIp())
                .addValue("userAgent", row.userAgent())
                .addValue("transactionId", row.transactionId())
                .addValue("systemCode", row.systemCode())
                .addValue("application", row.application())
                .addValue("instanceId", row.instanceId()));
    }

    /**
     * refresh token hash를 저장합니다.
     */
    public void insertRefreshToken(RefreshTokenWrite row) {
        jdbc().update(sql.required("auth-repository-insert-refresh-token-01"), new MapSqlParameterSource()
                .addValue("adminUserId", row.adminUserId())
                .addValue("loginDomain", row.loginDomain())
                .addValue("refreshTokenHash", row.refreshTokenHash())
                .addValue("transactionId", row.transactionId())
                .addValue("loginOperationId", row.loginOperationId())
                .addValue("expireAt", Timestamp.from(row.expireAt())));
    }

    /**
     * refresh token hash로 저장된 token 상태를 조회합니다.
     */
    public Optional<RefreshTokenRow> findRefreshToken(String refreshTokenHash) {
        List<RefreshTokenRow> rows = jdbc().query(sql.required("auth-repository-find-refresh-token-01"), new MapSqlParameterSource("refreshTokenHash", refreshTokenHash), this::mapRefreshToken);
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
        jdbc().update(sql.required("auth-repository-insert-business-audit-01"), values);
    }

    /** 기존 저장값이 그대로일 때만 강화된 비밀번호 hash로 교체합니다. */
    public int updatePasswordHashIfUnchanged(
            long adminUserId,
            String previousHash,
            String newHash,
            String updatedBy) {
        return jdbc().update(sql.required("auth-repository-update-password-hash-if-unchanged-01"), new MapSqlParameterSource()
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


    /** 상태 변경 등 loginId 기반 운영 조치 후 기존 refresh session을 모두 폐기합니다. */
    public void revokeAllRefreshTokensByLoginId(String loginId) {
        jdbc().update(sql.required("auth-revoke-all-refresh-by-login-id"),
                new MapSqlParameterSource("loginId", requireText(loginId, "loginId")));
    }

    /** Role/Permission 변경 시 해당 Role을 실제 보유한 모든 계정의 refresh session을 폐기합니다. */
    public void revokeRefreshTokensByRoleCode(String roleCode) {
        List<Long> userIds = jdbc().queryForList(sql.required("auth-find-user-ids-by-role-code"),
                new MapSqlParameterSource("roleCode", requireText(roleCode, "roleCode")), Long.class);
        userIds.stream().distinct().forEach(this::revokeAllRefreshTokens);
    }

    /**
     * 최근 로그인 이력을 조회합니다.
     */
    public List<Map<String, Object>> findLoginHistories(int limit) {
        return jdbc().queryForList(sql.required("auth-find-login-histories"),
                new MapSqlParameterSource("limit", limit));
    }

    /**
     * Legacy primary role과 유효기간 내 다중 역할을 합쳐 인증 시점의 실제 역할 집합을 계산합니다.
     * mbw_user_role이 아직 이관되지 않은 고객 DB도 기존 role_code로 계속 동작합니다.
     */
    private List<String> findEffectiveRoleCodes(long adminUserId, String legacyRoleCode) {
        Set<String> roles = new LinkedHashSet<>();
        Integer assignmentCount = jdbc().queryForObject(
                sql.required("auth-repository-find-effective-role-codes-03"),
                new MapSqlParameterSource("adminUserId", adminUserId), Integer.class);
        List<String> effective = jdbc().queryForList(sql.required("auth-repository-find-effective-role-codes-01"), new MapSqlParameterSource("adminUserId", adminUserId), String.class);
        roles.addAll(effective);

        // 다중 Role 이력이 한 번도 없는 구형 DB/계정만 legacy role_code를 fallback으로 사용합니다.
        // 이력이 존재하는 계정에서 만료/회수된 Role이 legacy 컬럼 때문에 부활하지 않게 합니다.
        if ((assignmentCount == null || assignmentCount == 0) && legacyRoleCode != null && !legacyRoleCode.isBlank()) {
            Integer activeLegacy = jdbc().queryForObject(sql.required("auth-repository-find-effective-role-codes-02"), new MapSqlParameterSource("roleCode", legacyRoleCode), Integer.class);
            if (activeLegacy != null && activeLegacy > 0) roles.add(legacyRoleCode);
        }
        return List.copyOf(roles);
    }

    private List<String> findMenus(List<String> roleCodes) {
        if (roleCodes.isEmpty()) return List.of();
        List<String> rows = jdbc().queryForList(sql.required("auth-repository-find-menus-01"), new MapSqlParameterSource()
                .addValue("roleCodes", roleCodes)
                .addValue("environmentCode", environmentCode), String.class);
        return rows.stream().map(BackofficeAuthRepository::normalizeMenuCode).distinct().toList();
    }

    private List<String> findButtons(List<String> roleCodes) {
        if (roleCodes.isEmpty()) return List.of();
        List<Map<String, Object>> rows = jdbc().queryForList(sql.required("auth-repository-find-buttons-01"), new MapSqlParameterSource()
                .addValue("roleCodes", roleCodes)
                .addValue("environmentCode", environmentCode));
        List<String> permissions = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String menu = normalizeMenuCode(String.valueOf(row.get("menuCode")));
            String button = String.valueOf(row.get("buttonCode"));
            permissions.add(menu + ":" + button);
        }
        return permissions.stream().distinct().toList();
    }

    private static String normalizeMenuCode(String menuCode) {
        if (menuCode == null) return "";
        String normalized = menuCode.trim();
        return normalized.regionMatches(true, 0, "MBW_", 0, 4) ? normalized.substring(4) : normalized;
    }

    private static String resolveEnvironmentCode(Environment environment) {
        String explicit = environment.getProperty("cpf.environment-code");
        if (explicit != null && !explicit.isBlank()) return explicit.trim().toUpperCase();
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) return "ALL";
        return profiles[0].trim().toUpperCase();
    }


    private static String nullableText(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static Instant toInstant(Object value) {
        if (value == null) return null;
        if (value instanceof Timestamp timestamp) return timestamp.toInstant();
        if (value instanceof java.util.Date date) return date.toInstant();
        return Instant.parse(String.valueOf(value));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "MBW DB datasource가 비활성화되어 인증 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }

    private BackofficeOperatorRow mapOperator(ResultSet rs, int rowNum) throws SQLException {
        return new BackofficeOperatorRow(
                rs.getLong("admin_user_id"),
                rs.getString("admin_login_id"),
                rs.getString("admin_name"),
                rs.getString("password_hash"),
                rs.getString("role_code"),
                rs.getString("account_status"),
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
                rs.getString("transaction_id"));
    }

    private Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    public record BackofficeOperatorRow(
            long adminUserId,
            String loginId,
            String adminName,
            String passwordHash,
            String roleCode,
            String accountStatus,
            String useYn,
            String lockYn,
            int loginFailCount,
            String passwordChangeRequiredYn,
            Instant passwordExpireAt,
            Instant lastLoginAt,
            List<String> menus,
            List<String> buttons) {
        private BackofficeOperatorRow withPermissions(List<String> resolvedMenus, List<String> resolvedButtons) {
            return new BackofficeOperatorRow(
                    adminUserId,
                    loginId,
                    adminName,
                    passwordHash,
                    roleCode,
                    accountStatus,
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
            String transactionId) {
    }

    public record LoginHistoryWrite(
            Long adminUserId,
            String loginDomain,
            String adminLoginId,
            String loginResult,
            String failureReason,
            String clientIp,
            String userAgent,
            String transactionId,
            String systemCode,
            String application,
            String instanceId) {
    }

    public record RefreshTokenWrite(
            long adminUserId,
            String loginDomain,
            String refreshTokenHash,
            String transactionId,
            String loginOperationId,
            Instant expireAt) {
    }
}
