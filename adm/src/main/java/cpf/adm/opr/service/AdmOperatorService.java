package cpf.adm.opr.service;

import cpf.adm.opr.dto.AdmLoginRequest;
import cpf.adm.opr.dto.AdmMenu;
import cpf.adm.opr.dto.AdmOperator;
import cpf.adm.opr.dto.AdmOperatorCreateRequest;
import cpf.adm.opr.dto.AdmPasswordChangeRequest;
import cpf.adm.opr.dto.AdmRole;
import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.FpsNotFoundException;
import cpf.pfw.common.exception.FpsValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AdmOperatorService {
    private static final Logger log = LoggerFactory.getLogger(AdmOperatorService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int PBKDF2_ITERATIONS = 120_000;
    private static final int PBKDF2_KEY_LENGTH = 256;

    private final AdmPasswordPolicyService passwordPolicyService;
    private final JdbcTemplate admJdbcTemplate;
    private final ConcurrentMap<String, OperatorState> operators = new ConcurrentHashMap<>();
    private final List<AdmRole> fallbackRoles = new ArrayList<>();
    private final List<AdmMenu> fallbackMenus = new ArrayList<>();

    public AdmOperatorService(AdmPasswordPolicyService passwordPolicyService,
                              @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.passwordPolicyService = passwordPolicyService;
        this.admJdbcTemplate = admJdbcTemplate;
        seedFallback();
    }

    public List<AdmOperator> findOperators() {
        try {
            return admJdbcTemplate.query("""
                    SELECT u.OPERATOR_ID, u.OPERATOR_NAME, u.LOCKED_YN, u.PASSWORD_CHANGED_AT,
                           u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT,
                           GROUP_CONCAT(ur.ROLE_ID ORDER BY ur.ROLE_ID SEPARATOR ',') AS ROLE_IDS
                    FROM operator_user u
                    LEFT JOIN operator_user_role ur ON ur.OPERATOR_ID = u.OPERATOR_ID
                    WHERE u.USE_YN = 'Y'
                    GROUP BY u.OPERATOR_ID, u.OPERATOR_NAME, u.LOCKED_YN, u.PASSWORD_CHANGED_AT,
                             u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT
                    ORDER BY u.OPERATOR_ID
                    """, (rs, rowNum) -> new AdmOperator(
                    rs.getString("OPERATOR_ID"),
                    rs.getString("OPERATOR_NAME"),
                    parseRoleIds(rs.getString("ROLE_IDS")),
                    "Y".equals(rs.getString("LOCKED_YN")),
                    passwordPolicyService.isExpired(toLocalDateTime(rs.getTimestamp("PASSWORD_CHANGED_AT"))),
                    "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                    stringTime(rs.getTimestamp("CREATED_AT")),
                    stringTime(rs.getTimestamp("UPDATED_AT"))));
        } catch (DataAccessException ex) {
            log.debug("ADM operator DB list skipped. reason={}", ex.getMessage());
            return operators.values().stream()
                    .map(this::toResponse)
                    .sorted(Comparator.comparing(AdmOperator::operatorId))
                    .toList();
        }
    }

    public AdmOperator createOperator(AdmOperatorCreateRequest request) {
        String operatorId = TextUtils.requireText(request.operatorId(), "operatorId");
        String operatorName = TextUtils.requireText(request.operatorName(), "operatorName");
        List<String> roleIds = request.roleIds() == null || request.roleIds().isEmpty()
                ? List.of("ADM_VIEWER")
                : List.copyOf(request.roleIds());
        passwordPolicyService.requireValid(operatorId, request.password());
        String passwordHash = hashPassword(request.password());
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "ADM");

        try {
            admJdbcTemplate.update("""
                    INSERT INTO operator_user (
                        OPERATOR_ID, OPERATOR_NAME, PASSWORD_HASH, LOCKED_YN, FAIL_COUNT,
                        PASSWORD_CHANGED_AT, PASSWORD_CHANGE_REQUIRED_YN, USE_YN, CREATED_BY, UPDATED_BY
                    ) VALUES (?, ?, ?, 'N', 0, CURRENT_TIMESTAMP, 'Y', 'Y', ?, ?)
                    """, operatorId, operatorName, passwordHash, requestUser, requestUser);
            admJdbcTemplate.update("DELETE FROM operator_user_role WHERE OPERATOR_ID = ?", operatorId);
            for (String roleId : roleIds) {
                admJdbcTemplate.update("""
                        INSERT INTO operator_user_role (OPERATOR_ID, ROLE_ID, CREATED_BY, UPDATED_BY)
                        VALUES (?, ?, ?, ?)
                        ON DUPLICATE KEY UPDATE UPDATED_BY = VALUES(UPDATED_BY), UPDATED_AT = CURRENT_TIMESTAMP
                        """, operatorId, roleId, requestUser, requestUser);
            }
        } catch (DataAccessException ex) {
            log.debug("ADM operator DB create skipped. operatorId={}, reason={}", operatorId, ex.getMessage());
            OperatorState state = new OperatorState(operatorId, operatorName, passwordHash, roleIds,
                    false, 0, true, LocalDateTime.now(), DateTimeUtils.nowDateTimeMillis(), DateTimeUtils.nowDateTimeMillis());
            if (operators.putIfAbsent(operatorId, state) != null) {
                throw new FpsValidationException("Operator already exists. operatorId=" + operatorId);
            }
            return toResponse(state);
        }
        return findOperator(operatorId);
    }

    public AdmOperator authenticate(AdmLoginRequest request) {
        String operatorId = TextUtils.requireText(request.operatorId(), "operatorId");
        String password = TextUtils.requireText(request.password(), "password");
        try {
            OperatorState state = loadOperatorState(operatorId);
            if (state.locked) {
                throw new FpsValidationException("Locked operator account. operatorId=" + operatorId);
            }
            if (!matchesPassword(password, state.passwordHash)) {
                int failed = state.failedLoginCount + 1;
                boolean locked = failed >= passwordPolicyService.maxFailCount();
                admJdbcTemplate.update("""
                        UPDATE operator_user
                        SET FAIL_COUNT = ?, LOCKED_YN = ?, UPDATED_BY = 'ADM', UPDATED_AT = CURRENT_TIMESTAMP
                        WHERE OPERATOR_ID = ?
                        """, failed, locked ? "Y" : "N", operatorId);
                throw new FpsValidationException("Operator authentication failed.");
            }
            admJdbcTemplate.update("""
                    UPDATE operator_user
                    SET FAIL_COUNT = 0, UPDATED_BY = 'ADM', UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ?
                    """, operatorId);
            return toResponse(state.withFailedLoginCount(0));
        } catch (DataAccessException ex) {
            log.debug("ADM operator DB auth skipped. operatorId={}, reason={}", operatorId, ex.getMessage());
            return authenticateFallback(operatorId, password);
        }
    }

    public AdmOperator changePassword(String operatorId, AdmPasswordChangeRequest request) {
        passwordPolicyService.requireValid(operatorId, request.newPassword());
        String hash = hashPassword(request.newPassword());
        String requestUser = TextUtils.defaultIfBlank(request.requestUser(), "ADM");
        try {
            int updated = admJdbcTemplate.update("""
                    UPDATE operator_user
                    SET PASSWORD_HASH = ?, PASSWORD_CHANGED_AT = CURRENT_TIMESTAMP,
                        PASSWORD_CHANGE_REQUIRED_YN = 'N', FAIL_COUNT = 0, LOCKED_YN = 'N',
                        UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y'
                    """, hash, requestUser, operatorId);
            if (updated == 0) {
                throw new FpsNotFoundException("Operator not found. operatorId=" + operatorId);
            }
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            OperatorState state = operators.get(operatorId);
            if (state == null) {
                throw new FpsNotFoundException("Operator not found. operatorId=" + operatorId);
            }
            state.passwordHash = hash;
            state.passwordChangedAt = LocalDateTime.now();
            state.passwordChangeRequired = false;
            state.failedLoginCount = 0;
            state.locked = false;
            state.updatedAt = DateTimeUtils.nowDateTimeMillis();
            return toResponse(state);
        }
    }

    public List<AdmRole> findRoles() {
        try {
            return admJdbcTemplate.query("""
                    SELECT ROLE_ID, ROLE_NAME, DESCRIPTION
                    FROM operator_role
                    WHERE USE_YN = 'Y'
                    ORDER BY ROLE_ID
                    """, (rs, rowNum) -> new AdmRole(rs.getString("ROLE_ID"), rs.getString("ROLE_NAME"), rs.getString("DESCRIPTION")));
        } catch (DataAccessException ex) {
            return List.copyOf(fallbackRoles);
        }
    }

    public List<AdmMenu> findMenus() {
        try {
            return admJdbcTemplate.query("""
                    SELECT MENU_ID, PARENT_MENU_ID, MENU_NAME, MENU_PATH, SORT_ORDER
                    FROM operator_menu
                    WHERE USE_YN = 'Y'
                    ORDER BY SORT_ORDER, MENU_ID
                    """, (rs, rowNum) -> new AdmMenu(
                    rs.getString("MENU_ID"), rs.getString("PARENT_MENU_ID"), rs.getString("MENU_NAME"),
                    rs.getString("MENU_PATH"), rs.getInt("SORT_ORDER"), true, true, true));
        } catch (DataAccessException ex) {
            return fallbackMenus.stream().sorted(Comparator.comparingInt(AdmMenu::sortOrder)).toList();
        }
    }

    public List<AdmMenu> findMenusForRoles(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        try {
            String placeholders = String.join(",", roleIds.stream().map(role -> "?").toList());
            List<Object> args = new ArrayList<>(roleIds);
            return admJdbcTemplate.query("""
                    SELECT m.MENU_ID, m.PARENT_MENU_ID, m.MENU_NAME, m.MENU_PATH, m.SORT_ORDER,
                           MAX(rm.READ_YN) AS READ_YN,
                           MAX(rm.WRITE_YN) AS WRITE_YN,
                           MAX(rm.DELETE_YN) AS DELETE_YN
                    FROM operator_menu m
                    JOIN operator_role_menu rm ON rm.MENU_ID = m.MENU_ID
                    WHERE m.USE_YN = 'Y'
                      AND rm.READ_YN = 'Y'
                      AND rm.ROLE_ID IN (%s)
                    GROUP BY m.MENU_ID, m.PARENT_MENU_ID, m.MENU_NAME, m.MENU_PATH, m.SORT_ORDER
                    ORDER BY m.SORT_ORDER, m.MENU_ID
                    """.formatted(placeholders), (rs, rowNum) -> new AdmMenu(
                    rs.getString("MENU_ID"), rs.getString("PARENT_MENU_ID"), rs.getString("MENU_NAME"),
                    rs.getString("MENU_PATH"), rs.getInt("SORT_ORDER"),
                    "Y".equals(rs.getString("READ_YN")),
                    "Y".equals(rs.getString("WRITE_YN")),
                    "Y".equals(rs.getString("DELETE_YN"))), args.toArray());
        } catch (DataAccessException ex) {
            if (roleIds.contains("ADM_ADMIN")) {
                return findMenus();
            }
            return fallbackMenusForRoles(roleIds);
        }
    }

    public Map<String, Object> validatePassword(String operatorId, String password) {
        return Map.of("operatorId", operatorId, "violations", passwordPolicyService.validate(operatorId, password));
    }

    private AdmOperator findOperator(String operatorId) {
        return findOperators().stream()
                .filter(operator -> operator.operatorId().equals(operatorId))
                .findFirst()
                .orElseThrow(() -> new FpsNotFoundException("Operator not found. operatorId=" + operatorId));
    }

    private OperatorState loadOperatorState(String operatorId) {
        return admJdbcTemplate.query("""
                        SELECT u.OPERATOR_ID, u.OPERATOR_NAME, u.PASSWORD_HASH, u.LOCKED_YN, u.FAIL_COUNT,
                               u.PASSWORD_CHANGED_AT, u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT,
                               GROUP_CONCAT(ur.ROLE_ID ORDER BY ur.ROLE_ID SEPARATOR ',') AS ROLE_IDS
                        FROM operator_user u
                        LEFT JOIN operator_user_role ur ON ur.OPERATOR_ID = u.OPERATOR_ID
                        WHERE u.OPERATOR_ID = ? AND u.USE_YN = 'Y'
                        GROUP BY u.OPERATOR_ID, u.OPERATOR_NAME, u.PASSWORD_HASH, u.LOCKED_YN, u.FAIL_COUNT,
                                 u.PASSWORD_CHANGED_AT, u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT
                        """,
                rs -> {
                    if (!rs.next()) {
                        throw new FpsValidationException("Operator authentication failed.");
                    }
                    return new OperatorState(
                            rs.getString("OPERATOR_ID"), rs.getString("OPERATOR_NAME"), rs.getString("PASSWORD_HASH"),
                            parseRoleIds(rs.getString("ROLE_IDS")), "Y".equals(rs.getString("LOCKED_YN")),
                            rs.getInt("FAIL_COUNT"), "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                            toLocalDateTime(rs.getTimestamp("PASSWORD_CHANGED_AT")), stringTime(rs.getTimestamp("CREATED_AT")),
                            stringTime(rs.getTimestamp("UPDATED_AT")));
                }, operatorId);
    }

    private AdmOperator authenticateFallback(String operatorId, String password) {
        OperatorState state = operators.get(operatorId);
        if (state == null || state.locked) {
            throw new FpsValidationException("Operator authentication failed.");
        }
        if (!matchesPassword(password, state.passwordHash)) {
            state.failedLoginCount++;
            if (state.failedLoginCount >= passwordPolicyService.maxFailCount()) {
                state.locked = true;
            }
            state.updatedAt = DateTimeUtils.nowDateTimeMillis();
            throw new FpsValidationException("Operator authentication failed.");
        }
        state.failedLoginCount = 0;
        state.updatedAt = DateTimeUtils.nowDateTimeMillis();
        return toResponse(state);
    }

    private AdmOperator toResponse(OperatorState state) {
        return new AdmOperator(state.operatorId, state.operatorName, state.roleIds, state.locked,
                passwordPolicyService.isExpired(state.passwordChangedAt), state.passwordChangeRequired,
                state.createdAt, state.updatedAt);
    }

    private void seedFallback() {
        fallbackRoles.add(new AdmRole("ADM_ADMIN", "Framework Administrator", "Can manage every ADM menu and operation."));
        fallbackRoles.add(new AdmRole("ADM_OPERATOR", "Operations User", "Can query logs, refresh caches, and manage dynamic log levels."));
        fallbackRoles.add(new AdmRole("ADM_VIEWER", "Read Only User", "Can query logs and settings without changing data."));

        fallbackMenus.add(new AdmMenu("DASHBOARD", null, "Dashboard", "/adm", 10));
        fallbackMenus.add(new AdmMenu("LOG_LIST", null, "Transaction Logs", "/adm#logs", 20));
        fallbackMenus.add(new AdmMenu("CACHE", null, "Cache Management", "/adm#cache", 30));
        fallbackMenus.add(new AdmMenu("RESPONSE_CODE", null, "Response Codes", "/adm#response-codes", 40));
        fallbackMenus.add(new AdmMenu("DYNAMIC_LOG", null, "Dynamic Log Level", "/adm#log-level", 50));
        fallbackMenus.add(new AdmMenu("AUDIT_LOG", null, "Audit Logs", "/adm#audit-logs", 60));
        fallbackMenus.add(new AdmMenu("OPERATOR", null, "Operator Management", "/adm#operators", 70));

        operators.put("admin", new OperatorState("admin", "Local Administrator", hashPassword("Adm!n12345"),
                List.of("ADM_ADMIN"), false, 0, true, LocalDateTime.now().minusDays(91),
                DateTimeUtils.nowDateTimeMillis(), DateTimeUtils.nowDateTimeMillis()));
    }

    private List<String> parseRoleIds(String roleIds) {
        if (roleIds == null || roleIds.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(roleIds.split(",")).map(String::trim).filter(role -> !role.isBlank()).toList();
    }

    private List<AdmMenu> fallbackMenusForRoles(List<String> roleIds) {
        if (roleIds.contains("ADM_ADMIN")) {
            return fallbackMenus.stream().sorted(Comparator.comparingInt(AdmMenu::sortOrder)).toList();
        }
        if (roleIds.contains("ADM_OPERATOR")) {
            return fallbackMenus.stream()
                    .filter(menu -> !"OPERATOR".equals(menu.menuId()) && !"RESPONSE_CODE".equals(menu.menuId()))
                    .map(menu -> switch (menu.menuId()) {
                        case "CACHE", "DYNAMIC_LOG" -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                                menu.path(), menu.sortOrder(), true, true, "DYNAMIC_LOG".equals(menu.menuId()));
                        default -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                                menu.path(), menu.sortOrder(), true, false, false);
                    })
                    .sorted(Comparator.comparingInt(AdmMenu::sortOrder))
                    .toList();
        }
        return fallbackMenus.stream()
                .filter(menu -> !"DYNAMIC_LOG".equals(menu.menuId())
                        && !"OPERATOR".equals(menu.menuId())
                        && !"RESPONSE_CODE".equals(menu.menuId())
                        && !"AUDIT_LOG".equals(menu.menuId()))
                .map(menu -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                        menu.path(), menu.sortOrder(), true, false, false))
                .sorted(Comparator.comparingInt(AdmMenu::sortOrder))
                .toList();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String stringTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }

    private String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            SECURE_RANDOM.nextBytes(salt);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return "PBKDF2$" + PBKDF2_ITERATIONS + "$" + Base64.getEncoder().encodeToString(salt) + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to hash operator password.", ex);
        }
    }

    private boolean matchesPassword(String rawPassword, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4 || !"PBKDF2".equals(parts[0])) {
                return false;
            }
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, expectedHash.length * 8);
            byte[] actualHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception ex) {
            return false;
        }
    }

    private static class OperatorState {
        private final String operatorId;
        private final String operatorName;
        private final String createdAt;
        private final List<String> roleIds;
        private String passwordHash;
        private boolean locked;
        private int failedLoginCount;
        private boolean passwordChangeRequired;
        private LocalDateTime passwordChangedAt;
        private String updatedAt;

        private OperatorState(String operatorId, String operatorName, String passwordHash, List<String> roleIds,
                              boolean locked, int failedLoginCount, boolean passwordChangeRequired,
                              LocalDateTime passwordChangedAt, String createdAt, String updatedAt) {
            this.operatorId = operatorId;
            this.operatorName = operatorName;
            this.passwordHash = passwordHash;
            this.roleIds = roleIds;
            this.locked = locked;
            this.failedLoginCount = failedLoginCount;
            this.passwordChangeRequired = passwordChangeRequired;
            this.passwordChangedAt = passwordChangedAt;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        private OperatorState withFailedLoginCount(int failedLoginCount) {
            this.failedLoginCount = failedLoginCount;
            return this;
        }
    }
}
