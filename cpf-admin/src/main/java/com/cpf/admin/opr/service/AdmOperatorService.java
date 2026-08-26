package com.cpf.admin.opr.service;

import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.admin.opr.dto.AdmLoginRequest;
import com.cpf.admin.opr.dto.AdmMenu;
import com.cpf.admin.opr.dto.AdmOperator;
import com.cpf.admin.opr.dto.AdmOperatorCreateRequest;
import com.cpf.admin.opr.dto.AdmOperatorRawContactResponse;
import com.cpf.admin.opr.dto.AdmOperatorContactUpdateRequest;
import com.cpf.admin.opr.dto.AdmOperatorStatusUpdateRequest;
import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.admin.opr.dto.AdmOperatorPasswordResetRequest;
import com.cpf.admin.opr.dto.AdmOperatorRoleUpdateRequest;
import com.cpf.admin.opr.dto.AdmPasswordChangeRequest;
import com.cpf.admin.opr.dto.AdmRole;
import com.cpf.admin.opr.dto.AdmPasswordPolicyResponse;
import com.cpf.admin.opr.dto.AdmPasswordValidationResponse;
import com.cpf.foundation.util.CpfTimes;
import com.cpf.foundation.util.CpfStrings;
import com.cpf.core.api.error.CpfBusinessException;
import com.cpf.core.api.error.CpfErrorCode;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.security.api.password.CpfPasswordEncoder;
import com.cpf.security.api.password.CpfPasswordVerification;
import com.cpf.security.api.CpfSensitiveData;
import com.cpf.core.api.context.CpfContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import com.cpf.foundation.annotation.CpfService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ADM 운영자, 역할, 메뉴 권한을 관리합니다.
 *
 * <p>제품 기본 모드는 DB fail-closed입니다. 메모리 저장소는 local/test/demo/library 프로필에서
 * 명시적으로 MEMORY 모드를 선택했을 때만 사용하며 DB 오류를 성공 응답으로 변환하지 않습니다.</p>
 */
@CpfService
public class AdmOperatorService extends com.cpf.admin.common.base.AdmBaseService {
    private static final Logger log = LoggerFactory.getLogger(AdmOperatorService.class);
    private static final Set<String> ACCOUNT_STATES = Set.of("PENDING_ACTIVATION", "ACTIVE", "LOCKED", "SUSPENDED", "DISABLED");
    private static final Map<String, Set<String>> ACCOUNT_TRANSITIONS = Map.of(
            "PENDING_ACTIVATION", Set.of("ACTIVE", "DISABLED"),
            "ACTIVE", Set.of("SUSPENDED", "LOCKED", "DISABLED"),
            "LOCKED", Set.of("ACTIVE", "SUSPENDED", "DISABLED"),
            "SUSPENDED", Set.of("ACTIVE", "DISABLED"),
            "DISABLED", Set.of());
    private final AdmPasswordPolicyService passwordPolicyService;
    private final CpfPasswordEncoder passwordHashingPort;
    private final JdbcTemplate admJdbcTemplate;
    private final AdmPersistencePolicy persistencePolicy;
    private final AdmSessionService sessionService;
    private final ConcurrentMap<String, OperatorState> operators = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> createOperationOwners = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, OperatorContactProfile> operatorContactProfiles = new ConcurrentHashMap<>();
    private final List<AdmRole> fallbackRoles = new ArrayList<>();
    private final List<AdmMenu> fallbackMenus = new ArrayList<>();

    public AdmOperatorService(AdmPasswordPolicyService passwordPolicyService,
                              CpfPasswordEncoder passwordHashingPort,
                              @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
                              AdmPersistencePolicy persistencePolicy,
                              AdmSessionService sessionService) {
        this.passwordPolicyService = passwordPolicyService;
        this.passwordHashingPort = passwordHashingPort;
        this.admJdbcTemplate = admJdbcTemplate;
        this.persistencePolicy = persistencePolicy;
        this.sessionService = sessionService;
        if (persistencePolicy.memoryEnabled()) {
            seedFallback();
        }
    }

