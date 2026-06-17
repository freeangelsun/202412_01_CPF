package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM 메뉴/버튼 권한 매트릭스를 조회하고 변경합니다.
 */
@Service
public class AdmPermissionService {
    private static final Logger log = LoggerFactory.getLogger(AdmPermissionService.class);

    private final JdbcTemplate admJdbcTemplate;

    public AdmPermissionService(@Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
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

    private String yn(String value) {
        return "Y".equalsIgnoreCase(value) ? "Y" : "N";
    }
}
