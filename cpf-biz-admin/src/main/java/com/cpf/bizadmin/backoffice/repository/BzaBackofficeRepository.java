package com.cpf.bizadmin.backoffice.repository;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
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
        return jdbc().queryForList(sql.required("backoffice-repository-find-organizations-01"), Map.of());
    }

    public int saveOrganization(Map<String, ?> values) {
        if (values.get("expectedVersion") == null && !organizationExists(String.valueOf(values.get("organizationCode")))) {
            return jdbc().update(sql.required("backoffice-repository-save-organization-01"), values);
        }
        return jdbc().update(sql.required("backoffice-repository-save-organization-02"), values);
    }

    public boolean organizationExists(String code) {
        Long count=jdbc().queryForObject(sql.required("backoffice-repository-organization-exists-01"),new MapSqlParameterSource("code",code),Long.class);
        return count!=null&&count>0;
    }

    /** parent chain을 재귀 CTE로 조회해 self/descendant parent 지정 cycle을 차단합니다. */
    public boolean wouldCreateOrganizationCycle(String organizationCode,String parentCode) {
        if (parentCode==null) return false;
        if (organizationCode.equalsIgnoreCase(parentCode)) return true;
        Long count=jdbc().queryForObject(sql.required("backoffice-repository-would-create-organization-cycle-01"),new MapSqlParameterSource().addValue("organizationCode",organizationCode).addValue("parentCode",parentCode),Long.class);
        return count!=null&&count>0;
    }

    public List<Map<String, Object>> findEmployees(String organizationCode, String status) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("organizationCode", organizationCode)
                .addValue("status", status);
        return jdbc().queryForList(sql.required("backoffice-repository-find-employees-01"), params);
    }

    public Optional<Map<String,Object>> findEmployee(String employeeNo) {
        return jdbc().queryForList(sql.required("backoffice-repository-find-employee-01"),new MapSqlParameterSource("employeeNo",employeeNo)).stream().findFirst();
    }

    public int saveEmployee(Map<String, ?> values) {
        String employeeNo=String.valueOf(values.get("employeeNo"));
        Long count=jdbc().queryForObject(sql.required("backoffice-repository-save-employee-03"),new MapSqlParameterSource("employeeNo",employeeNo),Long.class);
        if (count==null||count==0) {
            return jdbc().update(sql.required("backoffice-repository-save-employee-01"),values);
        }
        return jdbc().update(sql.required("backoffice-repository-save-employee-02"),values);
    }

    public CpfPage<Map<String,Object>> organizationPage(CpfPageRequest page) {
        MapSqlParameterSource q=new MapSqlParameterSource().addValue("limit",page.size()).addValue("offset",page.offset());
        Long total=jdbc().queryForObject(sql.required("backoffice-repository-organization-page-01"),new MapSqlParameterSource(),Long.class);
        List<Map<String,Object>> rows=jdbc().queryForList(sql.required("backoffice-repository-organization-page-02"),q);
        return new CpfPage<>(rows,total==null?0:total,page.page(),page.size());
    }

    public CpfPage<Map<String,Object>> employeePage(String organizationCode,String status,CpfPageRequest page) {
        MapSqlParameterSource q=new MapSqlParameterSource().addValue("organizationCode",organizationCode).addValue("status",status).addValue("limit",page.size()).addValue("offset",page.offset());
        Long total=jdbc().queryForObject(sql.required("backoffice-repository-employee-page-01"),q,Long.class);
        List<Map<String,Object>> rows=jdbc().queryForList(sql.required("backoffice-repository-employee-page-02"),q);
        return new CpfPage<>(rows,total==null?0:total,page.page(),page.size());
    }

    public List<Map<String, Object>> findEffectivePermissions(String loginId) {
        return jdbc().queryForList(sql.required("backoffice-repository-find-effective-permissions-01"),new MapSqlParameterSource("loginId",loginId));
    }

    /** 인증된 BZA 로그인 ID와 결재 처리용 직원 번호의 바인딩을 조회합니다. */
    public Optional<String> findEmployeeNoByLoginId(String loginId) {
        return jdbc().queryForList(sql.required("backoffice-repository-find-employee-no-by-login-id-01"), new MapSqlParameterSource("loginId", loginId)).stream()
                .map(row -> String.valueOf(row.get("employeeNo")))
                .findFirst();
    }

    public long createApproval(Map<String, ?> values) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc().update(sql.required("backoffice-repository-create-approval-01"), new MapSqlParameterSource(values), keyHolder, new String[]{"approval_id"});
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("결재 문서 생성 키를 확인할 수 없습니다.");
        }
        return key.longValue();
    }

    public void addApprovalLine(long approvalId, int stepNo, String approverEmployeeNo, String decisionRule, String requestUser) {
        jdbc().update(sql.required("backoffice-repository-add-approval-line-01"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("stepNo", stepNo)
                .addValue("approverEmployeeNo", approverEmployeeNo)
                .addValue("decisionRule", decisionRule)
                .addValue("requestUser", requestUser));
    }

    public List<Map<String, Object>> findApprovals(String status, String employeeNo, int limit) {
        return jdbc().queryForList(sql.required("backoffice-repository-find-approvals-01"), new MapSqlParameterSource()
                .addValue("status", status)
                .addValue("employeeNo", employeeNo)
                .addValue("limit", limit));
    }

    public Optional<Map<String, Object>> findApproval(long approvalId) {
        List<Map<String, Object>> rows = jdbc().queryForList(sql.required("backoffice-repository-find-approval-01"), new MapSqlParameterSource("approvalId", approvalId));
        return rows.stream().findFirst();
    }

    public List<Map<String, Object>> findApprovalLines(long approvalId) {
        return jdbc().queryForList(sql.required("backoffice-repository-find-approval-lines-01"), new MapSqlParameterSource("approvalId", approvalId));
    }

    public int decideLine(long approvalId, int stepNo, String actorEmployeeNo, String decision, String comment) {
        return jdbc().update(sql.required("backoffice-repository-decide-line-01"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("stepNo", stepNo)
                .addValue("actorEmployeeNo", actorEmployeeNo)
                .addValue("decision", decision)
                .addValue("comment", comment));
    }

    public long countWaitingAtStep(long approvalId, int stepNo) {
        Long value = jdbc().queryForObject(sql.required("backoffice-repository-count-waiting-at-step-01"), new MapSqlParameterSource().addValue("approvalId", approvalId).addValue("stepNo", stepNo), Long.class);
        return value == null ? 0 : value;
    }

    public Integer nextStep(long approvalId, int currentStep) {
        return jdbc().queryForObject(sql.required("backoffice-repository-next-step-01"), new MapSqlParameterSource().addValue("approvalId", approvalId).addValue("currentStep", currentStep), Integer.class);
    }

    public int updateApprovalStatus(long approvalId, long expectedVersion, String status, int currentStep, String actor) {
        return jdbc().update(sql.required("backoffice-repository-update-approval-status-01"), new MapSqlParameterSource()
                .addValue("approvalId", approvalId)
                .addValue("expectedVersion", expectedVersion)
                .addValue("status", status)
                .addValue("currentStep", currentStep)
                .addValue("actor", actor));
    }

    public boolean approvalActionExists(String idempotencyKey) {
        Long value = jdbc().queryForObject(sql.required("backoffice-repository-approval-action-exists-01"), new MapSqlParameterSource("idempotencyKey", idempotencyKey), Long.class);
        return value != null && value > 0;
    }

    public void insertApprovalHistory(Map<String, ?> values) {
        try {
            jdbc().update(sql.required("backoffice-repository-insert-approval-history-01"), values);
        } catch (DuplicateKeyException ex) {
            throw new IllegalStateException("이미 처리된 결재 행위입니다. idempotencyKey=" + values.get("idempotencyKey"), ex);
        }
    }

    public List<Map<String, Object>> findApprovalHistory(long approvalId) {
        return jdbc().queryForList(sql.required("backoffice-repository-find-approval-history-01"), new MapSqlParameterSource("approvalId", approvalId));
    }

    public void insertBusinessAudit(Map<String, ?> values) {
        jdbc().update(sql.required("backoffice-repository-insert-business-audit-01"), values);
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
