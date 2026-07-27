package com.cpf.bizadmin.backoffice.repository;

import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.sql = sqlCatalogProvider.forModule("bza");
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

    /** 원문 조회 API용 최소 Projection입니다. 일반 직원 Row 전체를 메모리로 올리지 않습니다. */
    public Optional<Map<String,Object>> findEmployeeRawContact(String employeeNo) {
        return jdbc().queryForList(
                sql.required("backoffice-repository-find-employee-raw-contact-01"),
                new MapSqlParameterSource("employeeNo", employeeNo)).stream().findFirst();
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
