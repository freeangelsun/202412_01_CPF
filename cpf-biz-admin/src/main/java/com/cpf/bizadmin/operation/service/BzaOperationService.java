package com.cpf.bizadmin.operation.service;

import com.cpf.bizadmin.operation.repository.BzaOperationRepository;
import com.cpf.common.utils.MaskingUtils;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.security.password.CpfPasswordHashingPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * BZA 업무 운영 서비스입니다.
 *
 * <p>조회 데이터는 DB 저장소에서 읽고, 개인정보 원문 조회는 감사 사유를 필수로 남깁니다.
 * 업무 관리자 기능은 ADM 공통 운영자 기능과 분리하되, ADM 화면에서 함께 관제할 수 있도록 동일한 응답 구조와 감사 기준을 사용합니다.</p>
 */
@Service
public class BzaOperationService extends com.cpf.bizadmin.common.base.BzaBaseService {
    private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "ALL");

    private final BzaOperationRepository repository;
    private final CpfPasswordHashingPort passwordHashingPort;

    public BzaOperationService(
            BzaOperationRepository repository,
            CpfPasswordHashingPort passwordHashingPort) {
        this.repository = repository;
        this.passwordHashingPort = passwordHashingPort;
    }

    public List<Map<String, Object>> findAdminUsers() {
        return repository.findAdminUsers();
    }

    /** 사용자 저장 시 원문 비밀번호를 응답·감사 데이터에 포함하지 않습니다. */
    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveAdminUser(AdminUserRequest request, String operatorId) {
        String loginId = required(request.loginId(), "loginId");
        String requestUser = required(operatorId, "operatorId");
        Map<String, Object> before = repository.findAdminUser(loginId).orElse(null);
        String passwordHash = hashPassword(request.rawPassword(), before == null);

        Map<String, Object> values = new LinkedHashMap<>();
        values.put("loginId", loginId);
        values.put("adminName", required(request.adminName(), "adminName"));
        values.put("passwordHash", passwordHash);
        values.put("roleCode", code(request.roleCode(), "roleCode"));
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("lockYn", yn(request.lockYn(), "N"));
        values.put("passwordChangeRequiredYn", yn(request.passwordChangeRequiredYn(), before == null ? "Y" : "N"));
        values.put("requestUser", requestUser);
        repository.saveAdminUser(values);

        Map<String, Object> after = withoutSecret(values);
        audit(requestUser, "ADMIN_USER_SAVE", "bza_admin_user", loginId,
                required(request.reason(), "reason"), before, after);
        return after;
    }

    public List<Map<String, Object>> findMenus() {
        return repository.findMenus();
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveMenu(MenuRequest request, String operatorId) {
        String menuCode = code(request.menuCode(), "menuCode");
        String requestUser = required(operatorId, "operatorId");
        Map<String, Object> before = repository.findMenu(menuCode).orElse(null);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("menuCode", menuCode);
        values.put("menuName", required(request.menuName(), "menuName"));
        values.put("parentMenuCode", blankToNull(request.parentMenuCode()));
        values.put("moduleCode", code(defaultText(request.moduleCode(), "BZA"), "moduleCode"));
        values.put("routePath", blankToNull(request.routePath()));
        values.put("iconCode", blankToNull(request.iconCode()));
        values.put("environmentCode", code(defaultText(request.environmentCode(), "ALL"), "environmentCode"));
        values.put("apiPath", blankToNull(request.apiPath()));
        values.put("sortOrder", request.sortOrder() == null ? 0 : request.sortOrder());
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("requestUser", requestUser);
        repository.saveMenu(values);
        audit(requestUser, "MENU_SAVE", "bza_menu", menuCode,
                required(request.reason(), "reason"), before, values);
        return values;
    }

    public List<Map<String, Object>> findRoles() {
        return repository.findRoles();
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveRole(RoleRequest request, String operatorId) {
        String roleCode = code(request.roleCode(), "roleCode");
        String requestUser = required(operatorId, "operatorId");
        Map<String, Object> before = repository.findRole(roleCode).orElse(null);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("roleCode", roleCode);
        values.put("roleName", required(request.roleName(), "roleName"));
        values.put("writeAllowedYn", yn(request.writeAllowedYn(), "N"));
        values.put("dataScope", code(defaultText(request.dataScope(), "OWN"), "dataScope"));
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("requestUser", requestUser);
        repository.saveRole(values);
        audit(requestUser, "ROLE_SAVE", "bza_role", roleCode,
                required(request.reason(), "reason"), before, values);
        return values;
    }

    public List<Map<String, Object>> findPermissions() {
        return repository.findPermissions();
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> savePermission(PermissionRequest request, String operatorId) {
        String roleCode = code(request.roleCode(), "roleCode");
        String menuCode = code(request.menuCode(), "menuCode");
        String buttonCode = code(request.buttonCode(), "buttonCode");
        String requestUser = required(operatorId, "operatorId");
        Map<String, Object> before = repository.findPermission(roleCode, menuCode, buttonCode).orElse(null);
        String method = blankToNull(request.httpMethod());
        if (method != null) {
            method = method.toUpperCase(Locale.ROOT);
            if (!HTTP_METHODS.contains(method)) {
                throw new CpfValidationException("허용되지 않은 HTTP 메서드입니다. method=" + method);
            }
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("roleCode", roleCode);
        values.put("menuCode", menuCode);
        values.put("buttonCode", buttonCode);
        values.put("permissionType", code(defaultText(request.permissionType(), "BUTTON"), "permissionType"));
        values.put("httpMethod", method);
        values.put("apiPattern", blankToNull(request.apiPattern()));
        values.put("domainCode", blankToNull(request.domainCode()));
        values.put("environmentCode", code(defaultText(request.environmentCode(), "ALL"), "environmentCode"));
        values.put("dataScope", code(defaultText(request.dataScope(), "ROLE"), "dataScope"));
        values.put("allowYn", yn(request.allowYn(), "N"));
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("requestUser", requestUser);
        repository.savePermission(values);
        audit(requestUser, "PERMISSION_SAVE", "bza_permission",
                roleCode + ":" + menuCode + ":" + buttonCode,
                required(request.reason(), "reason"), before, values);
        return values;
    }

    /**
     * 고객 목록은 기본적으로 이메일과 휴대폰 번호를 마스킹해서 반환합니다.
     */
    public List<Map<String, Object>> findCustomers() {
        return repository.findCustomers().stream()
                .map(this::maskedCustomer)
                .toList();
    }

    /**
     * 고객 원문 조회는 사유와 요청자를 감사 로그에 남긴 뒤 반환합니다.
     */
    public List<Map<String, Object>> unmaskCustomers(String reason, String requestUser) {
        String resolvedReason = TextUtils.requireText(reason, "reason");
        String resolvedUser = TextUtils.defaultIfBlank(requestUser, "BZA_OPERATOR");
        return repository.findCustomers().stream()
                .map(row -> {
                    repository.insertMaskingAudit(String.valueOf(row.get("customerNo")), resolvedUser, resolvedReason, "SUCCESS");
                    Map<String, Object> result = new LinkedHashMap<>(row);
                    result.put("unmaskAuditReason", resolvedReason);
                    result.put("unmaskRequestUser", resolvedUser);
                    return result;
                })
                .toList();
    }

    public List<Map<String, Object>> findProducts() {
        return repository.findProducts();
    }

    public List<Map<String, Object>> findOrders() {
        return repository.findOrders();
    }

    public List<Map<String, Object>> findSettings() {
        return repository.findSettings();
    }

    public List<Map<String, Object>> findDownloadPolicies() {
        return repository.findDownloadPolicies();
    }

    private String hashPassword(String rawPassword, boolean requiredForCreate) {
        if (rawPassword == null || rawPassword.isBlank()) {
            if (requiredForCreate) {
                throw new CpfValidationException("신규 사용자의 rawPassword는 필수입니다.");
            }
            return null;
        }
        if (rawPassword.length() < 12) {
            throw new CpfValidationException("비밀번호는 12자 이상이어야 합니다.");
        }
        char[] password = rawPassword.toCharArray();
        try {
            return passwordHashingPort.hash(password);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void audit(
            String actor,
            String action,
            String targetType,
            String targetId,
            String reason,
            Object before,
            Object after) {
        Map<String, Object> audit = new LinkedHashMap<>();
        audit.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        audit.put("actorId", actor);
        audit.put("actionType", action);
        audit.put("targetType", targetType);
        audit.put("targetId", targetId);
        audit.put("reason", reason);
        audit.put("beforeData", before == null ? null : String.valueOf(before));
        audit.put("afterData", after == null ? null : String.valueOf(after));
        repository.insertBusinessAudit(audit);
    }

    private Map<String, Object> withoutSecret(Map<String, Object> values) {
        Map<String, Object> result = new LinkedHashMap<>(values);
        result.remove("passwordHash");
        return result;
    }

    private String required(String value, String field) {
        return TextUtils.requireText(value, field);
    }

    private String code(String value, String field) {
        return required(value, field).toUpperCase(Locale.ROOT);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String yn(String value, String fallback) {
        String resolved = defaultText(value, fallback).toUpperCase(Locale.ROOT);
        if (!Set.of("Y", "N").contains(resolved)) {
            throw new CpfValidationException("Y/N 값이 올바르지 않습니다. value=" + value);
        }
        return resolved;
    }

    private Map<String, Object> maskedCustomer(Map<String, Object> row) {
        Map<String, Object> masked = new LinkedHashMap<>(row);
        masked.put("email", MaskingUtils.maskEmail(String.valueOf(row.get("email"))));
        masked.put("mobileNo", MaskingUtils.maskMobile(String.valueOf(row.get("mobileNo"))));
        return masked;
    }

    public record AdminUserRequest(
            String loginId,
            String adminName,
            String roleCode,
            String rawPassword,
            String useYn,
            String lockYn,
            String passwordChangeRequiredYn,
            String requestUser,
            String reason) {
    }

    public record RoleRequest(
            String roleCode,
            String roleName,
            String writeAllowedYn,
            String dataScope,
            String useYn,
            String requestUser,
            String reason) {
    }

    public record MenuRequest(
            String menuCode,
            String menuName,
            String parentMenuCode,
            String moduleCode,
            String routePath,
            String iconCode,
            String environmentCode,
            String apiPath,
            Integer sortOrder,
            String useYn,
            String requestUser,
            String reason) {
    }

    public record PermissionRequest(
            String roleCode,
            String menuCode,
            String buttonCode,
            String permissionType,
            String httpMethod,
            String apiPattern,
            String domainCode,
            String environmentCode,
            String dataScope,
            String allowYn,
            String useYn,
            String requestUser,
            String reason) {
    }
}
