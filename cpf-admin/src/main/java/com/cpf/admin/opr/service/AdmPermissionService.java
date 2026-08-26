package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmApiPermission;
import com.cpf.admin.opr.dto.AdmApiPermissionSaveRequest;
import com.cpf.admin.opr.dto.AdmButton;
import com.cpf.admin.opr.dto.AdmButtonSaveRequest;
import com.cpf.admin.opr.dto.AdmMenuManagement;
import com.cpf.admin.opr.dto.AdmMenuSaveRequest;
import com.cpf.admin.opr.dto.AdmRoleManagement;
import com.cpf.admin.opr.dto.AdmRoleSaveRequest;
import com.cpf.admin.opr.dto.AdmStatusUpdateRequest;
import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.foundation.annotation.CpfService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * ADM 역할, 메뉴, 버튼, API 권한 매트릭스를 조회하고 변경합니다.
 */
@CpfService
public class AdmPermissionService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmPermissionService.class);

    private final JdbcTemplate admJdbcTemplate;
    private final AdmPersistencePolicy persistencePolicy;

    public AdmPermissionService(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            AdmPersistencePolicy persistencePolicy) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.persistencePolicy = persistencePolicy;
    }

    public List<AdmRoleManagement> findRoles() {
        try {
            return admJdbcTemplate.query("""
                    SELECT ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_role
                    ORDER BY ROLE_ID
                    """, (rs, rowNum) -> new AdmRoleManagement(
                    rs.getString("ROLE_ID"),
                    rs.getString("ROLE_NAME"),
                    rs.getString("ROLE_TYPE"),
                    rs.getString("DESCRIPTION"),
                    rs.getString("USE_YN"),
                    stringTime(rs.getTimestamp("CREATED_AT")),
                    stringTime(rs.getTimestamp("UPDATED_AT"))));
        } catch (DataAccessException ex) {
            return readFailure("adm_role.list", ex, List.of());
        }
    }

    public AdmRoleManagement findRole(String roleId) {
        try {
            return admJdbcTemplate.queryForObject("""
                    SELECT ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_role
                    WHERE ROLE_ID = ?
                    """, (rs, rowNum) -> new AdmRoleManagement(
                    rs.getString("ROLE_ID"),
                    rs.getString("ROLE_NAME"),
                    rs.getString("ROLE_TYPE"),
                    rs.getString("DESCRIPTION"),
                    rs.getString("USE_YN"),
                    stringTime(rs.getTimestamp("CREATED_AT")),
                    stringTime(rs.getTimestamp("UPDATED_AT"))), roleId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CpfNotFoundException("ADM 역할을 찾을 수 없습니다. roleId=" + roleId);
        } catch (DataAccessException ex) {
            throw unavailable("adm_role.find", ex);
        }
    }

    public AdmRoleManagement createRole(AdmRoleSaveRequest request) {
        String roleId = CpfStrings.requireText(request.roleId(), "roleId");
        String roleName = CpfStrings.requireText(request.roleName(), "roleName");
        String roleType = CpfStrings.defaultIfBlank(request.roleType(), "BUSINESS_OPERATOR");
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_role (
                    ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, roleId, roleName, roleType, request.description(), ynDefaultY(request.useYn()), user, user);
        return findRole(roleId);
    }

    public AdmRoleManagement updateRole(String roleId, AdmRoleSaveRequest request) {
        String roleName = CpfStrings.requireText(request.roleName(), "roleName");
        String roleType = CpfStrings.defaultIfBlank(request.roleType(), "BUSINESS_OPERATOR");
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_role
                SET ROLE_NAME = ?,
                    ROLE_TYPE = ?,
                    DESCRIPTION = ?,
                    USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE ROLE_ID = ?
                """, roleName, roleType, request.description(), ynDefaultY(request.useYn()), user, roleId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM 역할을 찾을 수 없습니다. roleId=" + roleId);
        }
        return findRole(roleId);
    }

    public AdmRoleManagement updateRoleStatus(String roleId, AdmStatusUpdateRequest request) {
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_role
                SET USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE ROLE_ID = ?
                """, ynDefaultY(request.useYn()), user, roleId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM 역할을 찾을 수 없습니다. roleId=" + roleId);
        }
        return findRole(roleId);
    }

    public List<AdmMenuManagement> findManagedMenus() {
        try {
            return admJdbcTemplate.query("""
                    SELECT MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_menu
                    ORDER BY SORT_ORDER, MENU_ID
                    """, (rs, rowNum) -> menuManagement(rs.getString("MENU_ID"),
                    rs.getString("PARENT_MENU_ID"),
                    rs.getString("MENU_NAME"),
                    rs.getString("MENU_PATH"),
                    rs.getInt("SORT_ORDER"),
                    rs.getString("USE_YN"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")));
        } catch (DataAccessException ex) {
            return readFailure("adm_menu.list", ex, List.of());
        }
    }

    public AdmMenuManagement findManagedMenu(String menuId) {
        try {
            return admJdbcTemplate.queryForObject("""
                    SELECT MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_menu
                    WHERE MENU_ID = ?
                    """, (rs, rowNum) -> menuManagement(rs.getString("MENU_ID"),
                    rs.getString("PARENT_MENU_ID"),
                    rs.getString("MENU_NAME"),
                    rs.getString("MENU_PATH"),
                    rs.getInt("SORT_ORDER"),
                    rs.getString("USE_YN"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")), menuId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CpfNotFoundException("ADM 메뉴를 찾을 수 없습니다. menuId=" + menuId);
        } catch (DataAccessException ex) {
            throw unavailable("adm_menu.find", ex);
        }
    }

    public AdmMenuManagement createMenu(AdmMenuSaveRequest request) {
        String menuId = CpfStrings.requireText(request.menuId(), "menuId");
        validateMenuParent(menuId, request.parentMenuId());
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_menu (
                    MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                menuId,
                blankToNull(request.parentMenuId()),
                CpfStrings.requireText(request.menuName(), "menuName"),
                CpfStrings.defaultIfBlank(request.menuPath(), "/adm"),
                defaultInt(request.sortOrder()),
                ynDefaultY(request.useYn()),
                user,
                user);
        return findManagedMenu(menuId);
    }

    public AdmMenuManagement updateMenu(String menuId, AdmMenuSaveRequest request) {
        validateMenuParent(menuId, request.parentMenuId());
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_menu
                SET PARENT_MENU_ID = ?,
                    MENU_NAME = ?,
                    MENU_PATH = ?,
                    SORT_ORDER = ?,
                    USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE MENU_ID = ?
                """,
                blankToNull(request.parentMenuId()),
                CpfStrings.requireText(request.menuName(), "menuName"),
                CpfStrings.defaultIfBlank(request.menuPath(), "/adm"),
                defaultInt(request.sortOrder()),
                ynDefaultY(request.useYn()),
                user,
                menuId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM 메뉴를 찾을 수 없습니다. menuId=" + menuId);
        }
        return findManagedMenu(menuId);
    }

    public AdmMenuManagement updateMenuStatus(String menuId, AdmStatusUpdateRequest request) {
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_menu
                SET USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE MENU_ID = ?
                """, ynDefaultY(request.useYn()), user, menuId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM 메뉴를 찾을 수 없습니다. menuId=" + menuId);
        }
        return findManagedMenu(menuId);
    }

    public List<AdmButton> findButtons(String menuId) {
        try {
            if (CpfStrings.hasText(menuId)) {
                return admJdbcTemplate.query("""
                        SELECT BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN,
                               SORT_ORDER, USE_YN, CREATED_AT, UPDATED_AT
                        FROM adm_button
                        WHERE MENU_ID = ?
                        ORDER BY MENU_ID, SORT_ORDER, BUTTON_ID
                        """, (rs, rowNum) -> button(rs), menuId);
            }
            return admJdbcTemplate.query("""
                    SELECT BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN,
                           SORT_ORDER, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_button
                    ORDER BY MENU_ID, SORT_ORDER, BUTTON_ID
                    """, (rs, rowNum) -> button(rs));
        } catch (DataAccessException ex) {
            return readFailure("adm_button.list", ex, List.of());
        }
    }

    public AdmButton findButton(String buttonId) {
        try {
            return admJdbcTemplate.queryForObject("""
                    SELECT BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN,
                           SORT_ORDER, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_button
                    WHERE BUTTON_ID = ?
                    """, (rs, rowNum) -> button(rs), buttonId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CpfNotFoundException("ADM 버튼을 찾을 수 없습니다. buttonId=" + buttonId);
        } catch (DataAccessException ex) {
            throw unavailable("adm_button.find", ex);
        }
    }

    public AdmButton createButton(AdmButtonSaveRequest request) {
        String buttonId = CpfStrings.requireText(request.buttonId(), "buttonId");
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_button (
                    BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN,
                    SORT_ORDER, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                buttonId,
                CpfStrings.requireText(request.menuId(), "menuId"),
                CpfStrings.requireText(request.actionCode(), "actionCode"),
                CpfStrings.requireText(request.buttonName(), "buttonName"),
                normalizeMethod(request.httpMethod()),
                request.apiPattern(),
                defaultInt(request.sortOrder()),
                ynDefaultY(request.useYn()),
                user,
                user);
        return findButton(buttonId);
    }

    public AdmButton updateButton(String buttonId, AdmButtonSaveRequest request) {
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_button
                SET MENU_ID = ?,
                    ACTION_CODE = ?,
                    BUTTON_NAME = ?,
                    HTTP_METHOD = ?,
                    API_PATTERN = ?,
                    SORT_ORDER = ?,
                    USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE BUTTON_ID = ?
                """,
                CpfStrings.requireText(request.menuId(), "menuId"),
                CpfStrings.requireText(request.actionCode(), "actionCode"),
                CpfStrings.requireText(request.buttonName(), "buttonName"),
                normalizeMethod(request.httpMethod()),
                request.apiPattern(),
                defaultInt(request.sortOrder()),
                ynDefaultY(request.useYn()),
                user,
                buttonId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM 버튼을 찾을 수 없습니다. buttonId=" + buttonId);
        }
        return findButton(buttonId);
    }

    public AdmButton updateButtonStatus(String buttonId, AdmStatusUpdateRequest request) {
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_button
                SET USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE BUTTON_ID = ?
                """, ynDefaultY(request.useYn()), user, buttonId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM 버튼을 찾을 수 없습니다. buttonId=" + buttonId);
        }
        return findButton(buttonId);
    }

    public List<AdmApiPermission> findApiPermissions() {
        try {
            return admJdbcTemplate.query("""
                    SELECT API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
                           MENU_ID, BUTTON_ID, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_api_permission
                    ORDER BY API_GROUP_CODE, HTTP_METHOD, API_PATH, API_PERMISSION_ID
                    """, (rs, rowNum) -> apiPermission(rs.getString("API_PERMISSION_ID"),
                    rs.getString("API_GROUP_CODE"),
                    rs.getString("HTTP_METHOD"),
                    rs.getString("API_PATH"),
                    rs.getString("API_NAME"),
                    rs.getString("PERMISSION_CODE"),
                    rs.getString("MENU_ID"),
                    rs.getString("BUTTON_ID"),
                    rs.getString("USE_YN"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")));
        } catch (DataAccessException ex) {
            return readFailure("adm_api_permission.list", ex, List.of());
        }
    }

    public AdmApiPermission findApiPermission(String apiPermissionId) {
        try {
            return admJdbcTemplate.queryForObject("""
                    SELECT API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
                           MENU_ID, BUTTON_ID, USE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_api_permission
                    WHERE API_PERMISSION_ID = ?
                    """, (rs, rowNum) -> apiPermission(rs.getString("API_PERMISSION_ID"),
                    rs.getString("API_GROUP_CODE"),
                    rs.getString("HTTP_METHOD"),
                    rs.getString("API_PATH"),
                    rs.getString("API_NAME"),
                    rs.getString("PERMISSION_CODE"),
                    rs.getString("MENU_ID"),
                    rs.getString("BUTTON_ID"),
                    rs.getString("USE_YN"),
                    rs.getTimestamp("CREATED_AT"),
                    rs.getTimestamp("UPDATED_AT")), apiPermissionId);
        } catch (EmptyResultDataAccessException ex) {
            throw new CpfNotFoundException("ADM API 권한을 찾을 수 없습니다. apiPermissionId=" + apiPermissionId);
        } catch (DataAccessException ex) {
            throw unavailable("adm_api_permission.find", ex);
        }
    }

    public AdmApiPermission createApiPermission(AdmApiPermissionSaveRequest request) {
        String apiPermissionId = CpfStrings.requireText(request.apiPermissionId(), "apiPermissionId");
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_api_permission (
                    API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
                    MENU_ID, BUTTON_ID, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                apiPermissionId,
                CpfStrings.defaultIfBlank(request.apiGroupCode(), "ADM"),
                normalizeMethod(request.httpMethod()),
                CpfStrings.requireText(request.apiPath(), "apiPath"),
                CpfStrings.requireText(request.apiName(), "apiName"),
                CpfStrings.defaultIfBlank(request.permissionCode(), "READ"),
                blankToNull(request.menuId()),
                blankToNull(request.buttonId()),
                ynDefaultY(request.useYn()),
                user,
                user);
        return findApiPermission(apiPermissionId);
    }

    public AdmApiPermission updateApiPermission(String apiPermissionId, AdmApiPermissionSaveRequest request) {
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_api_permission
                SET API_GROUP_CODE = ?,
                    HTTP_METHOD = ?,
                    API_PATH = ?,
                    API_NAME = ?,
                    PERMISSION_CODE = ?,
                    MENU_ID = ?,
                    BUTTON_ID = ?,
                    USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE API_PERMISSION_ID = ?
                """,
                CpfStrings.defaultIfBlank(request.apiGroupCode(), "ADM"),
                normalizeMethod(request.httpMethod()),
                CpfStrings.requireText(request.apiPath(), "apiPath"),
                CpfStrings.requireText(request.apiName(), "apiName"),
                CpfStrings.defaultIfBlank(request.permissionCode(), "READ"),
                blankToNull(request.menuId()),
                blankToNull(request.buttonId()),
                ynDefaultY(request.useYn()),
                user,
                apiPermissionId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM API 권한을 찾을 수 없습니다. apiPermissionId=" + apiPermissionId);
        }
        return findApiPermission(apiPermissionId);
    }

    public AdmApiPermission updateApiPermissionStatus(String apiPermissionId, AdmStatusUpdateRequest request) {
        String user = requestUser(request.requestUser());
        int updated = admJdbcTemplate.update("""
                UPDATE adm_api_permission
                SET USE_YN = ?,
                    UPDATED_BY = ?,
                    UPDATED_AT = CURRENT_TIMESTAMP
                WHERE API_PERMISSION_ID = ?
                """, ynDefaultY(request.useYn()), user, apiPermissionId);
        if (updated == 0) {
            throw new CpfNotFoundException("ADM API 권한을 찾을 수 없습니다. apiPermissionId=" + apiPermissionId);
        }
        return findApiPermission(apiPermissionId);
    }

    public List<Map<String, Object>> findApiPermissionMatrix() {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT r.ROLE_ID, r.ROLE_NAME, a.API_PERMISSION_ID, a.API_GROUP_CODE,
                           a.HTTP_METHOD, a.API_PATH, a.API_NAME, a.PERMISSION_CODE,
                           a.MENU_ID, a.BUTTON_ID, COALESCE(ra.ALLOW_YN, 'N') AS ALLOW_YN
                    FROM adm_role r
                    CROSS JOIN adm_api_permission a
                    LEFT JOIN adm_role_api_permission ra
                           ON ra.ROLE_ID = r.ROLE_ID
                          AND ra.API_PERMISSION_ID = a.API_PERMISSION_ID
                    WHERE r.USE_YN = 'Y'
                      AND a.USE_YN = 'Y'
                    ORDER BY r.ROLE_ID, a.API_GROUP_CODE, a.HTTP_METHOD, a.API_PATH
                    """);
        } catch (DataAccessException ex) {
            return readFailure("adm_api_permission.matrix", ex, List.of());
        }
    }

    public Map<String, Object> findRoleApiPermission(String roleId, String apiPermissionId) {
        try {
            return admJdbcTemplate.queryForMap("""
                    SELECT ROLE_ID, API_PERMISSION_ID, ALLOW_YN, CREATED_AT, UPDATED_AT
                    FROM adm_role_api_permission
                    WHERE ROLE_ID = ?
                      AND API_PERMISSION_ID = ?
                    """, roleId, apiPermissionId);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        } catch (DataAccessException ex) {
            return readFailure("adm_role_api_permission.find", ex, Map.of());
        }
    }

    public Map<String, Object> updateRoleApiPermission(
            String roleId,
            String apiPermissionId,
            String allowYn,
            String requestUser) {
        String user = requestUser(requestUser);
        String normalizedAllow = yn(allowYn);
        int updated = admJdbcTemplate.update("""
                UPDATE adm_role_api_permission
                   SET ALLOW_YN = ?, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                 WHERE ROLE_ID = ? AND API_PERMISSION_ID = ?
                """, normalizedAllow, user, roleId, apiPermissionId);
        if (updated == 0) {
            try {
                admJdbcTemplate.update("""
                        INSERT INTO adm_role_api_permission (ROLE_ID, API_PERMISSION_ID, ALLOW_YN, CREATED_BY, UPDATED_BY)
                        VALUES (?, ?, ?, ?, ?)
                        """, roleId, apiPermissionId, normalizedAllow, user, user);
            } catch (DuplicateKeyException race) {
                admJdbcTemplate.update("""
                        UPDATE adm_role_api_permission
                           SET ALLOW_YN = ?, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                         WHERE ROLE_ID = ? AND API_PERMISSION_ID = ?
                        """, normalizedAllow, user, roleId, apiPermissionId);
            }
        }
        return findRoleApiPermission(roleId, apiPermissionId);
    }

    public List<Map<String, Object>> findMenuPermissions() {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT r.ROLE_ID, r.ROLE_NAME, m.MENU_ID, m.MENU_NAME, m.MENU_PATH,
                           COALESCE(rm.READ_YN, 'N') AS READ_YN,
                           COALESCE(rm.WRITE_YN, 'N') AS WRITE_YN,
                           COALESCE(rm.DELETE_YN, 'N') AS DELETE_YN
                    FROM adm_role r
                    CROSS JOIN adm_menu m
                    LEFT JOIN adm_role_menu rm ON rm.ROLE_ID = r.ROLE_ID AND rm.MENU_ID = m.MENU_ID
                    WHERE r.USE_YN = 'Y'
                      AND m.USE_YN = 'Y'
                    ORDER BY r.ROLE_ID, m.SORT_ORDER, m.MENU_ID
                    """);
        } catch (DataAccessException ex) {
            return readFailure("adm_role_menu.matrix", ex, List.of());
        }
    }

    public List<Map<String, Object>> findButtonPermissions() {
        try {
            return admJdbcTemplate.queryForList("""
                    SELECT r.ROLE_ID, r.ROLE_NAME, b.BUTTON_ID, b.MENU_ID, b.ACTION_CODE,
                           b.BUTTON_NAME, b.HTTP_METHOD, b.API_PATTERN,
                           COALESCE(rb.ALLOW_YN, 'N') AS ALLOW_YN
                    FROM adm_role r
                    CROSS JOIN adm_button b
                    LEFT JOIN adm_role_button rb ON rb.ROLE_ID = r.ROLE_ID AND rb.BUTTON_ID = b.BUTTON_ID
                    WHERE r.USE_YN = 'Y'
                      AND b.USE_YN = 'Y'
                    ORDER BY r.ROLE_ID, b.MENU_ID, b.SORT_ORDER, b.BUTTON_ID
                    """);
        } catch (DataAccessException ex) {
            return readFailure("adm_role_button.matrix", ex, List.of());
        }
    }

    public Map<String, Object> findMenuPermission(String roleId, String menuId) {
        try {
            return admJdbcTemplate.queryForMap("""
                    SELECT ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, CREATED_AT, UPDATED_AT
                    FROM adm_role_menu
                    WHERE ROLE_ID = ?
                      AND MENU_ID = ?
                    """, roleId, menuId);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        } catch (DataAccessException ex) {
            return readFailure("adm_role_menu.find", ex, Map.of());
        }
    }

    public Map<String, Object> findButtonPermission(String roleId, String buttonId) {
        try {
            return admJdbcTemplate.queryForMap("""
                    SELECT ROLE_ID, BUTTON_ID, ALLOW_YN, CREATED_AT, UPDATED_AT
                    FROM adm_role_button
                    WHERE ROLE_ID = ?
                      AND BUTTON_ID = ?
                    """, roleId, buttonId);
        } catch (EmptyResultDataAccessException ex) {
            return Map.of();
        } catch (DataAccessException ex) {
            return readFailure("adm_role_button.find", ex, Map.of());
        }
    }

    public Map<String, Object> updateMenuPermission(
            String roleId,
            String menuId,
            String readYn,
            String writeYn,
            String deleteYn,
            String requestUser) {
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        String read = yn(readYn);
        String write = yn(writeYn);
        String delete = yn(deleteYn);
        int updated = admJdbcTemplate.update("""
                UPDATE adm_role_menu
                   SET READ_YN = ?, WRITE_YN = ?, DELETE_YN = ?, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                 WHERE ROLE_ID = ? AND MENU_ID = ?
                """, read, write, delete, user, roleId, menuId);
        if (updated == 0) {
            try {
                admJdbcTemplate.update("""
                        INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, CREATED_BY, UPDATED_BY)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """, roleId, menuId, read, write, delete, user, user);
            } catch (DuplicateKeyException race) {
                admJdbcTemplate.update("""
                        UPDATE adm_role_menu
                           SET READ_YN = ?, WRITE_YN = ?, DELETE_YN = ?, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                         WHERE ROLE_ID = ? AND MENU_ID = ?
                        """, read, write, delete, user, roleId, menuId);
            }
        }
        return findMenuPermission(roleId, menuId);
    }

    public Map<String, Object> updateButtonPermission(
            String roleId,
            String buttonId,
            String allowYn,
            String requestUser) {
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        String allow = yn(allowYn);
        int updated = admJdbcTemplate.update("""
                UPDATE adm_role_button
                   SET ALLOW_YN = ?, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                 WHERE ROLE_ID = ? AND BUTTON_ID = ?
                """, allow, user, roleId, buttonId);
        if (updated == 0) {
            try {
                admJdbcTemplate.update("""
                        INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, CREATED_BY, UPDATED_BY)
                        VALUES (?, ?, ?, ?, ?)
                        """, roleId, buttonId, allow, user, user);
            } catch (DuplicateKeyException race) {
                admJdbcTemplate.update("""
                        UPDATE adm_role_button
                           SET ALLOW_YN = ?, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                         WHERE ROLE_ID = ? AND BUTTON_ID = ?
                        """, allow, user, roleId, buttonId);
            }
        }
        return findButtonPermission(roleId, buttonId);
    }

    private void validateMenuParent(String menuId, String parentMenuId) {
        if (!CpfStrings.hasText(parentMenuId)) {
            return;
        }
        if (menuId.equals(parentMenuId.trim())) {
            throw new CpfValidationException("메뉴는 자기 자신을 상위 메뉴로 지정할 수 없습니다. menuId=" + menuId);
        }
        try {
            String current = parentMenuId.trim();
            for (int depth = 0; depth < 20 && CpfStrings.hasText(current); depth++) {
                if (menuId.equals(current)) {
                    throw new CpfValidationException("메뉴 상하위 구조에 순환 참조가 발생합니다. menuId=" + menuId);
                }
                current = admJdbcTemplate.query("""
                                SELECT PARENT_MENU_ID
                                FROM adm_menu
                                WHERE MENU_ID = ?
                                """,
                        rs -> rs.next() ? rs.getString("PARENT_MENU_ID") : null,
                        current);
            }
        } catch (DataAccessException ex) {
            throw unavailable("adm_menu.parent", ex);
        }
    }

    private AdmMenuManagement menuManagement(
            String menuId,
            String parentMenuId,
            String menuName,
            String menuPath,
            int sortOrder,
            String useYn,
            Timestamp createdAt,
            Timestamp updatedAt) {
        return new AdmMenuManagement(menuId, parentMenuId, menuName, menuPath, sortOrder,
                useYn, stringTime(createdAt), stringTime(updatedAt));
    }

    private AdmButton button(ResultSet rs) throws SQLException {
        return new AdmButton(
                rs.getString("BUTTON_ID"),
                rs.getString("MENU_ID"),
                rs.getString("ACTION_CODE"),
                rs.getString("BUTTON_NAME"),
                rs.getString("HTTP_METHOD"),
                rs.getString("API_PATTERN"),
                rs.getInt("SORT_ORDER"),
                rs.getString("USE_YN"),
                stringTime(rs.getTimestamp("CREATED_AT")),
                stringTime(rs.getTimestamp("UPDATED_AT")));
    }

    private AdmApiPermission apiPermission(
            String apiPermissionId,
            String apiGroupCode,
            String httpMethod,
            String apiPath,
            String apiName,
            String permissionCode,
            String menuId,
            String buttonId,
            String useYn,
            Timestamp createdAt,
            Timestamp updatedAt) {
        return new AdmApiPermission(apiPermissionId, apiGroupCode, httpMethod, apiPath, apiName,
                permissionCode, menuId, buttonId, useYn, stringTime(createdAt), stringTime(updatedAt));
    }

    private <T> T readFailure(String component, DataAccessException ex, T memoryFallback) {
        if (persistencePolicy.memoryEnabled()) {
            return memoryFallback;
        }
        throw unavailable(component, ex);
    }

    private CpfBusinessException unavailable(String component, DataAccessException ex) {
        return new CpfBusinessException(
                CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "ADM 권한 저장소를 사용할 수 없습니다.",
                Map.of("0", component, "1", ex.getClass().getSimpleName()));
    }

    private String requestUser(String requestUser) {
        return CpfStrings.defaultIfBlank(requestUser, "ADM");
    }

    private String ynDefaultY(String value) {
        return "N".equalsIgnoreCase(value) ? "N" : "Y";
    }

    private String yn(String value) {
        return "Y".equalsIgnoreCase(value) ? "Y" : "N";
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeMethod(String value) {
        String method = CpfStrings.defaultIfBlank(value, "GET").trim().toUpperCase(java.util.Locale.ROOT);
        return switch (method) {
            case "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "ANY" -> method;
            default -> throw new CpfValidationException("허용하지 않는 HTTP 메서드입니다. method=" + value);
        };
    }

    private String blankToNull(String value) {
        return CpfStrings.hasText(value) ? value.trim() : null;
    }

    private String stringTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }
}
