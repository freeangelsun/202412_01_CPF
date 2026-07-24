package com.cpf.bizadmin.backoffice.repository;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** BZA 조직·직원·권한·결재 기능의 기본 DB adapter입니다. */
@Repository
public class BzaBackofficeRepository {
    private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
    private final CpfVendorSqlCatalog sql;

    public BzaBackofficeRepository(
            @Qualifier("bzaJdbcTemplate") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
            Environment environment) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.sql = CpfVendorSqlCatalog.create(environment, "bza");
    }

    public List<Map<String, Object>> findOrganizations() {
        return jdbc().queryForList("""
                SELECT organization_id AS organizationId, organization_code AS organizationCode,
                       parent_organization_code AS parentOrganizationCode, organization_name AS organizationName,
                       organization_type AS organizationType, sort_order AS sortOrder, use_yn AS useYn,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_organization
                 ORDER BY sort_order, organization_code
                """, Map.of());
    }

    public int saveOrganization(Map<String, ?> values) {
        return jdbc().update(sql.required("backoffice-save-organization"), values);
    }

    public List<Map<String, Object>> findEmployees(String organizationCode, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("status", status);
        return jdbc().queryForList("""
                SELECT employee_id AS employeeId, employee_no AS employeeNo, admin_user_id AS adminUserId,
                       organization_code AS organizationCode, employee_name AS employeeName,
                       position_code AS positionCode, job_title_code AS jobTitleCode,
                       manager_employee_no AS managerEmployeeNo, employment_status AS employmentStatus,
                       join_date AS joinDate, leave_date AS leaveDate, email, mobile_no AS mobileNo,
                       delegated_approver_no AS delegatedApproverNo,
                       absence_from AS absenceFrom, absence_to AS absenceTo, use_yn AS useYn,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_employee
                 WHERE (:organizationCode IS NULL OR organization_code = :organizationCode)
                   AND (:status IS NULL OR employment_status = :status)
                 ORDER BY organization_code, employee_no
                """, params);
    }

    public int saveEmployee(Map<String, ?> values) {
        return jdbc().update(sql.required("backoffice-save-employee"), values);
    }

    public List<Map<String, Object>> findEffectivePermissions(String loginId) {
        return jdbc().queryForList("""
                SELECT u.admin_login_id AS loginId, p.role_code AS roleCode, p.menu_code AS menuCode,
                       p.button_code AS actionCode, p.permission_type AS permissionType,
                       p.http_method AS httpMethod, p.api_pattern AS apiPattern,
                       p.domain_code AS domainCode, p.environment_code AS environmentCode,
                       p.data_scope AS dataScope, p.allow_yn AS allowYn
                  FROM bza_admin_user u
                  JOIN bza_permission p ON p.role_code = u.role_code
                 WHERE u.admin_login_id = :loginId
                   AND u.use_yn = 'Y' AND p.use_yn = 'Y'
                 ORDER BY p.menu_code, p.button_code
                """, new MapSqlParameterSource("loginId", loginId));
    }

    /** 인증된 BZA 로그인 ID와 결재 처리용 직원 번호의 바인딩을 조회합니다. */
    public Optional<String> findEmployeeNoByLoginId(String loginId) {
        return jdbc().queryForList("""
                SELECT e.employee_no AS employeeNo
                  FROM bza_admin_user u
                  JOIN bza_employee e ON e.admin_user_id = u.admin_user_id
                 WHERE u.admin_login_id = :loginId
                   AND u.use_yn = 'Y'
                   AND e.use_yn = 'Y'
                """, new MapSqlParameterSource("loginId", loginId)).stream()
                .map(row -> String.valueOf(row.get("employeeNo")))
                .findFirst();
    }

    public long createApproval(Map<String, ?> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update("""
                INSERT INTO bza_approval_document (
                    approval_no, approval_type, business_domain, title, requester_employee_no,
                    approval_status, approval_mode, current_step_no, due_at, payload_json,
                    attachment_group_id, version_no, transaction_global_id, created_by, updated_by
                ) VALUES (
                    :approvalNo, :approvalType, :businessDomain, :title, :requesterEmployeeNo,
                    'DRAFT', :approvalMode, 0, :dueAt, :payloadJson,
                    :attachmentGroupId, 0, :transactionGlobalId, :requestUser, :requestUser
                )
                """, new MapSqlParameterSource(values), keyHolder, new String[]{"approval_id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("결재 문서 생성 키를 확인할 수 없습니다.");
        }
        return key.longValue();
    }

    public void addApprovalLine(long approvalId, int stepNo, String approverEmployeeNo, String decisionRule, String requestUser) {
        jdbc().update("""
                INSERT INTO bza_approval_line (
                    approval_id, step_no, approver_employee_no,
                    step_type, target_type, target_code, decision_rule, required_yn,
                    decision_status, created_by, updated_by
                ) VALUES (
                    :approvalId, :stepNo, :approverEmployeeNo,
                    'APPROVAL', 'EMPLOYEE', :approverEmployeeNo, :decisionRule, 'Y',
                    'WAITING', :requestUser, :requestUser
                )
                """, new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("stepNo", stepNo)
                .addValue("approverEmployeeNo", approverEmployeeNo)
                .addValue("decisionRule", decisionRule)
                .addValue("requestUser", requestUser));
    }

    public List<Map<String, Object>> findApprovals(String status, String employeeNo, int limit) {
        return jdbc().queryForList(sql.required("backoffice-find-approvals"), new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("employeeNo", employeeNo)
                .addValue("limit", limit));
    }

    public Optional<Map<String, Object>> findApproval(long approvalId) {
        List<Map<String, Object>> rows = jdbc().queryForList("""
                SELECT approval_id AS approvalId, approval_no AS approvalNo, approval_type AS approvalType,
                       business_domain AS businessDomain, title, requester_employee_no AS requesterEmployeeNo,
                       approval_status AS approvalStatus, approval_mode AS approvalMode,
                       current_step_no AS currentStepNo, due_at AS dueAt, payload_json AS payloadJson,
                       attachment_group_id AS attachmentGroupId, version_no AS versionNo,
                       transaction_global_id AS transactionGlobalId, created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_approval_document WHERE approval_id = :approvalId
                """, new MapSqlParameterSource("approvalId", approvalId));
        return rows.stream().findFirst();
    }

    public List<Map<String, Object>> findApprovalLines(long approvalId) {
        return jdbc().queryForList("""
                SELECT approval_line_id AS approvalLineId, approval_id AS approvalId, step_no AS stepNo,
                       approver_employee_no AS approverEmployeeNo, decision_rule AS decisionRule,
                       decision_status AS decisionStatus, delegated_from_employee_no AS delegatedFromEmployeeNo,
                       decision_comment AS decisionComment, decided_at AS decidedAt
                  FROM bza_approval_line
                 WHERE approval_id = :approvalId
                 ORDER BY step_no, approval_line_id
                """, new MapSqlParameterSource("approvalId", approvalId));
    }

    public int decideLine(long approvalId, int stepNo, String actorEmployeeNo, String decision, String comment) {
        return jdbc().update(sql.required("backoffice-decide-line"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("stepNo", stepNo)
                .addValue("actorEmployeeNo", actorEmployeeNo)
                .addValue("decision", decision)
                .addValue("comment", comment));
    }

    public long countWaitingAtStep(long approvalId, int stepNo) {
        Long value = jdbc().queryForObject("""
                SELECT COUNT(*) FROM bza_approval_line
                 WHERE approval_id = :approvalId AND step_no = :stepNo AND decision_status = 'WAITING'
                """, new MapSqlParameterSource().addValue("approvalId", approvalId).addValue("stepNo", stepNo), Long.class);
        return value == null ? 0 : value;
    }

    public Integer nextStep(long approvalId, int currentStep) {
        return jdbc().queryForObject("""
                SELECT MIN(step_no) FROM bza_approval_line
                 WHERE approval_id = :approvalId AND step_no > :currentStep
                """, new MapSqlParameterSource().addValue("approvalId", approvalId).addValue("currentStep", currentStep), Integer.class);
    }

    public int updateApprovalStatus(long approvalId, long expectedVersion, String status, int currentStep, String actor) {
        return jdbc().update(sql.required("backoffice-update-approval-status"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("expectedVersion", expectedVersion)
                .addValue("status", status)
                .addValue("currentStep", currentStep)
                .addValue("actor", actor));
    }

    public boolean approvalActionExists(String idempotencyKey) {
        Long value = jdbc().queryForObject("""
                SELECT COUNT(*) FROM bza_approval_history WHERE idempotency_key = :idempotencyKey
                """, new MapSqlParameterSource("idempotencyKey", idempotencyKey), Long.class);
        return value != null && value > 0;
    }

    public void insertApprovalHistory(Map<String, ?> values) {
        try {
            jdbc().update("""
                    INSERT INTO bza_approval_history (
                        approval_id, action_type, actor_employee_no, idempotency_key, reason,
                        before_status, after_status, comment_text, transaction_global_id,
                        created_by, updated_by
                    ) VALUES (
                        :approvalId, :actionType, :actorEmployeeNo, :idempotencyKey, :reason,
                        :beforeStatus, :afterStatus, :comment, :transactionGlobalId,
                        :actorEmployeeNo, :actorEmployeeNo
                    )
                    """, values);
        } catch (DuplicateKeyException ex) {
            throw new IllegalStateException("이미 처리된 결재 행위입니다. idempotencyKey=" + values.get("idempotencyKey"), ex);
        }
    }

    public List<Map<String, Object>> findApprovalHistory(long approvalId) {
        return jdbc().queryForList("""
                SELECT approval_history_id AS historyId, action_type AS actionType,
                       actor_employee_no AS actorEmployeeNo, reason, before_status AS beforeStatus,
                       after_status AS afterStatus, comment_text AS comment,
                       transaction_global_id AS transactionGlobalId, created_at AS createdAt
                  FROM bza_approval_history
                 WHERE approval_id = :approvalId ORDER BY approval_history_id
                """, new MapSqlParameterSource("approvalId", approvalId));
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

    public List<Map<String, Object>> findBusinessAudits(int limit) {
        return jdbc().queryForList(sql.required("backoffice-find-business-audits"),
                new MapSqlParameterSource("limit", limit));
    }

    private NamedParameterJdbcTemplate jdbc() {
        NamedParameterJdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate == null) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "BZA DB datasource가 비활성화되어 백오피스 저장소를 사용할 수 없습니다.");
        }
        return jdbcTemplate;
    }
}
