package com.cpf.backoffice.online.operation.repository;


import com.cpf.data.persistence.api.CpfRepository;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.foundation.api.page.CpfPage;
import com.cpf.foundation.api.page.CpfPageRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

/** MBW 운영 정본 Repository. 조회는 서버 Paging, 변경은 version_no CAS를 사용합니다. */
@CpfRepository
public class BackofficeOperationRepository extends com.cpf.backoffice.online.base.BackofficeBaseRepository {
  private final ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider;
  private final CpfVendorSqlCatalog sql;

  public BackofficeOperationRepository(
      @Qualifier("MBW_JDBC_TEMPLATE") ObjectProvider<NamedParameterJdbcTemplate> jdbcTemplateProvider,
      com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider sqlCatalogProvider) {
    this.jdbcTemplateProvider = jdbcTemplateProvider;
    this.sql = sqlCatalogProvider.forModule("backoffice");
  }

  /** findAdminUsers 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findAdminUsers() {
    return adminUserPage(new CpfPageRequest(0, 200)).content();
  }

  public CpfPage<Map<String, Object>> adminUserPage(CpfPageRequest p) {
    return page(
        sql.required("operation-repository-admin-user-page-01"),
        sql.required("operation-repository-admin-user-page-02"),
        Map.of(),
        p);
  }

  /** findAdminUser 작업을 CPF 표준 계약에 따라 수행한다. */
  public Optional<Map<String, Object>> findAdminUser(String loginId) {
    return jdbc()
        .queryForList(
            sql.required("operation-repository-find-admin-user-01"),
            new MapSqlParameterSource("loginId", loginId))
        .stream()
        .findFirst();
  }

  /** insertAdminUser 작업을 CPF 표준 계약에 따라 수행한다. */
  public int insertAdminUser(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-insert-admin-user-01"), v);
  }

  public int updateAdminUser(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-update-admin-user-01"), v);
  }

  /** countEffectiveRoles 작업을 CPF 표준 계약에 따라 수행한다. */
  public long countEffectiveRoles(String loginId) {
    Long value =
        jdbc()
            .queryForObject(
                sql.required("operation-repository-count-effective-roles-01"),
                new MapSqlParameterSource("loginId", loginId),
                Long.class);
    return value == null ? 0 : value;
  }

  /** ensureInitialUserRole 작업을 CPF 표준 계약에 따라 수행한다. */
  public void ensureInitialUserRole(String loginId, String roleCode, String actor) {
    jdbc()
        .update(
            sql.required("operation-repository-ensure-initial-user-role-01"),
            new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("roleCode", roleCode)
                .addValue("actor", actor));
    syncLegacyPrimaryRole(loginId, roleCode, actor);
  }

  /** syncLegacyPrimaryRole 작업을 CPF 표준 계약에 따라 수행한다. */
  public void syncLegacyPrimaryRole(String loginId, String roleCode, String actor) {
    jdbc()
        .update(
            sql.required("operation-repository-sync-legacy-primary-role-01"),
            new MapSqlParameterSource()
                .addValue("loginId", loginId)
                .addValue("roleCode", roleCode)
                .addValue("actor", actor));
  }

  /** findMenus 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findMenus() {
    return menuPage(new CpfPageRequest(0, 200)).content();
  }

  public CpfPage<Map<String, Object>> menuPage(CpfPageRequest p) {
    return page(
        sql.required("operation-repository-menu-page-01"),
        sql.required("operation-repository-menu-page-02"),
        Map.of(),
        p);
  }

  /** findMenu 작업을 CPF 표준 계약에 따라 수행한다. */
  public Optional<Map<String, Object>> findMenu(String code) {
    return jdbc()
        .queryForList(
            sql.required("operation-repository-find-menu-01"),
            new MapSqlParameterSource("code", code))
        .stream()
        .findFirst();
  }

  /** insertMenu 작업을 CPF 표준 계약에 따라 수행한다. */
  public int insertMenu(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-insert-menu-01"), v);
  }

  public int updateMenu(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-update-menu-01"), v);
  }