    public List<AdmOperator> findOperators() {
        try {
            List<OperatorDirectoryRow> rows = admJdbcTemplate.query("""
                    SELECT u.OPERATOR_ID, COALESCE(p.DISPLAY_NAME, u.OPERATOR_NAME) AS OPERATOR_NAME,
                           p.MOBILE_NO, p.OFFICE_PHONE_NO, u.ACCOUNT_STATUS, u.VERSION_NO,
                           u.LOCKED_YN, u.PASSWORD_CHANGED_AT,
                           u.PASSWORD_CHANGE_REQUIRED_YN, u.CREATED_AT, u.UPDATED_AT
                    FROM adm_operator u
                    LEFT JOIN adm_operator_profile p ON p.OPERATOR_ID = u.OPERATOR_ID
                    WHERE u.USE_YN = 'Y'
                    ORDER BY u.OPERATOR_ID
                    """, (rs, rowNum) -> new OperatorDirectoryRow(
                    rs.getString("OPERATOR_ID"), rs.getString("OPERATOR_NAME"),
                    rs.getString("MOBILE_NO"), rs.getString("OFFICE_PHONE_NO"),
                    rs.getString("ACCOUNT_STATUS"), rs.getLong("VERSION_NO"),
                    "Y".equals(rs.getString("LOCKED_YN")),
                    toLocalDateTime(rs.getTimestamp("PASSWORD_CHANGED_AT")),
                    "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                    stringTime(rs.getTimestamp("CREATED_AT")), stringTime(rs.getTimestamp("UPDATED_AT"))));
            return rows.stream().map(row -> new AdmOperator(
                    row.operatorId(), row.operatorName(),
                    CpfSensitiveData.maskPhone(row.mobileNo()), CpfSensitiveData.maskPhone(row.officePhoneNo()),
                    row.accountStatus(), row.versionNo(), findRoleIds(row.operatorId()), row.locked(),
                    passwordPolicyService.isExpired(row.passwordChangedAt()), row.passwordChangeRequired(), false,
                    row.createdAt(), row.updatedAt())).toList();
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            return operators.values().stream()
                    .map(this::toResponse)
                    .sorted(Comparator.comparing(value -> value.operatorId()))
                    .toList();
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public AdmOperator createOperator(AdmOperatorCreateRequest request) {
        String operatorId = CpfStrings.requireText(request.operatorId(), "operatorId");
        String operatorName = CpfStrings.requireText(request.operatorName(), "operatorName");
        String operationId = CpfStrings.requireText(request.operationId(), "operationId");
        List<String> requestedRoles = request.roleIds() == null ? List.of() : request.roleIds().stream()
                .filter(roleId -> roleId != null && !roleId.isBlank()).toList();
        if (!requestedRoles.isEmpty()) {
            throw new CpfValidationException("일반 운영자 생성에서는 역할을 함께 부여할 수 없습니다. 생성 후 별도 권한 작업을 사용하십시오.");
        }
        passwordPolicyService.requireValid(operatorId, request.password());
        String passwordHash = hashPassword(request.password());
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");

        try {
            List<String> prior = admJdbcTemplate.queryForList(
                    "SELECT OPERATOR_ID FROM adm_operator WHERE CREATE_OPERATION_ID = ?", String.class, operationId);
            if (!prior.isEmpty()) {
                if (!prior.getFirst().equals(operatorId)) {
                    throw new CpfValidationException("operationId가 다른 운영자 생성에 이미 사용되었습니다.");
                }
                return findOperator(operatorId);
            }
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator (
                        OPERATOR_ID, OPERATOR_NAME, PASSWORD_HASH, ACCOUNT_STATUS, VERSION_NO, CREATE_OPERATION_ID, LOCKED_YN, FAIL_COUNT,
                        PASSWORD_CHANGED_AT, PASSWORD_CHANGE_REQUIRED_YN, USE_YN, CREATED_BY, UPDATED_BY
                    ) VALUES (?, ?, ?, 'PENDING_ACTIVATION', 0, ?, 'N', 0, CURRENT_TIMESTAMP, 'Y', 'Y', ?, ?)
                    """, operatorId, operatorName, passwordHash, operationId, requestUser, requestUser);
            upsertOperatorContactProfile(operatorId, operatorName, request.mobileNo(), request.officePhoneNo(), requestUser);
        } catch (DuplicateKeyException ex) {
            if (persistencePolicy.databaseRequired()) {
                List<String> owners = admJdbcTemplate.queryForList(
                        "SELECT OPERATOR_ID FROM adm_operator WHERE CREATE_OPERATION_ID = ?", String.class, operationId);
                if (!owners.isEmpty() && owners.getFirst().equals(operatorId)) {
                    return findOperator(operatorId);
                }
                throw new CpfValidationException("이미 존재하는 운영자이거나 operationId가 사용되었습니다. operatorId=" + operatorId);
            }
            throw ex;
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            String priorOwner = createOperationOwners.putIfAbsent(operationId, operatorId);
            if (priorOwner != null) {
                if (!priorOwner.equals(operatorId)) {
                    throw new CpfValidationException("operationId가 다른 운영자 생성에 이미 사용되었습니다.");
                }
                OperatorState priorState = operators.get(operatorId);
                if (priorState != null) return toResponse(priorState);
            }
            OperatorState state = new OperatorState(operatorId, operatorName, passwordHash, "PENDING_ACTIVATION", 0, List.of(), false, 0, true,
                    LocalDateTime.now(), CpfTimes.nowDateTimeMillis(), CpfTimes.nowDateTimeMillis());
            if (operators.putIfAbsent(operatorId, state) != null) {
                createOperationOwners.remove(operationId, operatorId);
                throw new CpfValidationException("이미 존재하는 운영자입니다. operatorId=" + operatorId);
            }
            operatorContactProfiles.put(operatorId, new OperatorContactProfile(
                    CpfSensitiveData.normalizePhone(request.mobileNo(), "mobileNo"),
                    CpfSensitiveData.normalizePhone(request.officePhoneNo(), "officePhoneNo")));
            return toResponse(state);
        }
        return findOperator(operatorId);
    }

    /** 결과불명 재시도에서 동일 operationId로 생성 결과를 조회합니다. */
    public AdmOperator findOperatorByCreateOperationId(String operationIdValue) {
        String operationId = CpfStrings.requireText(operationIdValue, "operationId");
        if (persistencePolicy.memoryEnabled()) {
            String operatorId = createOperationOwners.get(operationId);
            if (operatorId == null) {
                throw new CpfNotFoundException("operationId에 해당하는 운영자 생성 결과가 없습니다.");
            }
            return findOperator(operatorId);
        }
        try {
            List<String> operatorIds = admJdbcTemplate.queryForList(
                    "SELECT OPERATOR_ID FROM adm_operator WHERE CREATE_OPERATION_ID = ?", String.class, operationId);
            if (operatorIds.isEmpty()) {
                throw new CpfNotFoundException("operationId에 해당하는 운영자 생성 결과가 없습니다.");
            }
            return findOperator(operatorIds.getFirst());
        } catch (DataAccessException ex) {
            throw unavailable(ex);
        }
    }

    /**
     * 환경변수로 승인된 최초 운영자 계정을 한 번만 생성합니다.
     *
     * <p>이미 같은 운영자가 있으면 비밀번호와 역할을 변경하지 않습니다. DB가 없는 로컬 fallback도
     * 같은 idempotency 규칙을 적용합니다.</p>
     *
     * @return 새 계정을 생성했으면 {@code true}, 이미 존재하면 {@code false}
     */
    @CpfTransactional(transactionManager="admTransactionManager")
    public boolean bootstrapOperator(String operatorIdValue, String operatorNameValue, String password) {
        String operatorId = CpfStrings.requireText(operatorIdValue, "operatorId");
        String operatorName = CpfStrings.requireText(operatorNameValue, "operatorName");
        passwordPolicyService.requireValid(operatorId, password);
        String passwordHash = hashPassword(password);
        try {
            int inserted = admJdbcTemplate.update("""
                    INSERT INTO adm_operator (
                        OPERATOR_ID, OPERATOR_NAME, PASSWORD_HASH, ACCOUNT_STATUS, VERSION_NO, LOCKED_YN, FAIL_COUNT,
                        PASSWORD_CHANGED_AT, PASSWORD_CHANGE_REQUIRED_YN, USE_YN, CREATED_BY, UPDATED_BY
                    )
                    SELECT ?, ?, ?, 'ACTIVE', 0, 'N', 0, CURRENT_TIMESTAMP, 'Y', 'Y', 'BOOTSTRAP', 'BOOTSTRAP'
                    WHERE NOT EXISTS (
                        SELECT 1 FROM adm_operator WHERE OPERATOR_ID = ?
                    )
                    """, operatorId, operatorName, passwordHash, operatorId);
            if (inserted > 0) {
                replaceRoles(operatorId, List.of("ADM_ADMIN"), "BOOTSTRAP");
            }
            return inserted > 0;
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            log.debug("ADM bootstrap DB 처리를 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
            OperatorState state = new OperatorState(
                    operatorId,
                    operatorName,
                    passwordHash,
                    "ACTIVE",
                    0,
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
            if (!"ACTIVE".equals(state.accountStatus)) {
                throw new CpfValidationException("활성화되지 않은 운영자 계정입니다. operatorId=" + operatorId);
            }
            if (state.locked) {
                throw new CpfValidationException("잠긴 운영자 계정입니다. operatorId=" + operatorId);
            }
            CpfPasswordVerification verification = verifyPassword(password, state.passwordHash);
            if (!verification.matched()) {
                int failed = state.failedLoginCount + 1;
                boolean locked = failed >= passwordPolicyService.maxFailCount();
                admJdbcTemplate.update("""
                        UPDATE adm_operator
                        SET FAIL_COUNT = ?, LOCKED_YN = ?, ACCOUNT_STATUS = CASE WHEN ? = 'Y' THEN 'LOCKED' ELSE ACCOUNT_STATUS END,
                            VERSION_NO = VERSION_NO + 1, UPDATED_BY = 'ADM', UPDATED_AT = CURRENT_TIMESTAMP
                        WHERE OPERATOR_ID = ?
                        """, failed, locked ? "Y" : "N", locked ? "Y" : "N", operatorId);
                throw new CpfValidationException("운영자 인증에 실패했습니다.");
            }
            admJdbcTemplate.update("""
                    UPDATE adm_operator
                    SET FAIL_COUNT = 0,
                        LAST_LOGIN_AT = CURRENT_TIMESTAMP,
                        VERSION_NO = VERSION_NO + 1,
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
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            log.debug("ADM 운영자 DB 인증을 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
            return authenticateFallback(operatorId, password);
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
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
                        ACCOUNT_STATUS = CASE WHEN ACCOUNT_STATUS = 'LOCKED' THEN 'ACTIVE' ELSE ACCOUNT_STATUS END,
                        VERSION_NO = VERSION_NO + 1,
                        UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND PASSWORD_HASH = ? AND USE_YN = 'Y'
                    """, hash, operatorId, operatorId, before.passwordHash);
            if (updated == 0) {
                throw new CpfValidationException("비밀번호가 동시에 변경되었습니다. 다시 로그인한 뒤 재시도하세요.");
            }
            sessionService.revokeOperatorSessions(operatorId);
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
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
                if ("LOCKED".equals(state.accountStatus)) state.accountStatus = "ACTIVE";
                state.versionNo++;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                sessionService.revokeOperatorSessions(operatorId);
                return toResponse(state);
            }
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
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
                        ACCOUNT_STATUS = CASE WHEN ACCOUNT_STATUS = 'LOCKED' THEN 'ACTIVE' ELSE ACCOUNT_STATUS END,
                        VERSION_NO = VERSION_NO + 1,
                        UPDATED_BY = ?,
                        UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y'
                    """, hash, request.forceChange() ? "Y" : "N", requestUser, operatorId);
            if (updated == 0) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            sessionService.revokeOperatorSessions(operatorId);
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
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
                if ("LOCKED".equals(state.accountStatus)) state.accountStatus = "ACTIVE";
                state.versionNo++;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                sessionService.revokeOperatorSessions(operatorId);
                return toResponse(state);
            }
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public AdmOperator unlockOperator(String operatorId, String requestUser) {
        String user = CpfStrings.defaultIfBlank(requestUser, "ADM");
        try {
            Integer roleCount = admJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM adm_operator_role WHERE OPERATOR_ID = ?", Integer.class, operatorId);
            if (roleCount == null || roleCount == 0) {
                throw new CpfValidationException("역할이 없는 운영자는 잠금 해제 후 ACTIVE로 전환할 수 없습니다.");
            }
            int updated = admJdbcTemplate.update("""
                    UPDATE adm_operator
                    SET LOCKED_YN = 'N',
                        FAIL_COUNT = 0,
                        ACCOUNT_STATUS = 'ACTIVE',
                        VERSION_NO = VERSION_NO + 1,
                        UPDATED_BY = ?,
                        UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y' AND ACCOUNT_STATUS = 'LOCKED'
                    """, user, operatorId);
            if (updated == 0) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            sessionService.revokeOperatorSessions(operatorId);
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            OperatorState state = operators.get(operatorId);
            if (state == null) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            if (!"LOCKED".equals(state.accountStatus)) {
                throw new CpfValidationException("LOCKED 상태의 운영자만 잠금 해제할 수 있습니다.");
            }
            if (state.roleIds.isEmpty()) {
                throw new CpfValidationException("역할이 없는 운영자는 잠금 해제 후 ACTIVE로 전환할 수 없습니다.");
            }
            state.failedLoginCount = 0;
            state.locked = false;
            state.accountStatus = "ACTIVE";
            state.versionNo++;
            state.updatedAt = CpfTimes.nowDateTimeMillis();
            sessionService.revokeOperatorSessions(operatorId);
            return toResponse(state);
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public AdmOperator updateRoles(String operatorId, AdmOperatorRoleUpdateRequest request) {
        List<String> roleIds = request.roleIds() == null ? List.of() : request.roleIds().stream()
                .filter(roleId -> roleId != null && !roleId.isBlank()).distinct().sorted().toList();
        String requestUser = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");
        try {
            Integer operatorCount = admJdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM adm_operator WHERE OPERATOR_ID = ? AND USE_YN = 'Y'", Integer.class, operatorId);
            if (operatorCount == null || operatorCount == 0) {
                throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            }
            replaceRoles(operatorId, roleIds, requestUser);
            int updated = admJdbcTemplate.update("""
                    UPDATE adm_operator SET VERSION_NO = VERSION_NO + 1, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                    WHERE OPERATOR_ID = ? AND USE_YN = 'Y'
                    """, requestUser, operatorId);
            if (updated != 1) throw new CpfValidationException("운영자 역할 변경에 실패했습니다.");
            sessionService.revokeOperatorSessions(operatorId);
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            OperatorState state = operators.get(operatorId);
            if (state == null) throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            synchronized (state) {
                state.roleIds = roleIds;
                state.versionNo++;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                sessionService.revokeOperatorSessions(operatorId);
                return toResponse(state);
            }
        }
    }



    public AdmOperatorRawContactResponse findOperatorRaw(String operatorId) {
        String id = CpfStrings.requireText(operatorId, "operatorId");
        if (persistencePolicy.memoryEnabled()) {
            if (!operators.containsKey(id)) throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + id);
            OperatorContactProfile contact = operatorContactProfiles.getOrDefault(id, OperatorContactProfile.EMPTY);
            return new AdmOperatorRawContactResponse(id, contact.mobileNo(), contact.officePhoneNo(), true, CpfContexts.transactionId());
        }
        try {
            List<AdmOperatorRawContactResponse> rows = admJdbcTemplate.query("""
                    SELECT u.OPERATOR_ID, p.MOBILE_NO, p.OFFICE_PHONE_NO
                    FROM adm_operator u
                    LEFT JOIN adm_operator_profile p ON p.OPERATOR_ID = u.OPERATOR_ID
                    WHERE u.OPERATOR_ID = ? AND u.USE_YN = 'Y'
                    """, (rs, rowNum) -> new AdmOperatorRawContactResponse(
                    rs.getString("OPERATOR_ID"), rs.getString("MOBILE_NO"), rs.getString("OFFICE_PHONE_NO"),
                    true, CpfContexts.transactionId()), id);
            if (rows.isEmpty()) throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + id);
            return rows.getFirst();
        } catch (DataAccessException ex) {
            throw unavailable(ex);
        }
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public AdmOperator updateContact(String operatorId, AdmOperatorContactUpdateRequest request) {
        if (request.expectedVersion() == null) {
            throw new CpfValidationException("연락처 수정에는 expectedVersion이 필요합니다.");
        }
        String user = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");
        if (persistencePolicy.memoryEnabled()) {
            OperatorState state = operators.get(operatorId);
            if (state == null) throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            synchronized (state) {
                if (state.versionNo != request.expectedVersion()) throw new CpfValidationException("운영자 정보가 동시에 변경되었습니다.");
                OperatorContactProfile current = operatorContactProfiles.getOrDefault(operatorId, OperatorContactProfile.EMPTY);
                String mobile = request.clearMobileNo() ? null
                        : request.mobileNo() == null || request.mobileNo().isBlank() ? current.mobileNo()
                        : CpfSensitiveData.normalizePhone(request.mobileNo(), "mobileNo");
                String office = request.clearOfficePhoneNo() ? null
                        : request.officePhoneNo() == null || request.officePhoneNo().isBlank() ? current.officePhoneNo()
                        : CpfSensitiveData.normalizePhone(request.officePhoneNo(), "officePhoneNo");
                operatorContactProfiles.put(operatorId, new OperatorContactProfile(mobile, office));
                state.versionNo++;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                return toResponse(state);
            }
        }
        OperatorContactProfile current = findOperatorContactProfile(operatorId);
        String mobile = request.clearMobileNo() ? null
                : request.mobileNo() == null || request.mobileNo().isBlank() ? current.mobileNo()
                : CpfSensitiveData.normalizePhone(request.mobileNo(), "mobileNo");
        String office = request.clearOfficePhoneNo() ? null
                : request.officePhoneNo() == null || request.officePhoneNo().isBlank() ? current.officePhoneNo()
                : CpfSensitiveData.normalizePhone(request.officePhoneNo(), "officePhoneNo");
        int updated = admJdbcTemplate.update("""
                UPDATE adm_operator
                   SET VERSION_NO = VERSION_NO + 1, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                 WHERE OPERATOR_ID = ? AND VERSION_NO = ? AND USE_YN = 'Y'
                """, user, operatorId, request.expectedVersion());
        if (updated != 1) throw new CpfValidationException("운영자 정보가 동시에 변경되었습니다. 다시 조회하십시오.");
        upsertOperatorContactProfile(operatorId, null, mobile, office, user);
        return findOperator(operatorId);
    }

    @CpfTransactional(transactionManager="admTransactionManager")
    public AdmOperator updateAccountStatus(String operatorId, AdmOperatorStatusUpdateRequest request) {
        if (request.expectedVersion() == null) {
            throw new CpfValidationException("상태 변경에는 expectedVersion이 필요합니다.");
        }
        String status = CpfStrings.requireText(request.accountStatus(), "accountStatus").toUpperCase(java.util.Locale.ROOT);
        if (!ACCOUNT_STATES.contains(status)) throw new CpfValidationException("지원하지 않는 운영자 계정 상태입니다.");
        String user = CpfStrings.defaultIfBlank(request.requestUser(), "ADM");

        if (persistencePolicy.memoryEnabled()) {
            OperatorState state = operators.get(operatorId);
            if (state == null) throw new CpfNotFoundException("운영자를 찾을 수 없습니다. operatorId=" + operatorId);
            synchronized (state) {
                if (state.versionNo != request.expectedVersion()) throw new CpfValidationException("운영자 정보가 동시에 변경되었습니다.");
                requireAllowedTransition(state.accountStatus, status, state.roleIds);
                state.accountStatus = status;
                state.locked = "LOCKED".equals(status);
                state.versionNo++;
                state.updatedAt = CpfTimes.nowDateTimeMillis();
                if (!"ACTIVE".equals(status)) sessionService.revokeOperatorSessions(operatorId);
                return toResponse(state);
            }
        }

        try {
            OperatorState current = loadOperatorState(operatorId);
            requireAllowedTransition(current.accountStatus, status, current.roleIds);
            int updated = admJdbcTemplate.update("""
                    UPDATE adm_operator
                       SET ACCOUNT_STATUS = ?, LOCKED_YN = CASE WHEN ? = 'LOCKED' THEN 'Y' ELSE 'N' END,
                           VERSION_NO = VERSION_NO + 1, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE OPERATOR_ID = ? AND VERSION_NO = ? AND ACCOUNT_STATUS = ? AND USE_YN = 'Y'
                    """, status, status, user, operatorId, request.expectedVersion(), current.accountStatus);
            if (updated != 1) throw new CpfValidationException("운영자 정보가 동시에 변경되었습니다. 다시 조회하십시오.");
            if (!"ACTIVE".equals(status)) sessionService.revokeOperatorSessions(operatorId);
            return findOperator(operatorId);
        } catch (DataAccessException ex) {
            throw unavailable(ex);
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
            useMemoryFallbackOrThrow(ex);
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
            useMemoryFallbackOrThrow(ex);
            return fallbackMenus.stream().sorted(Comparator.comparingInt(value -> value.sortOrder())).toList();
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
            useMemoryFallbackOrThrow(ex);
            if (roleIds.contains("ADM_ADMIN")) {
                return findMenus();
            }
            return fallbackMenusForRoles(roleIds);
        }
    }

    /** 현재 Role 집합에 실제 허용된 Action Button ID를 반환합니다. */
    public List<String> findButtonIdsForRoles(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return List.of();
        }
        try {
            String placeholders = String.join(",", roleIds.stream().map(role -> "?").toList());
            return admJdbcTemplate.queryForList("""
                    SELECT DISTINCT b.BUTTON_ID
                    FROM adm_button b
                    JOIN adm_role_button rb ON rb.BUTTON_ID = b.BUTTON_ID
                    WHERE b.USE_YN = 'Y'
                      AND rb.ALLOW_YN = 'Y'
                      AND rb.ROLE_ID IN (%s)
                    ORDER BY b.BUTTON_ID
                    """.formatted(placeholders), String.class, roleIds.toArray());
        } catch (DataAccessException ex) {
            useMemoryFallbackOrThrow(ex);
            // MEMORY는 local/test 전용이며 메뉴 권한으로 fallback하고 Backend Filter가 최종 차단합니다.
            return List.of();
        }
    }

    public AdmPasswordValidationResponse validatePassword(String operatorId, String password) {
        return new AdmPasswordValidationResponse(operatorId, passwordPolicyService.validate(operatorId, password));
    }

    public AdmPasswordPolicyResponse passwordPolicy() {
        return passwordPolicyService.currentPolicy();
    }

    private void replaceRoles(String operatorId, List<String> roleIds, String requestUser) {
        admJdbcTemplate.update("DELETE FROM adm_operator_role WHERE OPERATOR_ID = ?", operatorId);
        for (String roleId : roleIds.stream().filter(value -> value != null && !value.isBlank()).distinct().sorted().toList()) {
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator_role (OPERATOR_ID, ROLE_ID, CREATED_BY, UPDATED_BY)
                    VALUES (?, ?, ?, ?)
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
        List<OperatorState> rows = admJdbcTemplate.query("""
                SELECT u.OPERATOR_ID, u.OPERATOR_NAME, u.PASSWORD_HASH, u.ACCOUNT_STATUS, u.VERSION_NO,
                       u.LOCKED_YN, u.FAIL_COUNT, u.PASSWORD_CHANGED_AT, u.PASSWORD_CHANGE_REQUIRED_YN,
                       u.CREATED_AT, u.UPDATED_AT
                FROM adm_operator u
                WHERE u.OPERATOR_ID = ? AND u.USE_YN = 'Y'
                """, (rs, rowNum) -> new OperatorState(
                rs.getString("OPERATOR_ID"), rs.getString("OPERATOR_NAME"), rs.getString("PASSWORD_HASH"),
                rs.getString("ACCOUNT_STATUS"), rs.getLong("VERSION_NO"), List.of(),
                "Y".equals(rs.getString("LOCKED_YN")), rs.getInt("FAIL_COUNT"),
                "Y".equals(rs.getString("PASSWORD_CHANGE_REQUIRED_YN")),
                toLocalDateTime(rs.getTimestamp("PASSWORD_CHANGED_AT")), stringTime(rs.getTimestamp("CREATED_AT")),
                stringTime(rs.getTimestamp("UPDATED_AT"))), operatorId);
        if (rows.isEmpty()) throw new CpfValidationException("운영자 인증에 실패했습니다.");
        OperatorState state = rows.getFirst();
        state.roleIds = findRoleIds(operatorId);
        return state;
    }

    private List<String> findRoleIds(String operatorId) {
        return admJdbcTemplate.queryForList("""
                SELECT ROLE_ID FROM adm_operator_role WHERE OPERATOR_ID = ? ORDER BY ROLE_ID
                """, String.class, operatorId);
    }

    private AdmOperator authenticateFallback(String operatorId, String password) {
        OperatorState state = operators.get(operatorId);
        if (state == null || state.locked || !"ACTIVE".equals(state.accountStatus)) {
            throw new CpfValidationException("운영자 인증에 실패했습니다.");
        }
        CpfPasswordVerification verification = verifyPassword(password, state.passwordHash);
        if (!verification.matched()) {
            state.failedLoginCount++;
            if (state.failedLoginCount >= passwordPolicyService.maxFailCount()) {
                state.locked = true;
                state.accountStatus = "LOCKED";
            }
            state.versionNo++;
            state.updatedAt = CpfTimes.nowDateTimeMillis();
            throw new CpfValidationException("운영자 인증에 실패했습니다.");
        }
        state.failedLoginCount = 0;
        if (verification.rehashRequired()) {
            state.passwordHash = hashPassword(password);
        }
        state.versionNo++;
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
                """, String.class, operatorId).stream().limit(historyLimit).toList();
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
        return new AdmOperator(state.operatorId, state.operatorName,
                CpfSensitiveData.maskPhone(contact.mobileNo()), CpfSensitiveData.maskPhone(contact.officePhoneNo()),
                state.accountStatus, state.versionNo, state.roleIds, state.locked,
                passwordPolicyService.isExpired(state.passwordChangedAt), state.passwordChangeRequired, false,
                state.createdAt, state.updatedAt);
    }

    /**
     * 운영자 인증 Identity와 Directory/Profile 저장 책임을 분리합니다.
     */
    private void upsertOperatorContactProfile(
            String operatorId, String displayName, String mobileNo, String officePhoneNo, String requestUser) {
        String normalizedMobileNo = CpfSensitiveData.normalizePhone(mobileNo, "mobileNo");
        String normalizedOfficePhoneNo = CpfSensitiveData.normalizePhone(officePhoneNo, "officePhoneNo");
        int updated = admJdbcTemplate.update("""
                UPDATE adm_operator_profile
                   SET DISPLAY_NAME = COALESCE(?, DISPLAY_NAME), MOBILE_NO = ?, OFFICE_PHONE_NO = ?,
                       VERSION_NO = VERSION_NO + 1, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                 WHERE OPERATOR_ID = ?
                """, displayName, normalizedMobileNo, normalizedOfficePhoneNo, requestUser, operatorId);
        if (updated > 0) return;
        try {
            String resolvedDisplayName = displayName;
            if (resolvedDisplayName == null || resolvedDisplayName.isBlank()) {
                resolvedDisplayName = admJdbcTemplate.queryForObject(
                        "SELECT OPERATOR_NAME FROM adm_operator WHERE OPERATOR_ID = ?", String.class, operatorId);
            }
            admJdbcTemplate.update("""
                    INSERT INTO adm_operator_profile (
                        OPERATOR_ID, DISPLAY_NAME, MOBILE_NO, OFFICE_PHONE_NO, VERSION_NO, CREATED_BY, UPDATED_BY
                    ) VALUES (?, ?, ?, ?, 0, ?, ?)
                    """, operatorId, resolvedDisplayName, normalizedMobileNo, normalizedOfficePhoneNo, requestUser, requestUser);
        } catch (DuplicateKeyException race) {
            admJdbcTemplate.update("""
                    UPDATE adm_operator_profile
                       SET DISPLAY_NAME = COALESCE(?, DISPLAY_NAME), MOBILE_NO = ?, OFFICE_PHONE_NO = ?,
                           VERSION_NO = VERSION_NO + 1, UPDATED_BY = ?, UPDATED_AT = CURRENT_TIMESTAMP
                     WHERE OPERATOR_ID = ?
                    """, displayName, normalizedMobileNo, normalizedOfficePhoneNo, requestUser, operatorId);
        }
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
            useMemoryFallbackOrThrow(ex);
            log.debug("ADM 운영자 연락처 Profile 조회를 건너뜁니다. operatorId={}, reason={}", operatorId, ex.getMessage());
        }
        return operatorContactProfiles.getOrDefault(operatorId, OperatorContactProfile.EMPTY);
    }


    private void requireAllowedTransition(String currentStatus, String targetStatus, List<String> roleIds) {
        if (currentStatus.equals(targetStatus)) {
            throw new CpfValidationException("동일한 계정 상태로는 변경할 수 없습니다. currentStatus=" + currentStatus);
        }
        Set<String> allowed = ACCOUNT_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowed.contains(targetStatus)) {
            throw new CpfValidationException("허용되지 않은 계정 상태 전이입니다. " + currentStatus + " -> " + targetStatus);
        }
        if ("ACTIVE".equals(targetStatus) && (roleIds == null || roleIds.isEmpty())) {
            throw new CpfValidationException("역할이 없는 운영자는 ACTIVE로 전환할 수 없습니다.");
        }
    }

    private CpfBusinessException unavailable(DataAccessException ex) {
        return new CpfBusinessException(CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE,
                "component=ADM_DB, reason=" + ex.getClass().getSimpleName());
    }

    private void useMemoryFallbackOrThrow(DataAccessException ex) {
        if (!persistencePolicy.memoryEnabled()) throw unavailable(ex);
    }

    private void seedFallback() {
        fallbackRoles.add(new AdmRole("ADM_ADMIN", "프레임워크 관리자", "모든 ADM 메뉴와 운영 작업을 관리합니다."));
        fallbackRoles.add(new AdmRole("ADM_DEV_OPERATOR", "개발자 운영자", "로그, 캐시, 코드, 메시지, 설정, 배치 관제를 운영합니다."));
        fallbackRoles.add(new AdmRole("ADM_BIZ_OPERATOR", "업무 운영자", "거래 로그, 배치, 캐시 같은 공통 업무 운영 기능을 수행합니다."));
        fallbackRoles.add(new AdmRole("ADM_VIEWER", "조회 전용 운영자", "운영 정보를 조회만 할 수 있습니다."));
        fallbackRoles.add(new AdmRole("ADM_OPERATOR", "운영자 호환 역할", "기존 ADM_OPERATOR 호환을 위한 역할입니다."));

        fallbackMenus.add(new AdmMenu("DASHBOARD", null, "대시보드", "/adm", 10));
        fallbackMenus.add(new AdmMenu("CAPABILITY_FLEET", null, "CPF Capability", "/adm#capabilities", 15));
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


    private List<AdmMenu> fallbackMenusForRoles(List<String> roleIds) {
        if (roleIds.contains("ADM_ADMIN")) {
            return fallbackMenus.stream().sorted(Comparator.comparingInt(value -> value.sortOrder())).toList();
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
                    .sorted(Comparator.comparingInt(value -> value.sortOrder()))
                    .toList();
        }
        if (roleIds.contains("ADM_BIZ_OPERATOR")) {
            return fallbackMenus.stream()
                    .filter(menu -> List.of("DASHBOARD", "LOG_LIST", "STANDARD_EXECUTION", "REMOTE_LOG", "AUDIT_LOG", "BATCH", "CACHE", "MESSAGE", "CODE").contains(menu.menuId()))
                    .map(menu -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                            menu.path(), menu.sortOrder(), true, List.of("BATCH", "CACHE").contains(menu.menuId()), false))
                    .sorted(Comparator.comparingInt(value -> value.sortOrder()))
                    .toList();
        }
        return fallbackMenus.stream()
                .filter(menu -> List.of("DASHBOARD", "CAPABILITY_FLEET", "LOG_LIST", "STANDARD_EXECUTION", "REMOTE_LOG", "AUDIT_LOG", "BATCH", "CACHE", "MESSAGE", "CODE", "RESPONSE_CODE", "CONFIG").contains(menu.menuId()))
                .map(menu -> new AdmMenu(menu.menuId(), menu.parentMenuId(), menu.menuName(),
                        menu.path(), menu.sortOrder(), true, false, false))
                .sorted(Comparator.comparingInt(value -> value.sortOrder()))
                .toList();
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
            return passwordHashingPort.encode(rawPassword);
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
            boolean matched=passwordHashingPort.matches(passwordChars, storedHash);
            return new CpfPasswordVerification(matched, matched && passwordHashingPort.upgradeEncoding(storedHash));
        } finally {
            java.util.Arrays.fill(passwordChars, '\0');
        }
    }

    private record OperatorContactProfile(String mobileNo, String officePhoneNo) {
        private static final OperatorContactProfile EMPTY = new OperatorContactProfile(null, null);
    }

    private record OperatorDirectoryRow(
            String operatorId,
            String operatorName,
            String mobileNo,
            String officePhoneNo,
            String accountStatus,
            long versionNo,
            boolean locked,
            LocalDateTime passwordChangedAt,
            boolean passwordChangeRequired,
            String createdAt,
            String updatedAt) {
    }

    private static class OperatorState {
        private final String operatorId;
        private final String operatorName;
        private final String createdAt;
        private List<String> roleIds;
        private String passwordHash;
        private String accountStatus;
        private long versionNo;
        private boolean locked;
        private int failedLoginCount;
        private boolean passwordChangeRequired;
        private LocalDateTime passwordChangedAt;
        private String updatedAt;
        private final List<String> passwordHistoryHashes = new ArrayList<>();

        private OperatorState(String operatorId, String operatorName, String passwordHash,
                              String accountStatus, long versionNo, List<String> roleIds,
                              boolean locked, int failedLoginCount,
                              boolean passwordChangeRequired, LocalDateTime passwordChangedAt,
                              String createdAt, String updatedAt) {
            this.operatorId = operatorId;
            this.operatorName = operatorName;
            this.passwordHash = passwordHash;
            this.accountStatus = accountStatus;
            this.versionNo = versionNo;
            this.roleIds = roleIds;
            this.locked = locked;
            this.failedLoginCount = failedLoginCount;
            this.passwordChangeRequired = passwordChangeRequired;
            this.passwordChangedAt = passwordChangedAt;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

    }
}
