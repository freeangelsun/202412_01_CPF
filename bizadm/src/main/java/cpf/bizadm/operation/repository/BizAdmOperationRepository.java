package cpf.bizadm.operation.repository;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * BIZADM 운영 데이터를 bizadmDB에서 조회하고 기록하는 저장소입니다.
 *
 * <p>운영 API는 메모리 샘플 데이터로 응답하지 않고 DB 조회 결과만 반환합니다.
 * datasource가 비활성화된 환경에서는 명확하게 503 오류를 반환해 미구현 기능이 성공처럼
 * 보이지 않게 합니다.</p>
 */
@Repository
public class BizAdmOperationRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;

    public BizAdmOperationRepository(
            @Qualifier("bizAdmJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider) {
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
                  FROM bizadm_admin_user
                 ORDER BY admin_user_id
                """, Map.of());
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
                  FROM bizadm_menu
                 ORDER BY module_code, menu_code
                """, Map.of());
    }

    public List<Map<String, Object>> findRoles() {
        return jdbc().queryForList("""
                SELECT role_id AS roleId,
                       role_code AS roleCode,
                       role_name AS roleName,
                       write_allowed_yn AS writeAllowedYn,
                       use_yn AS useYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bizadm_role
                 ORDER BY role_code
                """, Map.of());
    }

    public List<Map<String, Object>> findPermissions() {
        return jdbc().queryForList("""
                SELECT permission_id AS permissionId,
                       role_code AS roleCode,
                       menu_code AS menuCode,
                       button_code AS buttonCode,
                       allow_yn AS allowYn,
                       created_at AS createdAt,
                       updated_at AS updatedAt
                  FROM bizadm_permission
                 ORDER BY role_code, menu_code, button_code
                """, Map.of());
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
                  FROM bizadm_customer
                 ORDER BY customer_id
                """, Map.of());
    }

    /**
     * 개인정보 원문 조회는 대상, 운영자, 사유, 결과를 감사 이력으로 남깁니다.
     */
    public void insertMaskingAudit(String targetId, String operatorId, String reason, String resultType) {
        jdbc().update("""
                INSERT INTO bizadm_masking_audit (
                    target_type,
                    target_id,
                    operator_id,
                    reason,
                    result_type,
                    created_by,
                    updated_by
                )
                VALUES (
                    'bizadm_customer',
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
                  FROM bizadm_product
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
                  FROM bizadm_order
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
                  FROM bizadm_project_setting
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
                  FROM bizadm_project_setting
                 WHERE setting_key LIKE 'DOWNLOAD.%'
                 ORDER BY setting_key
                """, Map.of());
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BIZADM DB datasource가 비활성화되어 운영 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }
}