  /** 메뉴 순환·하위 영향 검사를 위해 전체 hierarchy만 가볍게 조회합니다. */
  public List<Map<String, Object>> findMenuHierarchy() {
    return jdbc().queryForList(
        sql.required("operation-repository-find-menu-hierarchy-01"), Map.of());
  }

  public long countMenuPermissions(String menuCode) {
    Long value =
        jdbc()
            .queryForObject(
                sql.required("operation-repository-count-menu-permissions-01"),
                new MapSqlParameterSource("menuCode", menuCode),
                Long.class);
    return value == null ? 0 : value;
  }

  /** deleteMenu 작업을 CPF 표준 계약에 따라 수행한다. */
  public int deleteMenu(String menuCode, long expectedVersion) {
    return jdbc()
        .update(
            sql.required("operation-repository-delete-menu-01"),
            new MapSqlParameterSource()
                .addValue("menuCode", menuCode)
                .addValue("expectedVersion", expectedVersion));
  }

  /** findRoles 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findRoles() {
    return rolePage(new CpfPageRequest(0, 200)).content();
  }

  public CpfPage<Map<String, Object>> rolePage(CpfPageRequest p) {
    return page(
        sql.required("operation-repository-role-page-01"),
        sql.required("operation-repository-role-page-02"),
        Map.of(),
        p);
  }

  /** findRole 작업을 CPF 표준 계약에 따라 수행한다. */
  public Optional<Map<String, Object>> findRole(String code) {
    return jdbc()
        .queryForList(
            sql.required("operation-repository-find-role-01"),
            new MapSqlParameterSource("code", code))
        .stream()
        .findFirst();
  }

  /** insertRole 작업을 CPF 표준 계약에 따라 수행한다. */
  public int insertRole(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-insert-role-01"), v);
  }

  public int updateRole(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-update-role-01"), v);
  }

  /** findPermissions 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findPermissions() {
    return permissionPage(new CpfPageRequest(0, 200)).content();
  }

  public CpfPage<Map<String, Object>> permissionPage(CpfPageRequest p) {
    return page(
        sql.required("operation-repository-permission-page-01"),
        sql.required("operation-repository-permission-page-02"),
        Map.of(),
        p);
  }

  /** findPermission 작업을 CPF 표준 계약에 따라 수행한다. */
  public Optional<Map<String, Object>> findPermission(Long id) {
    if (id == null) return Optional.empty();
    return jdbc()
        .queryForList(
            sql.required("operation-repository-find-permission-01"),
            new MapSqlParameterSource("id", id))
        .stream()
        .findFirst();
  }

  /** insertPermission 작업을 CPF 표준 계약에 따라 수행한다. */
  public int insertPermission(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-insert-permission-01"), v);
  }

  public int updatePermission(Map<String, ?> v) {
    return jdbc().update(sql.required("operation-repository-update-permission-01"), v);
  }

  /** findSettings 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findSettings() {
    return jdbc().queryForList(sql.required("operation-repository-find-settings-01"), Map.of());
  }

  public List<Map<String, Object>> findDownloadPolicies() {
    return jdbc()
        .queryForList(sql.required("operation-repository-find-download-policies-01"), Map.of());
  }

  /** insertBusinessAudit 작업을 CPF 표준 계약에 따라 수행한다. */
  public void insertBusinessAudit(Map<String, ?> v) {
    jdbc().update(sql.required("operation-repository-insert-business-audit-01"), v);
  }

  private CpfPage<Map<String, Object>> page(
      String sql, String countSql, Map<String, ?> params, CpfPageRequest p) {
    MapSqlParameterSource q =
        new MapSqlParameterSource(params)
            .addValue("limit", p.size())
            .addValue("offset", p.offset());
    Long total = jdbc().queryForObject(countSql, new MapSqlParameterSource(params), Long.class);
    return new CpfPage<>(
        jdbc().queryForList(sql, q), total == null ? 0 : total, p.page(), p.size());
  }

  private NamedParameterJdbcTemplate jdbc() {
    NamedParameterJdbcTemplate j = jdbcTemplateProvider.getIfAvailable();
    if (j == null)
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "MBW DB datasource가 비활성화되어 운영 저장소를 사용할 수 없습니다.");
    return j;
  }
}
