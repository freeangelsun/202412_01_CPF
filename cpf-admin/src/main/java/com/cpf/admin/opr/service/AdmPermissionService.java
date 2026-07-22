package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmApiPermission;
import cpf.adm.opr.dto.AdmApiPermissionSaveRequest;
import cpf.adm.opr.dto.AdmButton;
import cpf.adm.opr.dto.AdmButtonSaveRequest;
import cpf.adm.opr.dto.AdmMenuManagement;
import cpf.adm.opr.dto.AdmMenuSaveRequest;
import cpf.adm.opr.dto.AdmRoleManagement;
import cpf.adm.opr.dto.AdmRoleSaveRequest;
import cpf.adm.opr.dto.AdmStatusUpdateRequest;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfNotFoundException;
import cpf.pfw.common.exception.CpfValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * ADM 역할, 메뉴, 버튼, API 권한 매트릭스를 조회하고 변경합니다.
 */
@Service
public class AdmPermissionService extends cpf.adm.common.base.AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmPermissionService.class);

    private final JdbcTemplate admJdbcTemplate;

    public AdmPermissionService(@Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
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
            log.debug("ADM role management list skipped. reason={}", ex.getMessage());
            return List.of();
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
        } catch (DataAccessException ex) {
            throw new CpfNotFoundException("ADM 역할을 찾을 수 없습니다. roleId=" + roleId);
        }
    }

    public AdmRoleManagement createRole(AdmRoleSaveRequest request) {
        String roleId = TextUtils.requireText(request.roleId(), "roleId");
        String roleName = TextUtils.requireText(request.roleName(), "roleName");
        String roleType = TextUtils.defaultIfBlank(request.roleType(), "BUSINESS_OPERATOR");
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_role (
                    ROLE_ID, ROLE_NAME, ROLE_TYPE, DESCRIPTION, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """, roleId, roleName, roleType, request.description(), ynDefaultY(request.useYn()), user, user);
        return findRole(roleId);
    }

    public AdmRoleManagement updateRole(String roleId, AdmRoleSaveRequest request) {
        String roleName = TextUtils.requireText(request.roleName(), "roleName");
        String roleType = TextUtils.defaultIfBlank(request.roleType(), "BUSINESS_OPERATOR");
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
            log.debug("ADM menu management list skipped. reason={}", ex.getMessage());
            return List.of();
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
        } catch (DataAccessException ex) {
            throw new CpfNotFoundException("ADM 메뉴를 찾을 수 없습니다. menuId=" + menuId);
        }
    }

    public AdmMenuManagement createMenu(AdmMenuSaveRequest request) {
        String menuId = TextUtils.requireText(request.menuId(), "menuId");
        validateMenuParent(menuId, request.parentMenuId());
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_menu (
                    MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                menuId,
                blankToNull(request.parentMenuId()),
                TextUtils.requireText(request.menuName(), "menuName"),
                TextUtils.defaultIfBlank(request.menuPath(), "/adm"),
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
                TextUtils.requireText(request.menuName(), "menuName"),
                TextUtils.defaultIfBlank(request.menuPath(), "/adm"),
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
            if (TextUtils.hasText(menuId)) {
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
            log.debug("ADM button management list skipped. reason={}", ex.getMessage());
            return List.of();
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
        } catch (DataAccessException ex) {
            throw new CpfNotFoundException("ADM 버튼을 찾을 수 없습니다. buttonId=" + buttonId);
        }
    }

    public AdmButton createButton(AdmButtonSaveRequest request) {
        String buttonId = TextUtils.requireText(request.buttonId(), "buttonId");
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_button (
                    BUTTON_ID, MENU_ID, ACTION_CODE, BUTTON_NAME, HTTP_METHOD, API_PATTERN,
                    SORT_ORDER, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                buttonId,
                TextUtils.requireText(request.menuId(), "menuId"),
                TextUtils.requireText(request.actionCode(), "actionCode"),
                TextUtils.requireText(request.buttonName(), "buttonName"),
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
                TextUtils.requireText(request.menuId(), "menuId"),
                TextUtils.requireText(request.actionCode(), "actionCode"),
                TextUtils.requireText(request.buttonName(), "buttonName"),
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
            log.debug("ADM API permission list skipped. reason={}", ex.getMessage());
            return List.of();
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
        } catch (DataAccessException ex) {
            throw new CpfNotFoundException("ADM API 권한을 찾을 수 없습니다. apiPermissionId=" + apiPermissionId);
        }
    }

    public AdmApiPermission createApiPermission(AdmApiPermissionSaveRequest request) {
        String apiPermissionId = TextUtils.requireText(request.apiPermissionId(), "apiPermissionId");
        String user = requestUser(request.requestUser());
        admJdbcTemplate.update("""
                INSERT INTO adm_api_permission (
                    API_PERMISSION_ID, API_GROUP_CODE, HTTP_METHOD, API_PATH, API_NAME, PERMISSION_CODE,
                    MENU_ID, BUTTON_ID, USE_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                apiPermissionId,
                TextUtils.defaultIfBlank(request.apiGroupCode(), "ADM"),
                normalizeMethod(request.httpMethod()),
                TextUtils.requireText(request.apiPath(), "apiPath"),
                TextUtils.requireText(request.apiName(), "apiName"),
                TextUtils.defaultIfBlank(request.permissionCode(), "READ"),
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
                TextUtils.defaultIfBlank(request.apiGroupCode(), "ADM"),
                normalizeMethod(request.httpMethod()),
                TextUtils.requireText(request.apiPath(), "apiPath"),
                TextUtils.requireText(request.apiName(), "apiName"),
                TextUtils.defaultIfBlank(request.permissionCode(), "READ"),
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
            log.debug("ADM API permission matrix skipped. reason={}", ex.getMessage());
            return List.of();
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
        } catch (DataAccessException ex) {
            return Map.of();
        }
    }

    public Map<String, Object> updateRoleApiPermission(
            String roleId,
            String apiPermissionId,
            String allowYn,
            String requestUser) {
        String user = requestUser(requestUser);
        admJdbcTemplate.update("""
                INSERT INTO adm_role_api_permission (
                    ROLE_ID, API_PERMISSION_ID, ALLOW_YN, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    ALLOW_YN = VALUES(ALLOW_YN),
                    UPDATED_BY = VALUES(UPDATED_BY),
                    UPDATED_AT = CURRENT_TIMESTAMP
                """, roleId, apiPermissionId, yn(allowYn), user, user);
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
            log.debug("ADM menu permission matrix skipped. reason={}", ex.getMessage());
            return List.of();
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
            log.debug("ADM button permission matrix skipped. reason={}", ex.getMessage());
            return List.of();
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
        } catch (DataAccessException ex) {
            return Map.of();
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
        } catch (DataAccessException ex) {
            return Map.of();
        }
    }

    public Map<String, Object> updateMenuPermission(
            String roleId,
            String menuId,
            String readYn,
            String writeYn,
            String deleteYn,
            String requestUser) {
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        admJdbcTemplate.update("""
                INSERT INTO adm_role_menu (ROLE_ID, MENU_ID, READ_YN, WRITE_YN, DELETE_YN, CREATED_BY, UPDATED_BY)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    READ_YN = VALUES(READ_YN),
                    WRITE_YN = VALUES(WRITE_YN),
                    DELETE_YN = VALUES(DELETE_YN),
                    UPDATED_BY = VALUES(UPDATED_BY),
                    UPDATED_AT = CURRENT_TIMESTAMP
                """, roleId, menuId, yn(readYn), yn(writeYn), yn(deleteYn), user, user);
        return findMenuPermission(roleId, menuId);
    }

    public Map<String, Object> updateButtonPermission(
            String roleId,
            String buttonId,
            String allowYn,
            String requestUser) {
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        admJdbcTemplate.update("""
                INSERT INTO adm_role_button (ROLE_ID, BUTTON_ID, ALLOW_YN, CREATED_BY, UPDATED_BY)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    ALLOW_YN = VALUES(ALLOW_YN),
                    UPDATED_BY = VALUES(UPDATED_BY),
                    UPDATED_AT = CURRENT_TIMESTAMP
                """, roleId, buttonId, yn(allowYn), user, user);
        return findButtonPermission(roleId, buttonId);
    }

    private void validateMenuParent(String menuId, String parentMenuId) {
        if (!TextUtils.hasText(parentMenuId)) {
            return;
        }
        if (menuId.equals(parentMenuId.trim())) {
            throw new CpfValidationException("메뉴는 자기 자신을 상위 메뉴로 지정할 수 없습니다. menuId=" + menuId);
        }
        try {
            String current = parentMenuId.trim();
            for (int depth = 0; depth < 20 && TextUtils.hasText(current); depth++) {
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
            throw new CpfValidationException("상위 메뉴를 찾을 수 없습니다. parentMenuId=" + parentMenuId);
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

    private String requestUser(String requestUser) {
        return TextUtils.defaultIfBlank(requestUser, "ADM");
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
        String method = TextUtils.defaultIfBlank(value, "GET").trim().toUpperCase();
        return switch (method) {
            case "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS", "ANY" -> method;
            default -> throw new CpfValidationException("허용하지 않는 HTTP 메서드입니다. method=" + value);
        };
    }

    private String blankToNull(String value) {
        return TextUtils.hasText(value) ? value.trim() : null;
    }

    private String stringTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }
}
