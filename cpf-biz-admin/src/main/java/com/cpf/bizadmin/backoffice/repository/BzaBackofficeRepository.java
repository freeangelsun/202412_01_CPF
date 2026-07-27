package com.cpf.bizadmin.backoffice.repository;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.core.env.Environment;
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
                       organization_type AS organizationType, sort_order AS sortOrder, effective_from AS effectiveFrom, effective_to AS effectiveTo, use_yn AS useYn,
                       version_no AS versionNo, created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_organization
                 ORDER BY sort_order, organization_code
                """, Map.of());
    }

    public int saveOrganization(Map<String, ?> values) {
        if (values.get("expectedVersion") == null && !organizationExists(String.valueOf(values.get("organizationCode")))) {
            return jdbc().update("""
                    INSERT INTO bza_organization (organization_code,parent_organization_code,organization_name,organization_type,sort_order,effective_from,effective_to,use_yn,version_no,created_by,updated_by)
                    VALUES (:organizationCode,:parentOrganizationCode,:organizationName,:organizationType,:sortOrder,:effectiveFrom,:effectiveTo,:useYn,0,:requestUser,:requestUser)
                    """, values);
        }
        return jdbc().update("""
                UPDATE bza_organization SET parent_organization_code=:parentOrganizationCode,organization_name=:organizationName,organization_type=:organizationType,
                       sort_order=:sortOrder,effective_from=:effectiveFrom,effective_to=:effectiveTo,use_yn=:useYn,version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP(3)
                 WHERE organization_code=:organizationCode AND version_no=:expectedVersion
                """, values);
    }

    public boolean organizationExists(String code) {
        Long count=jdbc().queryForObject("SELECT COUNT(*) FROM bza_organization WHERE organization_code=:code",new MapSqlParameterSource("code",code),Long.class);
        return count!=null&&count>0;
    }

    /** parent chain을 재귀 CTE로 조회해 self/descendant parent 지정 cycle을 차단합니다. */
    public boolean wouldCreateOrganizationCycle(String organizationCode,String parentCode) {
        if (parentCode==null) return false;
        if (organizationCode.equalsIgnoreCase(parentCode)) return true;
        Long count=jdbc().queryForObject("""
                WITH RECURSIVE descendants AS (
                    SELECT organization_code FROM bza_organization WHERE parent_organization_code=:organizationCode
                    UNION ALL
                    SELECT o.organization_code FROM bza_organization o JOIN descendants d ON o.parent_organization_code=d.organization_code
                )
                SELECT COUNT(*) FROM descendants WHERE organization_code=:parentCode
                """,new MapSqlParameterSource().addValue("organizationCode",organizationCode).addValue("parentCode",parentCode),Long.class);
        return count!=null&&count>0;
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
                       office_phone_no AS officePhoneNo, use_yn AS useYn, version_no AS versionNo,
                       created_at AS createdAt, updated_at AS updatedAt
                  FROM bza_employee
                 WHERE (:organizationCode IS NULL OR organization_code = :organizationCode)
                   AND (:status IS NULL OR employment_status = :status)
                 ORDER BY organization_code, employee_no
                """, params);
    }

    public int saveEmployee(Map<String, ?> values) {
        String employeeNo=String.valueOf(values.get("employeeNo"));
        Long count=jdbc().queryForObject("SELECT COUNT(*) FROM bza_employee WHERE employee_no=:employeeNo",new MapSqlParameterSource("employeeNo",employeeNo),Long.class);
        if (count==null||count==0) {
            return jdbc().update("""
                    INSERT INTO bza_employee(employee_no,admin_user_id,organization_code,employee_name,position_code,job_title_code,manager_employee_no,employment_status,join_date,leave_date,email,mobile_no,office_phone_no,use_yn,version_no,created_by,updated_by)
                    VALUES(:employeeNo,:adminUserId,:organizationCode,:employeeName,:positionCode,:jobTitleCode,:managerEmployeeNo,:employmentStatus,:joinDate,:leaveDate,:email,:mobileNo,:officePhoneNo,:useYn,0,:requestUser,:requestUser)
                    """,values);
        }
        return jdbc().update("""
                UPDATE bza_employee SET admin_user_id=:adminUserId,organization_code=:organizationCode,employee_name=:employeeName,position_code=:positionCode,job_title_code=:jobTitleCode,
                       manager_employee_no=:managerEmployeeNo,employment_status=:employmentStatus,join_date=:joinDate,leave_date=:leaveDate,email=:email,mobile_no=:mobileNo,office_phone_no=:officePhoneNo,use_yn=:useYn,
                       version_no=version_no+1,updated_by=:requestUser,updated_at=CURRENT_TIMESTAMP(3)
                 WHERE employee_no=:employeeNo AND version_no=:expectedVersion
                """,values);
    }

    public CpfPage<Map<String,Object>> organizationPage(CpfPageRequest page) {
        MapSqlParameterSource q=new MapSqlParameterSource().addValue("limit",page.size()).addValue("offset",page.offset());
        Long total=jdbc().queryForObject("SELECT COUNT(*) FROM bza_organization",new MapSqlParameterSource(),Long.class);
        List<Map<String,Object>> rows=jdbc().queryForList("SELECT organization_id AS organizationId,organization_code AS organizationCode,parent_organization_code AS parentOrganizationCode,organization_name AS organizationName,organization_type AS organizationType,sort_order AS sortOrder,effective_from AS effectiveFrom,effective_to AS effectiveTo,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt FROM bza_organization ORDER BY sort_order,organization_code LIMIT :limit OFFSET :offset",q);
        return new CpfPage<>(rows,total==null?0:total,page.page(),page.size());
    }

    public CpfPage<Map<String,Object>> employeePage(String organizationCode,String status,CpfPageRequest page) {
        MapSqlParameterSource q=new MapSqlParameterSource().addValue("organizationCode",organizationCode).addValue("status",status).addValue("limit",page.size()).addValue("offset",page.offset());
        Long total=jdbc().queryForObject("SELECT COUNT(*) FROM bza_employee WHERE (:organizationCode IS NULL OR organization_code=:organizationCode) AND (:status IS NULL OR employment_status=:status)",q,Long.class);
        List<Map<String,Object>> rows=jdbc().queryForList("SELECT employee_id AS employeeId,employee_no AS employeeNo,admin_user_id AS adminUserId,organization_code AS organizationCode,employee_name AS employeeName,position_code AS positionCode,job_title_code AS jobTitleCode,manager_employee_no AS managerEmployeeNo,employment_status AS employmentStatus,join_date AS joinDate,leave_date AS leaveDate,email,mobile_no AS mobileNo,office_phone_no AS officePhoneNo,use_yn AS useYn,version_no AS versionNo,updated_at AS updatedAt FROM bza_employee WHERE (:organizationCode IS NULL OR organization_code=:organizationCode) AND (:status IS NULL OR employment_status=:status) ORDER BY organization_code,employee_no LIMIT :limit OFFSET :offset",q);
        return new CpfPage<>(rows,total==null?0:total,page.page(),page.size());
    }

    public List<Map<String, Object>> findEffectivePermissions(String loginId) {
        return jdbc().queryForList("""
                SELECT u.admin_login_id AS loginId, p.role_code AS roleCode, p.menu_code AS menuCode,
                       p.button_code AS actionCode, p.permission_type AS permissionType,
                       p.http_method AS httpMethod, p.api_pattern AS apiPattern,
                       p.domain_code AS domainCode, p.environment_code AS environmentCode,
                       p.data_scope AS dataScope, p.allow_yn AS allowYn
                  FROM bza_admin_user u
                  JOIN bza_user_role ur ON ur.admin_user_id=u.admin_user_id
                  JOIN bza_role r ON r.role_code=ur.role_code AND r.use_yn='Y'
                  JOIN bza_permission p ON p.role_code=ur.role_code AND p.use_yn='Y'
                 WHERE u.admin_login_id=:loginId AND u.use_yn='Y'
                   AND (ur.valid_from IS NULL OR ur.valid_from<=CURRENT_TIMESTAMP(3))
                   AND (ur.valid_to IS NULL OR ur.valid_to>CURRENT_TIMESTAMP(3))
                 ORDER BY p.menu_code,p.button_code,p.allow_yn
                """,new MapSqlParameterSource("loginId",loginId));
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
                    attachment_group_id, version_no, transaction_id, created_by, updated_by
                ) VALUES (
                    :approvalNo, :approvalType, :businessDomain, :title, :requesterEmployeeNo,
                    'DRAFT', :approvalMode, 0, :dueAt, :payloadJson,
                    :attachmentGroupId, 0, :transactionId, :requestUser, :requestUser
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
        return jdbc().queryForList("SELECT approval_id AS approvalId,approval_no AS approvalNo,approval_type AS approvalType,business_domain AS businessDomain,title,requester_employee_no AS requesterEmployeeNo,approval_status AS approvalStatus,current_step_no AS currentStepNo,version_no AS versionNo,created_at AS createdAt,updated_at AS updatedAt FROM bza_approval_document WHERE (:status IS NULL OR approval_status=:status) AND (:employeeNo IS NULL OR requester_employee_no=:employeeNo) ORDER BY approval_id DESC LIMIT :limit", new MapSqlParameterSource()
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
                       transaction_id AS transactionId, created_at AS createdAt, updated_at AS updatedAt
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
        return jdbc().update("UPDATE bza_approval_line SET decision_status=:decision,decision_comment=:comment,decided_at=CURRENT_TIMESTAMP(3),updated_at=CURRENT_TIMESTAMP(3) WHERE approval_id=:approvalId AND step_no=:stepNo AND approver_employee_no=:actorEmployeeNo AND decision_status='WAITING'", new MapSqlParameterSource()
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
                        before_status, after_status, comment_text, transaction_id,
                        created_by, updated_by
                    ) VALUES (
                        :approvalId, :actionType, :actorEmployeeNo, :idempotencyKey, :reason,
                        :beforeStatus, :afterStatus, :comment, :transactionId,
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
                       transaction_id AS transactionId, created_at AS createdAt
                  FROM bza_approval_history
                 WHERE approval_id = :approvalId ORDER BY approval_history_id
                """, new MapSqlParameterSource("approvalId", approvalId));
    }

    public void insertBusinessAudit(Map<String, ?> values) {
        jdbc().update("""
                INSERT INTO bza_business_audit (
                    transaction_id, actor_id, action_type, target_type, target_id,
                    reason, before_data, after_data, created_by, updated_by
                ) VALUES (
                    :transactionId, :actorId, :actionType, :targetType, :targetId,
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
