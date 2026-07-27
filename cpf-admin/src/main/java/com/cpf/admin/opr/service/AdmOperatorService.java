package com.cpf.admin.opr.service;

import com.cpf.admin.opr.dto.AdmLoginRequest;
import com.cpf.admin.opr.dto.AdmMenu;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.admin.opr.dto.AdmOperatorCreateRequest;
import com.cpf.admin.opr.dto.AdmOperatorPasswordResetRequest;
import com.cpf.admin.opr.dto.AdmOperatorRoleUpdateRequest;
import com.cpf.admin.opr.dto.AdmPasswordChangeRequest;
import com.cpf.admin.opr.dto.AdmRole;
import com.cpf.core.api.util.CpfTimes;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.security.password.CpfPasswordService;
import com.cpf.core.api.security.password.CpfPasswordVerification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ADM 운영자, 역할, 메뉴 권한을 관리합니다.
 *
 * <p>DB가 준비되지 않은 로컬 초반에도 ADM UI를 확인할 수 있도록 메모리 fallback을 유지합니다.
 * DB가 정상 연결되면 DB 기준 운영자와 권한을 우선 사용합니다.</p>
 */
@Service
public class AdmOperatorService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmOperatorService.class);
    private final AdmPasswordPolicyService passwordPolicyService;
    private final CpfPasswordService passwordHashingPort;
    private final JdbcTemplate admJdbcTemplate;
    private final ConcurrentMap<String, OperatorState> operators = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, OperatorContactProfile> operatorContactProfiles = new ConcurrentHashMap<>();
    private final List<AdmRole> fallbackRoles = new ArrayList<>();
    private final List<AdmMenu> fallbackMenus = new ArrayList<>();

    public AdmOperatorService(AdmPasswordPolicyService passwordPolicyService,
                              CpfPasswordService passwordHashingPort,
                              @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate) {
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHashingPort = passwordHashingPort;
        this.admJdbcTemplate = admJdbcTemplate;
        seedFallback();
    }

    public List<AdmOperator> findOperators() {
        try {
            return admJdbcTemplate.query("""
                    SELECT u.OPERATOR_ID, u.OPERATOR_NAME, p.MOBILE_NO, p.OFFICE_PHONE_NO, u.LOCKED_YN, u.PASSWORD_CHANGED_AT,
                           u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT,
                           GROUP_CONCAT(ur.ROLE_ID ORDER BY ur.ROLE_ID SEPARATOR ',') AS ROLE_IDS
                    FROM adm_operator u
                    LEFT JOIN adm_operator_profile p ON p.OPERATOR_ID = u.OPERATOR_ID
                    LEFT JOIN adm_operator_role ur ON ur.OPERATOR_ID = u.OPERATOR_ID
                    WHERE u.USE_YN = 'Y'
                    GROUP BY u.OPERATOR_ID, u.OPERATOR_NAME, p.MOBILE_NO, p.OFFICE_PHONE_NO, u.LOCKED_YN, u.PASSWORD_CHANGED_AT,
                             u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT
                    ORDER BY u.OPERATOR_ID
                    """, (rs, rowNum) -> new AdmOperator(
                    rs.getString("OPERATOR_ID"),
                    rs.getString("OPERATOR_NAME"),
                    rs.getString("MOBILE_NO"),
                    rs.getString("OFFICE_PHONE_NO"),
                    parseRoleIds(rs.getString("ROLE_IDS")),
                    "Y".equals(rs.getString("LOCKED_YN")),
                    passwordPolicyService.isExpired(toLocalDateTime(rs.getTimestamp("PASSWORD_CHANGED_AT"))),
                    "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                    stringTime(rs.getTimestamp("CREATED_AT")),
                    stringTime(rs.getTimestamp("UPDATED_AT"))));
        } catch (DataAccessException ex) {
            log.debug("ADM 운영자 DB 조회를 건너뜁니다. reason={}", ex.getMessage());
            return operators.values().stream()
                    .map(this::toResponse)
                    .sorted(Comparator.comparing(AdmOperator::operatorId))
                    .toList();
        }
    }

    public AdmOperator createOperator(AdmOperatorCreateRequest request) {
        String operatorId = CpfStrings.requireText(request.operatorId(), "operatorId");
        String operatorName = CpfStrings.requireText(request.operatorName(), "operatorName");
        List<String> roleIds = request.roleIds() == null || request.roleIds().isEmpty()
                ? List.of("ADM_VIEWER")
                : List.copyOf(request.roleIds());
        passwordPolicyService.requireValid(operatorId, request.password());
        String passwordHash = hashPassword(request.password());
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");

        try {
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator (
                        OPERATOR_ID, OPERATOR_NAME, PASSWORD_HASH, LOCKED_YN, FAIL_COUNT,
                        PASSWORD_CHANGED_AT, PASSWORD_CHANGE_REQUIRED_YN, USE_YN, CREATED_BY, UPDATED_BY
                    ) VALUES (?, ?, ?, 'N', 0, CURRENT_TIMESTAMP, 'Y', 'Y', ?, ?)
                    """, operatorId, operatorName, passwordHash, requestUser, requestUser);
            upsertOperatorContactProfile(operatorId, request.mobileNo(), request.officePhoneNo(), requestUser);
            replaceRoles(operatorId, roleIds, requestUser);
        } catch (DataAccessException ex) {
            log.debug("ADM 운영자 DB 생성을 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
            OperatorState state = new OperatorState(operatorId, operatorName, passwordHash, roleIds, false, 0, true,
                    LocalDateTime.now(), CpfTimes.nowDateTimeMillis(), CpfTimes.nowDateTimeMillis());
            if (operators.putIfAbsent(operatorId, state) != null) {
                throw new CpfValidationException("이미 존재하는 운영자입니다. operatorId=" + operatorId);
            }
            operatorContactProfiles.put(operatorId, new OperatorContactProfile(
                    blankToNull(request.mobileNo()), blankToNull(request.officePhoneNo())));
            return toResponse(state);
        }
        return findOperator(operatorId);
    }

    /**
     * 환경변수로 승인된 최초 운영자 계정을 한 번만 생성합니다.
     *
     * <p>이미 같은 운영자가 있으면 비밀번호와 역할을 변경하지 않습니다. DB가 없는 로컬 fallback도
     * 같은 idempotency 규칙을 적용합니다.</p>
     *
     * @return 새 계정을 생성했으면 {@code true}, 이미 존재하면 {@code false}
     */
    public boolean bootstrapOperator(String operatorIdValue, String operatorNameValue, String password) {
        String operatorId = CpfStrings.requireText(operatorIdValue, "operatorId");
        String operatorName = CpfStrings.requireText(operatorNameValue, "operatorName");
        passwordPolicyService.requireValid(operatorId, password);
        String passwordHash = hashPassword(password);
        try {
            int inserted = admJdbcTemplate.update("""
                    INSERT INTO adm_operator (
                        OPERATOR_ID, OPERATOR_NAME, PASSWORD_HASH, LOCKED_YN, FAIL_COUNT,
                        PASSWORD_CHANGED_AT, PASSWORD_CHANGE_REQUIRED_YN, USE_YN, CREATED_BY, UPDATED_BY
                    )
                    SELECT ?, ?, ?, 'N', 0, CURRENT_TIMESTAMP, 'Y', 'Y', 'BOOTSTRAP', 'BOOTSTRAP'
                    WHERE NOT EXISTS (
                        SELECT 1 FROM adm_operator WHERE OPERATOR_ID = ?
                    )
                    """, operatorId, operatorName, passwordHash, operatorId);
            if (inserted > 0) {
                replaceRoles(operatorId, List.of("ADM_ADMIN"), "BOOTSTRAP");
            }
            return inserted > 0;
        } catch (DataAccessException ex) {
            log.debug("ADM bootstrap DB 처리를 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
            OperatorState state = new OperatorState(
                    operatorId,
                    operatorName,
                    passwordHash,
                    List.of("ADM_ADMIN"),
                    false,
                    0,
                    true,
                    LocalDateTime.now(),
                    CpfTimes.nowDateTimeMillis(),
                    CpfTimes.nowDateTimeMillis());
            return operators.putIfAbsent(operatorId, state) == null;
        }
    }

    public AdmOperator authenticate(AdmLoginRequest request) {
        String operatorId = CpfStrings.requireText(request.operatorId(), "operatorId");
        String password = CpfStrings.requireText(request.password(), "password");
        try {
            OperatorState state = loadOperatorState(operatorId);
            if (state.locked) {
                throw new CpfValidationException("잠긴 운영자 계정입니다. operatorId=" + operatorId);
            }
            CpfPasswordVerification verification = verifyPassword(password, state.passwordHash);
            if (!verification.matched()) {
                int failed = state.failedLoginCount + 1;
                boolean locked = failed >= passwordPolicyService.maxFailCount();
                admJdbcTemplate.update("""
                        UPDATE adm_operator
                        SET FAIL_COUNT = ?, LOCKED_YN = ?, UPDATED_BY = 'ADM', UPDATED_AT = CURRENT_TIMESTAMP
                        WHERE OPERATOR_ID = ?
                        """, failed, locked ? "Y" : "N", operatorId);
                throw new CpfValidationException("운영자 인증에 실패했습니다.");
            }
            admJdbcTemplate.update("""
                    UPDATE adm_operator
                    SET FAIL_COUNT = 0,
                        LAST_LOGIN_AT = CURRENT_TIMESTAMP,
                        UPDATED_BY = 'ADM',
                        UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ?
                    """, operatorId);
            if (verification.rehashRequired()) {
                admJdbcTemplate.update("""
                        UPDATE adm_operator
                        SET PASSWORD_HASH = ?, UPDATED_BY = 'CPF_PASSWORD_UPGRADE', UPDATED_AT = CURRENT_TIMESTAMP
                        WHERE OPERATOR_ID = ? AND PASSWORD_HASH = ?
                        """, hashPassword(password), operatorId, state.passwordHash);
            }
            return toResponse(state.withFailedLoginCount(0));
        } catch (DataAccessException ex) {
            log.debug("ADM 운영자 DB 인증을 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
            return authenticateFallback(operatorId, password);
        }
    }

    public AdmOperator changePassword(String operatorId, AdmPasswordChangeRequest request) {
        String newPassword = CpfStrings.requireText(request.newPassword(), "newPassword");
        String currentPassword = CpfStrings.requireText(request.currentPassword(), "currentPassword");
        String newPasswordConfirm = CpfStrings.requireText(request.newPasswordConfirm(), "newPasswordConfirm");
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new CpfValidationException("새 비밀번호와 확인값이 일치하지 않습니다.");
        }
        passwordPolicyService.requireValid(operatorId, newPassword);
        String hash = hashPassword(newPassword);
        String reason = CpfStrings.requireText(request.reason(), "reason");
        try {
            OperatorState before = loadOperatorState(operatorId);
            if (!matchesPassword(currentPassword, before.passwordHash)) {
                throw new CpfValidationException("현재 비밀번호가 일치하지 않습니다.");
            }
            requirePasswordNotReused(operatorId, newPassword, before);
            admJdbcTemplate.update("""
                    INSERT INTO adm_password_history (OPERATOR_ID, PASSWORD_HASH, CHANGED_REASON, CREATED_BY, UPDATED_BY)
                    VALUES (?, ?, ?, ?, ?)
                    """, operatorId, before.passwordHash, reason, operatorId, operatorId);
            int updated = admJdbcTemplate.update("""
                    UPDATE adm_operator
                    SET PASSWORD_HASH = ?, PASSWORD_CHANGED_AT = CURRENT_TIMESTAMP,
                        PASSWORD_CHANGE_REQUIRED_YN = 'N', FAIL_COUNT = 0, LOCKED_YN = 'N',
                        UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND PASSWORD_HASH = ? AND USE_YN = 'Y'
                    """, hash, operatorId, operatorId, before.passwordHash);
            if (updated == 0) {
                throw new CpfValidationException("비밀번호가 동시에 변경되었습니다. 다시 로그인한 뒤 재시도하세요.");
            }
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            OperatorState state = operators.get(operatorId);
            if (state == null) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            synchronized (state) {
                if (!matchesPassword(currentPassword, state.passwordHash)) {
                    throw new CpfValidationException("현재 비밀번호가 일치하지 않습니다.");
                }
                requirePasswordNotReused(newPassword, state);
                rememberPassword(state, state.passwordHash);
                state.passwordHash = hash;
                state.passwordChangedAt = LocalDateTime.now();
                state.passwordChangeRequired = false;
                state.failedLoginCount = 0;
                state.locked = false;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                return toResponse(state);
            }
        }
    }

    public AdmOperator resetPassword(String operatorId, AdmOperatorPasswordResetRequest request) {
        passwordPolicyService.requireValid(operatorId, request.newPassword());
        String hash = hashPassword(request.newPassword());
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");
        try {
            OperatorState before = loadOperatorState(operatorId);
            requirePasswordNotReused(operatorId, request.newPassword(), before);
            admJdbcTemplate.update("""
                    INSERT INTO adm_password_history (OPERATOR_ID, PASSWORD_HASH, CHANGED_REASON, CREATED_BY, UPDATED_BY)
                    VALUES (?, ?, ?, ?, ?)
                    """, operatorId, before.passwordHash, CpfStrings.defaultIfBlank(request.reason(), "비밀번호 초기화"), requestUser, requestUser);
            int updated = admJdbcTemplate.update("""
                    UPDATE adm_operator
                    SET PASSWORD_HASH = ?,
                        PASSWORD_CHANGED_AT = CURRENT_TIMESTAMP,
                        PASSWORD_CHANGE_REQUIRED_YN = ?,
                        FAIL_COUNT = 0,
                        LOCKED_YN = 'N',
                        UPDATED_BY = ?,
                        UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y'
                    """, hash, request.forceChange() ? "Y" : "N", requestUser, operatorId);
            if (updated == 0) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            OperatorState state = operators.get(operatorId);
            if (state == null) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            synchronized (state) {
                requirePasswordNotReused(request.newPassword(), state);
                rememberPassword(state, state.passwordHash);
                state.passwordHash = hash;
                state.passwordChangedAt = LocalDateTime.now();
                state.passwordChangeRequired = request.forceChange();
                state.failedLoginCount = 0;
                state.locked = false;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                return toResponse(state);
            }
        }
    }

    public AdmOperator unlockOperator(String operatorId, String requestUser) {
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        try {
            int updated = admJdbcTemplate.update("""
                    UPDATE adm_operator
                    SET LOCKED_YN = 'N',
                        FAIL_COUNT = 0,
                        UPDATED_BY = ?,
                        UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y'
                    """, user, operatorId);
            if (updated == 0) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            OperatorState state = operators.get(operatorId);
            if (state == null) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            state.failedLoginCount = 0;
            state.locked = false;
            state.updatedAt = CpfTimes.nowDateTimeMillis();
            return toResponse(state);
        }
    }

    public AdmOperator updateRoles(String operatorId, AdmOperatorRoleUpdateRequest request) {
        List<String> roleIds = request.roleIds() == null || request.roleIds().isEmpty()
                ? List.of("ADM_VIEWER")
                : List.copyOf(request.roleIds());
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");
        try {
            Integer operatorCount = admJdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM adm_operator
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y'
                    """, Integer.class, operatorId);
            if (operatorCount == null || operatorCount == 0) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            replaceRoles(operatorId, roleIds, requestUser);
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            OperatorState state = operators.get(operatorId);
            if (state == null) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            state.roleIds = roleIds;
            state.updatedAt = CpfTimes.nowDateTimeMillis();
            return toResponse(state);
        }
    }

    public List<AdmRole> findRoles() {
        try {
            return admJdbcTemplate.query("""
                    SELECT ROLE_ID, ROLE_NAME, DESCRIPTION
                    FROM adm_role
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
                    FROM adm_menu
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
                    FROM adm_menu m
                    JOIN adm_role_menu rm ON rm.MENU_ID = m.MENU_ID
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

    public Map<String, Object> passwordPolicy() {
        return passwordPolicyService.currentPolicy();
    }

    private void replaceRoles(String operatorId, List<String> roleIds, String requestUser) {
        admJdbcTemplate.update("DELETE FROM adm_operator_role WHERE OPERATOR_ID = ?", operatorId);
        for (String roleId : roleIds) {
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator_role (OPERATOR_ID, ROLE_ID, CREATED_BY, UPDATED_BY)
                    VALUES (?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE UPDATED_BY = VALUES(UPDATED_BY), UPDATED_AT = CURRENT_TIMESTAMP
                    """, operatorId, roleId, requestUser, requestUser);
        }
    }

    private AdmOperator findOperator(String operatorId) {
        return findOperators().stream()
                .filter(operator -> operator.operatorId().equals(operatorId))
                .findFirst()
                .orElseThrow(() -> new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId));
    }

    private OperatorState loadOperatorState(String operatorId) {
        return admJdbcTemplate.query("""
                        SELECT u.OPERATOR_ID, u.OPERATOR_NAME, u.PASSWORD_HASH, u.LOCKED_YN, u.FAIL_COUNT,
                               u.PASSWORD_CHANGED_AT, u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT,
                               GROUP_CONCAT(ur.ROLE_ID ORDER BY ur.ROLE_ID SEPARATOR ',') AS ROLE_IDS
                        FROM adm_operator u
                        LEFT JOIN adm_operator_role ur ON ur.OPERATOR_ID = u.OPERATOR_ID
                        WHERE u.OPERATOR_ID = ? AND u.USE_YN = 'Y'
                        GROUP BY u.OPERATOR_ID, u.OPERATOR_NAME, u.PASSWORD_HASH, u.LOCKED_YN, u.FAIL_COUNT,
                                 u.PASSWORD_CHANGED_AT, u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT
                        """,
                rs -> {
                    if (!rs.next()) {
                        throw new CpfValidationException("운영자 인증에 실패했습니다.");
                    }
                    return new OperatorState(
                            rs.getString("OPERATOR_ID"), rs.getString("OPERATOR_NAME"),
                            rs.getString("PASSWORD_HASH"), parseRoleIds(rs.getString("ROLE_IDS")), "Y".equals(rs.getString("LOCKED_YN")),
                            rs.getInt("FAIL_COUNT"), "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                            toLocalDateTime(rs.getTimestamp("PASSWORD_CHANGED_AT")), stringTime(rs.getTimestamp("CREATED_AT")),
                            stringTime(rs.getTimestamp("UPDATED_AT")));
                }, operatorId);
    }

    private AdmOperator authenticateFallback(String operatorId, String password) {
        OperatorState state = operators.get(operatorId);
        if (state == null || state.locked) {
            throw new CpfValidationException("운영자 인증에 실패했습니다.");
        }
        CpfPasswordVerification verification = verifyPassword(password, state.passwordHash);
        if (!verification.matched()) {
            state.failedLoginCount++;
            if (state.failedLoginCount >= passwordPolicyService.maxFailCount()) {
                state.locked = true;
            }
            state.updatedAt = CpfTimes.nowDateTimeMillis();
            throw new CpfValidationException("운영자 인증에 실패했습니다.");
        }
        state.failedLoginCount = 0;
        if (verification.rehashRequired()) {
            state.passwordHash = hashPassword(password);
        }
        state.updatedAt = CpfTimes.nowDateTimeMillis();
        return toResponse(state);
    }

    private void requirePasswordNotReused(String operatorId, String newPassword, OperatorState current) {
        if (matchesPassword(newPassword, current.passwordHash)) {
            throw new CpfValidationException("최근 사용한 비밀번호는 다시 사용할 수 없습니다.");
        }
        int historyLimit = Math.max(0, passwordPolicyService.historyCount() - 1);
        if (historyLimit == 0) {
            return;
        }
        List<String> historyHashes = admJdbcTemplate.queryForList("""
                SELECT PASSWORD_HASH
                FROM adm_password_history
                WHERE OPERATOR_ID = ?
                ORDER BY created_at DESC, HISTORY_ID DESC
                LIMIT ?
                """, String.class, operatorId, historyLimit);
        if (historyHashes.stream().anyMatch(historyHash -> matchesPassword(newPassword, historyHash))) {
            throw new CpfValidationException("최근 사용한 비밀번호는 다시 사용할 수 없습니다.");
        }
    }

    private void requirePasswordNotReused(String newPassword, OperatorState state) {
        if (matchesPassword(newPassword, state.passwordHash)
                || state.passwordHistoryHashes.stream().anyMatch(historyHash -> matchesPassword(newPassword, historyHash))) {
            throw new CpfValidationException("최근 사용한 비밀번호는 다시 사용할 수 없습니다.");
        }
    }

    private void rememberPassword(OperatorState state, String passwordHash) {
        state.passwordHistoryHashes.add(0, passwordHash);
        int retainedHistory = Math.max(0, passwordPolicyService.historyCount() - 1);
        while (state.passwordHistoryHashes.size() > retainedHistory) {
            state.passwordHistoryHashes.remove(state.passwordHistoryHashes.size() - 1);
        }
    }

    private AdmOperator toResponse(OperatorState state) {
        OperatorContactProfile contact = findOperatorContactProfile(state.operatorId);
        return new AdmOperator(state.operatorId, state.operatorName, contact.mobileNo(), contact.officePhoneNo(),
                state.roleIds, state.locked, passwordPolicyService.isExpired(state.passwordChangedAt),
                state.passwordChangeRequired, state.createdAt, state.updatedAt);
    }

    /**
     * 운영자 인증 Identity와 연락처 Directory Profile의 저장 책임을 분리합니다.
     */
    private void upsertOperatorContactProfile(String operatorId, String mobileNo, String officePhoneNo, String requestUser) {
        String normalizedMobileNo = blankToNull(mobileNo);
        String normalizedOfficePhoneNo = blankToNull(officePhoneNo);
        if (normalizedMobileNo == null && normalizedOfficePhoneNo == null) {
            return;
        }
        admJdbcTemplate.update("""
                INSERT INTO adm_operator_profile (
                    OPERATOR_ID, MOBILE_NO, OFFICE_PHONE_NO, CREATED_BY, UPDATED_BY
                ) VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    MOBILE_NO = VALUES(MOBILE_NO),
                    OFFICE_PHONE_NO = VALUES(OFFICE_PHONE_NO),
                    UPDATED_BY = VALUES(UPDATED_BY),
                    UPDATED_AT = CURRENT_TIMESTAMP
                """, operatorId, normalizedMobileNo, normalizedOfficePhoneNo, requestUser, requestUser);
    }

    private OperatorContactProfile findOperatorContactProfile(String operatorId) {
        try {
            List<OperatorContactProfile> profiles = admJdbcTemplate.query("""
                    SELECT MOBILE_NO, OFFICE_PHONE_NO
                    FROM adm_operator_profile
                    WHERE OPERATOR_ID = ?
                    """, (rs, rowNum) -> new OperatorContactProfile(
                    rs.getString("MOBILE_NO"), rs.getString("OFFICE_PHONE_NO")), operatorId);
            if (!profiles.isEmpty()) {
                return profiles.getFirst();
            }
        } catch (DataAccessException ex) {
            log.debug("ADM 운영자 연락처 Profile 조회를 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
        }
        return operatorContactProfiles.getOrDefault(operatorId, OperatorContactProfile.EMPTY);
    }

    private void seedFallback() {
        fallbackRoles.add(new AdmRole("ADM_ADMIN", "프레임워크 관리자", "모든 ADM 메뉴와 운영 작업을 관리합니다."));
        fallbackRoles.add(new AdmRole("ADM_DEV_OPERATOR", "개발자 운영자", "로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다."));
        fallbackRoles.add(new AdmRole("ADM_BIZ_OPERATOR", "업무 운영자", "거래 로그, 배치, 캐시 같은 공통 업무 운영 기능을 수행합니다."));
        fallbackRoles.add(new AdmRole("ADM_VIEWER", "조회 전용 운영자", "운영 정보를 조회만 할 수 있습니다."));
        fallbackRoles.add(new AdmRole("ADM_OPERATOR", "운영자 호환 역할", "기존 ADM_OPERATOR 호환을 위한 역할입니다."));

        fallbackMenus.add(new AdmMenu("DASHBOARD", null, "대시보드", "/adm", 10));
        fallbackMenus.add(new AdmMenu("LOG_LIST", null, "온라인 거래 로그", "/adm#logs", 20));
        fallbackMenus.add(new AdmMenu("STANDARD_EXECUTION", null, "표준 실행 카탈로그", "/adm#standard-executions", 23));
        fallbackMenus.add(new AdmMenu("REMOTE_LOG", null, "원격 로그 관리", "/adm#remote-logs", 24));
        fallbackMenus.add(new AdmMenu("AUDIT_LOG", null, "감사 로그", "/adm#audit-logs", 30));
        fallbackMenus.add(new AdmMenu("BATCH", null, "배치 관제", "/adm#batch", 50));
        fallbackMenus.add(new AdmMenu("CACHE", null, "캐시 관리", "/adm#cache", 60));
        fallbackMenus.add(new AdmMenu("MESSAGE", null, "메시지 관리", "/adm#messages", 70));
        fallbackMenus.add(new AdmMenu("CODE", null, "코드 관리", "/adm#codes", 80));
        fallbackMenus.add(new AdmMenu("RESPONSE_CODE", null, "응답코드 관리", "/adm#response-codes", 90));
        fallbackMenus.add(new AdmMenu("CONFIG", null, "설정 관리", "/adm#configs", 100));
        fallbackMenus.add(new AdmMenu("DYNAMIC_LOG", null, "동적 로그 레벨", "/adm#log-level", 110));
        fallbackMenus.add(new AdmMenu("PASSWORD", null, "비밀번호 관리", "/adm#password", 120));
        fallbackMenus.add(new AdmMenu("SECURITY", null, "보안 운영", "/adm#security", 130));
        fallbackMenus.add(new AdmMenu("PERMISSION", null, "권한 관리", "/adm#permissions", 140));
        fallbackMenus.add(new AdmMenu("OPERATOR", null, "운영자 관리", "/adm#operators", 150));

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
        if (roleIds.contains("ADM_OPERATOR") || roleIds.contains("ADM_DEV_OPERATOR")) {
            return fallbackMenus.stream()
                    .filter(menu -> !"OPERATOR".equals(menu.menuId())
                            && !"PERMISSION".equals(menu.menuId())
                            && !"PASSWORD".equals(menu.menuId())
                            && !"SECURITY".equals(menu.menuId()))
                    .map(menu -> switch (menu.menuId()) {
                        case "BATCH", "CACHE", "MESSAGE", "CODE", "RESPONSE_CODE", "CONFIG", "DYNAMIC_LOG" ->
                                new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                                        menu.path(), menu.sortOrder(), true, true, "MESSAGE".equals(menu.menuId()) || "CODE".equals(menu.menuId()));
                        default -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                                menu.path(), menu.sortOrder(), true, false, false);
                    })
                    .sorted(Comparator.comparingInt(AdmMenu::sortOrder))
                    .toList();
        }
        if (roleIds.contains("ADM_BIZ_OPERATOR")) {
            return fallbackMenus.stream()
                    .filter(menu -> List.of("DASHBOARD", "LOG_LIST", "STANDARD_EXECUTION", "REMOTE_LOG", "AUDIT_LOG", "BATCH", "CACHE", "MESSAGE", "CODE").contains(menu.menuId()))
                    .map(menu -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                            menu.path(), menu.sortOrder(), true, List.of("BATCH", "CACHE").contains(menu.menuId()), false))
                    .sorted(Comparator.comparingInt(AdmMenu::sortOrder))
                    .toList();
        }
        return fallbackMenus.stream()
                .filter(menu -> List.of("DASHBOARD", "LOG_LIST", "STANDARD_EXECUTION", "REMOTE_LOG", "AUDIT_LOG", "BATCH", "CACHE", "MESSAGE", "CODE", "RESPONSE_CODE", "CONFIG").contains(menu.menuId()))
                .map(menu -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                        menu.path(), menu.sortOrder(), true, false, false))
                .sorted(Comparator.comparingInt(AdmMenu::sortOrder))
                .toList();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String stringTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }

    private String hashPassword(String password) {
        char[] rawPassword = password.toCharArray();
        try {
            return passwordHashingPort.hash(rawPassword);
        } finally {
            java.util.Arrays.fill(rawPassword, '\0');
        }
    }

    private boolean matchesPassword(String rawPassword, String storedHash) {
        return verifyPassword(rawPassword, storedHash).matched();
    }

    private CpfPasswordVerification verifyPassword(String rawPassword, String storedHash) {
        char[] passwordChars = rawPassword.toCharArray();
        try {
            return passwordHashingPort.verify(passwordChars, storedHash);
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private record OperatorContactProfile(String mobileNo, String officePhoneNo) {
        private static final OperatorContactProfile EMPTY = new OperatorContactProfile(null, null);
    }

    private static class OperatorState {
        private final String operatorId;
        private final String operatorName;
        private final String createdAt;
        private List<String> roleIds;
        private String passwordHash;
        private boolean locked;
        private int failedLoginCount;
        private boolean passwordChangeRequired;
        private LocalDateTime passwordChangedAt;
        private String updatedAt;
        private final List<String> passwordHistoryHashes = new ArrayList<>();

        private OperatorState(String operatorId, String operatorName, String passwordHash, List<String> roleIds,
                              boolean locked, int failedLoginCount,
                              boolean passwordChangeRequired, LocalDateTime passwordChangedAt,
                              String createdAt, String updatedAt) {
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
