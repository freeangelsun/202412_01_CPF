package com.cpf.bizadmin.operation.repository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * BZA 운영 데이터를 bzaDB에서 조회하고 기록하는 저장소입니다.
 *
 * <p>운영 API는 메모리 샘플 데이터로 응답하지 않고 DB 조회 결과만 반환합니다.
 * datasource가 비활성화된 환경에서는 명확하게 503 오류를 반환해 미구현 기능이 성공처럼
 * 보이지 않게 합니다.</p>
 */
@Repository
public class BzaOperationRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;

    public BzaOperationRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
    }

    public List<Map<String, Object>> findAdminUsers() {
        return jdbc().queryForList("""
                SELECT admin_user_id AS adminUserId,
                       admin_login_id AS adminLoginId,
                       admin_name AS adminName,
                       role_code AS roleCode,
                       use_yn AS useYn,
                       last_login_at AS lastLoginAt,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_admin_user
                 ORDER BY admin_user_id
                """, Map.of());
    }

    public Optional<Map<String, Object>> findAdminUser(String loginId) {
        return jdbc().queryForList("""
                SELECT admin_user_id AS adminUserId, admin_login_id AS adminLoginId,
                       admin_name AS adminName, role_code AS roleCode, use_yn AS useYn,
                       lock_yn AS lockYn, password_change_required_yn AS passwordChangeRequiredYn
                  FROM bza_admin_user
                 WHERE admin_login_id = :loginId
                """, new MapSqlParameterSource("loginId", loginId)).stream().findFirst();
    }

    public void saveAdminUser(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_admin_user (
                    admin_login_id, admin_name, password_hash, role_code, use_yn, lock_yn,
                    password_change_required_yn, created_by, updated_by
                ) VALUES (
                    :loginId, :adminName, :passwordHash, :roleCode, :useYn, :lockYn,
                    :passwordChangeRequiredYn, :requestUser, :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    admin_name = VALUES(admin_name),
                    password_hash = COALESCE(VALUES(password_hash), password_hash),
                    role_code = VALUES(role_code), use_yn = VALUES(use_yn), lock_yn = VALUES(lock_yn),
                    password_change_required_yn = VALUES(password_change_required_yn),
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, values);
    }

    public List<Map<String, Object>> findMenus() {
        return jdbc().queryForList("""
                SELECT menu_id AS menuId,
                       menu_code AS menuCode,
                       menu_name AS menuName,
                       module_code AS moduleCode,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_menu
                 ORDER BY module_code, menu_code
                """, Map.of());
    }

    public Optional<Map<String, Object>> findMenu(String menuCode) {
        return jdbc().queryForList("""
                SELECT menu_code AS menuCode, menu_name AS menuName,
                       parent_menu_code AS parentMenuCode, module_code AS moduleCode,
                       route_path AS routePath, icon_code AS iconCode,
                       environment_code AS environmentCode, api_path AS apiPath,
                       sort_order AS sortOrder, use_yn AS useYn
                  FROM bza_menu WHERE menu_code = :menuCode
                """, new MapSqlParameterSource("menuCode", menuCode)).stream().findFirst();
    }

    public void saveMenu(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_menu (
                    menu_code, menu_name, parent_menu_code, module_code, route_path, icon_code,
                    environment_code, api_path, sort_order, use_yn, created_by, updated_by
                ) VALUES (
                    :menuCode, :menuName, :parentMenuCode, :moduleCode, :routePath, :iconCode,
                    :environmentCode, :apiPath, :sortOrder, :useYn, :requestUser, :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    menu_name = VALUES(menu_name), parent_menu_code = VALUES(parent_menu_code),
                    module_code = VALUES(module_code), route_path = VALUES(route_path),
                    icon_code = VALUES(icon_code), environment_code = VALUES(environment_code),
                    api_path = VALUES(api_path), sort_order = VALUES(sort_order), use_yn = VALUES(use_yn),
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, values);
    }

    public List<Map<String, Object>> findRoles() {
        return jdbc().queryForList("""
                SELECT role_id AS roleId,
                       role_code AS roleCode,
                       role_name AS roleName,
                       write_allowed_yn AS writeAllowedYn,
                       data_scope AS dataScope,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_role
                 ORDER BY role_code
                """, Map.of());
    }

    public Optional<Map<String, Object>> findRole(String roleCode) {
        return jdbc().queryForList("""
                SELECT role_code AS roleCode, role_name AS roleName,
                       write_allowed_yn AS writeAllowedYn, data_scope AS dataScope, use_yn AS useYn
                  FROM bza_role WHERE role_code = :roleCode
                """, new MapSqlParameterSource("roleCode", roleCode)).stream().findFirst();
    }

    public void saveRole(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_role (
                    role_code, role_name, write_allowed_yn, data_scope, use_yn, created_by, updated_by
                ) VALUES (
                    :roleCode, :roleName, :writeAllowedYn, :dataScope, :useYn, :requestUser, :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    role_name = VALUES(role_name), write_allowed_yn = VALUES(write_allowed_yn),
                    data_scope = VALUES(data_scope), use_yn = VALUES(use_yn),
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, values);
    }

    public List<Map<String, Object>> findPermissions() {
        return jdbc().queryForList("""
                SELECT permission_id AS permissionId,
                       role_code AS roleCode,
                       menu_code AS menuCode,
                       button_code AS buttonCode,
                       permission_type AS permissionType,
                       http_method AS httpMethod,
                       api_pattern AS apiPattern,
                       domain_code AS domainCode,
                       environment_code AS environmentCode,
                       data_scope AS dataScope,
                       allow_yn AS allowYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_permission
                 ORDER BY role_code, menu_code, button_code
                """, Map.of());
    }

    public Optional<Map<String, Object>> findPermission(String roleCode, String menuCode, String buttonCode) {
        return jdbc().queryForList("""
                SELECT role_code AS roleCode, menu_code AS menuCode, button_code AS buttonCode,
                       permission_type AS permissionType, http_method AS httpMethod,
                       api_pattern AS apiPattern, domain_code AS domainCode,
                       environment_code AS environmentCode, data_scope AS dataScope,
                       allow_yn AS allowYn, use_yn AS useYn
                  FROM bza_permission
                 WHERE role_code = :roleCode AND menu_code = :menuCode AND button_code = :buttonCode
                """, new MapSqlParameterSource()
                .addValue("roleCode", roleCode)
                .addValue("menuCode", menuCode)
                .addValue("buttonCode", buttonCode)).stream().findFirst();
    }

    public void savePermission(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_permission (
                    role_code, menu_code, button_code, permission_type, http_method, api_pattern,
                    domain_code, environment_code, data_scope, allow_yn, use_yn, created_by, updated_by
                ) VALUES (
                    :roleCode, :menuCode, :buttonCode, :permissionType, :httpMethod, :apiPattern,
                    :domainCode, :environmentCode, :dataScope, :allowYn, :useYn, :requestUser, :requestUser
                )
                ON DUPLICATE KEY UPDATE
                    permission_type = VALUES(permission_type), http_method = VALUES(http_method),
                    api_pattern = VALUES(api_pattern), domain_code = VALUES(domain_code),
                    environment_code = VALUES(environment_code), data_scope = VALUES(data_scope),
                    allow_yn = VALUES(allow_yn), use_yn = VALUES(use_yn),
                    updated_by = VALUES(updated_by), updated_at = CURRENT_TIMESTAMP
                """, values);
    }

    public List<Map<String, Object>> findCustomers() {
        return jdbc().queryForList("""
                SELECT customer_id AS customerId,
                       customer_no AS customerNo,
                       customer_name AS customerName,
                       email,
                       mobile_no AS mobileNo,
                       customer_status AS customerStatus,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_customer
                 ORDER BY customer_id
                """, Map.of());
    }

    /**
     * 개인정보 원문 조회는 대상, 운영자, 사유, 결과를 감사 이력으로 남깁니다.
     */
    public void insertMaskingAudit(String targetId, String operatorId, String reason, String resultType) {
        jdbc().update("""
                INSERT INTO bza_masking_audit (
                    target_type,
                    target_id,
                    operator_id,
                    reason,
                    result_type,
                    created_by,
                    updated_by
                )
                VALUES (
                    'bza_customer',
                    :targetId,
                    :operatorId,
                    :reason,
                    :resultType,
                    :operatorId,
                    :operatorId
                )
                """, new MapSqlParameterSource()
                .addValue("targetId", targetId)
                .addValue("operatorId", operatorId)
                .addValue("reason", reason)
                .addValue("resultType", resultType));
    }

    public List<Map<String, Object>> findProducts() {
        return jdbc().queryForList("""
                SELECT product_id AS productId,
                       product_code AS productCode,
                       product_name AS productName,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_product
                 ORDER BY product_code
                """, Map.of());
    }

    public List<Map<String, Object>> findOrders() {
        return jdbc().queryForList("""
                SELECT order_id AS orderId,
                       order_no AS orderNo,
                       customer_no AS customerNo,
                       product_code AS productCode,
                       order_amount AS orderAmount,
                       order_status AS orderStatus,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_order
                 ORDER BY order_id DESC
                """, Map.of());
    }

    public List<Map<String, Object>> findSettings() {
        return jdbc().queryForList("""
                SELECT setting_id AS settingId,
                       setting_key AS settingKey,
                       setting_value AS settingValue,
                       description,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bza_project_setting
                 ORDER BY setting_key
                """, Map.of());
    }

    public List<Map<String, Object>> findDownloadPolicies() {
        return jdbc().queryForList("""
                SELECT setting_key AS policyKey,
                       setting_value AS policyValue,
                       description,
                       use_yn AS useYn,
                       updated_at AS updatedAt
                  FROM bza_project_setting
                 WHERE setting_key LIKE 'DOWNLOAD.%'
                 ORDER BY setting_key
                """, Map.of());
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

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BZA DB datasource가 비활성화되어 운영 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }
}
