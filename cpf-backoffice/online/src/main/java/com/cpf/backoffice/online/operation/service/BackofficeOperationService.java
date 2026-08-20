package com.cpf.backoffice.online.operation.service;


import com.cpf.data.persistence.api.annotation.CpfTransactional;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.backoffice.online.audit.service.BackofficeBusinessAuditService;
import com.cpf.backoffice.online.auth.repository.BackofficeAuthRepository;
import com.cpf.backoffice.online.auth.model.BackofficeAdminAccountStatus;
import com.cpf.backoffice.online.operation.repository.BackofficeOperationRepository;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.foundation.api.page.CpfPage;
import com.cpf.foundation.api.page.CpfPageRequest;
import com.cpf.security.api.password.CpfPasswordEncoder;
import com.cpf.foundation.util.CpfStrings;
import java.util.*;

/** MBW 사용자/메뉴/Role/Permission 운영 서비스. */
@CpfService
public class BackofficeOperationService extends com.cpf.backoffice.online.base.BackofficeBaseService {
  private static final Set<String> HTTP_METHODS =
      Set.of("GET", "POST", "PUT", "PATCH", "DELETE", "ALL");
  private final BackofficeOperationRepository repository;
  private final CpfPasswordEncoder passwordHashingPort;
  private final BackofficeBusinessAuditService auditService;
  private final BackofficeAuthRepository authRepository;

  /** BackofficeOperationService 작업을 CPF 표준 계약에 따라 수행한다. */
  public BackofficeOperationService(
      BackofficeOperationRepository repository,
      CpfPasswordEncoder passwordHashingPort,
      BackofficeBusinessAuditService auditService,
      BackofficeAuthRepository authRepository) {
    this.repository = repository;
    this.passwordHashingPort = passwordHashingPort;
    this.auditService = auditService;
    this.authRepository = authRepository;
  }

  /** findAdminUsers 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findAdminUsers() {
    return repository.findAdminUsers();
  }

  public CpfPage<Map<String, Object>> findAdminUsersPage(Integer page, Integer size) {
    return repository.adminUserPage(CpfPageRequest.of(page, size));
  }

  /** findMenus 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findMenus() {
    return repository.findMenus();
  }

  public CpfPage<Map<String, Object>> findMenusPage(Integer page, Integer size) {
    return repository.menuPage(CpfPageRequest.of(page, size));
  }

  /** findMenuImpact 작업을 CPF 표준 계약에 따라 수행한다. */
  public MenuImpact findMenuImpact(String menuCode) {
    String key = code(menuCode, "menuCode");
    Map<String, Object> current =
        repository.findMenu(key).orElseThrow(() -> new CpfValidationException("메뉴를 찾을 수 없습니다."));
    Set<String> descendants = descendants(repository.findMenuHierarchy(), key);
    long permissions = repository.countMenuPermissions(key);
    return new MenuImpact(
        key,
        descendants.size(),
        permissions,
        String.valueOf(current.getOrDefault("routePath", "")),
        descendants.isEmpty() && permissions == 0);
  }

  /** findRoles 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findRoles() {
    return repository.findRoles();
  }

  public CpfPage<Map<String, Object>> findRolesPage(Integer page, Integer size) {
    return repository.rolePage(CpfPageRequest.of(page, size));
  }

  /** findPermissions 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findPermissions() {
    return repository.findPermissions();
  }

  public CpfPage<Map<String, Object>> findPermissionsPage(Integer page, Integer size) {
    return repository.permissionPage(CpfPageRequest.of(page, size));
  }

  /** findSettings 작업을 CPF 표준 계약에 따라 수행한다. */
  public List<Map<String, Object>> findSettings() {
    return repository.findSettings();
  }

  public List<Map<String, Object>> findDownloadPolicies() {
    return repository.findDownloadPolicies();
  }

  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  /** saveAdminUser 작업을 CPF 표준 계약에 따라 수행한다. */
  public Map<String, Object> saveAdminUser(AdminUserRequest r, String operatorId) {
    String login = required(r.loginId(), "loginId"), actor = required(operatorId, "operatorId");
    Map<String, Object> before = repository.findAdminUser(login).orElse(null);
    boolean create = before == null;
    if (create && r.roleCode() != null && !r.roleCode().isBlank()) {
      throw new CpfValidationException(
          "신규 관리자는 Role을 자동 부여하지 않습니다. 생성 후 사용자 Role 이력에서 명시적으로 부여하십시오.");
    }
    if (!create && r.expectedVersion() == null)
      throw new CpfValidationException("관리자 수정에는 expectedVersion이 필요합니다.");
    String accountStatus =
        BackofficeAdminAccountStatus.parse(
                create
                    ? "PENDING_ACTIVATION"
                    : defaultText(r.accountStatus(), String.valueOf(before.get("accountStatus"))))
            .name();
    if (!create)
      validateStatusTransition(String.valueOf(before.get("accountStatus")), accountStatus);
    if ("ACTIVE".equals(accountStatus) && repository.countEffectiveRoles(login) == 0)
      throw new CpfValidationException("Role이 없는 관리자는 ACTIVE로 전환할 수 없습니다.");
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("loginId", login);
    v.put("adminName", required(r.adminName(), "adminName"));
    v.put("passwordHash", hashPassword(r.rawPassword(), create));
    v.put("accountStatus", accountStatus);
    String useYn = "DISABLED".equals(accountStatus) ? "N" : yn(r.useYn(), "Y");
    v.put("useYn", useYn);
    v.put("lockYn", "LOCKED".equals(accountStatus) ? "Y" : "N");
    v.put("passwordChangeRequiredYn", yn(r.passwordChangeRequiredYn(), create ? "Y" : "N"));
    v.put("expectedVersion", r.expectedVersion());
    v.put("requestUser", actor);
    int changed = create ? repository.insertAdminUser(v) : repository.updateAdminUser(v);
    if (changed != 1) throw new CpfValidationException("관리자 정보가 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
    if (!create) authRepository.revokeAllRefreshTokensByLoginId(login);
    audit(
        actor,
        "ADMIN_USER_SAVE",
        "mbw_admin_user",
        login,
        required(r.reason(), "reason"),
        before,
        withoutSecret(v));
    return withoutSecret(v);
  }

  // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  public Map<String, Object> saveMenu(MenuRequest r, String operatorId) {
    String key = code(r.menuCode(), "menuCode"), actor = required(operatorId, "operatorId");
    Map<String, Object> before = repository.findMenu(key).orElse(null);
    String parentMenuCode = blank(r.parentMenuCode());
    validateMenuHierarchy(repository.findMenuHierarchy(), key, parentMenuCode);
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("menuCode", key);
    v.put("menuName", required(r.menuName(), "menuName"));
    v.put("parentMenuCode", parentMenuCode);
    v.put("moduleCode", code(defaultText(r.moduleCode(), "MBW"), "moduleCode"));
    v.put("routePath", blank(r.routePath()));
    v.put("iconCode", blank(r.iconCode()));
    v.put("environmentCode", code(defaultText(r.environmentCode(), "ALL"), "environmentCode"));
    v.put("apiPath", blank(r.apiPath()));
    v.put("sortOrder", r.sortOrder() == null ? 0 : r.sortOrder());
    v.put("useYn", yn(r.useYn(), "Y"));
    v.put("requestUser", actor);
    v.put("expectedVersion", r.expectedVersion());
    int changed =
        before == null
            ? repository.insertMenu(v)
            : repository.updateMenu(requireVersion(v, r.expectedVersion(), "menu"));
    if (changed != 1) throw new CpfValidationException("메뉴가 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
    audit(actor, "MENU_SAVE", "mbw_menu", key, required(r.reason(), "reason"), before, v);
    return v;
  }

  // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  public MenuDeleteResult deleteMenu(String menuCode, MenuDeleteRequest r, String operatorId) {
    String key = code(menuCode, "menuCode");
    String actor = required(operatorId, "operatorId");
    if (r == null || r.expectedVersion() == null) {
      throw new CpfValidationException("메뉴 삭제에는 expectedVersion이 필요합니다.");
    }
    String reason = required(r.reason(), "reason");
    Map<String, Object> before =
        repository.findMenu(key).orElseThrow(() -> new CpfValidationException("메뉴를 찾을 수 없습니다."));
    MenuImpact impact = findMenuImpact(key);
    if (impact.descendantCount() > 0) {
      throw new CpfValidationException("하위 메뉴를 먼저 이동하거나 비활성화해야 합니다.");
    }
    if (impact.permissionCount() > 0) {
      throw new CpfValidationException("활성 Permission이 연결된 메뉴는 삭제할 수 없습니다.");
    }
    int changed = repository.deleteMenu(key, r.expectedVersion());
    if (changed != 1) {
      throw new CpfValidationException("메뉴가 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
    }
    audit(actor, "MENU_DELETE", "mbw_menu", key, reason, before, Map.of("deleted", true));
    return new MenuDeleteResult(key, true, r.expectedVersion(), actor);
  }

  private void validateMenuHierarchy(
      List<Map<String, Object>> hierarchy, String menuCode, String parentMenuCode) {
    if (parentMenuCode == null) return;
    if (menuCode.equals(parentMenuCode)) {
      throw new CpfValidationException("메뉴 자신을 상위 메뉴로 지정할 수 없습니다.");
    }
    Map<String, String> parents = new HashMap<>();
    boolean parentExists = false;
    for (Map<String, Object> row : hierarchy) {
      String code = stringValue(row, "menuCode", "MENU_CODE");
      String parent = stringValue(row, "parentMenuCode", "PARENT_MENU_CODE");
      if (code != null) {
        parents.put(code, parent);
        if (code.equals(parentMenuCode)) parentExists = true;
      }
    }
    if (!parentExists) {
      throw new CpfValidationException("상위 메뉴가 존재하지 않습니다: " + parentMenuCode);
    }
    String cursor = parentMenuCode;
    Set<String> visited = new HashSet<>();
    while (cursor != null && visited.add(cursor)) {
      if (menuCode.equals(cursor)) {
        throw new CpfValidationException("상위 메뉴 이동으로 순환 구조가 생성됩니다.");
      }
      cursor = parents.get(cursor);
    }
    if (cursor != null) {
      throw new CpfValidationException("기존 메뉴 hierarchy에 순환 구조가 존재합니다.");
    }
  }

  private Set<String> descendants(List<Map<String, Object>> hierarchy, String menuCode) {
    Map<String, List<String>> children = new HashMap<>();
    for (Map<String, Object> row : hierarchy) {
      String code = stringValue(row, "menuCode", "MENU_CODE");
      String parent = stringValue(row, "parentMenuCode", "PARENT_MENU_CODE");
      if (code != null && parent != null) {
        children.computeIfAbsent(parent, ignored -> new ArrayList<>()).add(code);
      }
    }
    Set<String> result = new LinkedHashSet<>();
    Deque<String> queue = new ArrayDeque<>(children.getOrDefault(menuCode, List.of()));
    while (!queue.isEmpty()) {
      String current = queue.removeFirst();
      if (!result.add(current)) {
        throw new CpfValidationException("메뉴 hierarchy에 순환 구조가 존재합니다.");
      }
      queue.addAll(children.getOrDefault(current, List.of()));
    }
    return result;
  }

  private String stringValue(Map<String, Object> row, String camel, String snakeUpper) {
    Object value = row.containsKey(camel) ? row.get(camel) : row.get(snakeUpper);
    if (value == null) return null;
    String text = String.valueOf(value).trim();
    return text.isEmpty() ? null : text.toUpperCase(Locale.ROOT);
  }

  // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  public Map<String, Object> saveRole(RoleRequest r, String operatorId) {
    String key = code(r.roleCode(), "roleCode"), actor = required(operatorId, "operatorId");
    Map<String, Object> before = repository.findRole(key).orElse(null);
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("roleCode", key);
    v.put("roleName", required(r.roleName(), "roleName"));
    v.put("writeAllowedYn", yn(r.writeAllowedYn(), "N"));
    v.put("dataScope", code(defaultText(r.dataScope(), "OWN"), "dataScope"));
    v.put("useYn", yn(r.useYn(), "Y"));
    v.put("requestUser", actor);
    v.put("expectedVersion", r.expectedVersion());
    int changed =
        before == null
            ? repository.insertRole(v)
            : repository.updateRole(requireVersion(v, r.expectedVersion(), "role"));
    if (changed != 1) throw new CpfValidationException("Role이 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
    authRepository.revokeRefreshTokensByRoleCode(key);
    audit(actor, "ROLE_SAVE", "mbw_role", key, required(r.reason(), "reason"), before, v);
    return v;
  }

  // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
  @CpfTransactional(transactionManager="MBW_TRANSACTION_MANAGER")
  public Map<String, Object> savePermission(PermissionRequest r, String operatorId) {
    String actor = required(operatorId, "operatorId"), method = blank(r.httpMethod());
    if (method != null) {
      method = method.toUpperCase(Locale.ROOT);
      if (!HTTP_METHODS.contains(method)) throw new CpfValidationException("허용되지 않은 HTTP 메서드입니다.");
    }
    Map<String, Object> before = repository.findPermission(r.permissionId()).orElse(null);
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("permissionId", r.permissionId());
    v.put("roleCode", code(r.roleCode(), "roleCode"));
    v.put("menuCode", code(r.menuCode(), "menuCode"));
    v.put("buttonCode", code(r.buttonCode(), "buttonCode"));
    v.put("permissionType", code(defaultText(r.permissionType(), "BUTTON"), "permissionType"));
    v.put("httpMethod", method);
    v.put("apiPattern", blank(r.apiPattern()));
    v.put("domainCode", blank(r.domainCode()));
    v.put("environmentCode", code(defaultText(r.environmentCode(), "ALL"), "environmentCode"));
    v.put("dataScope", code(defaultText(r.dataScope(), "ROLE"), "dataScope"));
    v.put("allowYn", yn(r.allowYn(), "N"));
    v.put("useYn", yn(r.useYn(), "Y"));
    v.put("requestUser", actor);
    v.put("expectedVersion", r.expectedVersion());
    int changed =
        before == null
            ? repository.insertPermission(v)
            : repository.updatePermission(requireVersion(v, r.expectedVersion(), "permission"));
    if (changed != 1)
      throw new CpfValidationException("Permission이 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
    authRepository.revokeRefreshTokensByRoleCode(String.valueOf(v.get("roleCode")));
    audit(
        actor,
        "PERMISSION_SAVE",
        "mbw_permission",
        String.valueOf(r.permissionId()),
        required(r.reason(), "reason"),
        before,
        v);
    return v;
  }

  private void validateStatusTransition(String currentValue, String nextValue) {
    BackofficeAdminAccountStatus current = BackofficeAdminAccountStatus.parse(currentValue),
        next = BackofficeAdminAccountStatus.parse(nextValue);
    if (current == next) return;
    Map<BackofficeAdminAccountStatus, Set<BackofficeAdminAccountStatus>> allowed =
        Map.of(
            BackofficeAdminAccountStatus.PENDING_ACTIVATION,
                Set.of(BackofficeAdminAccountStatus.ACTIVE, BackofficeAdminAccountStatus.DISABLED),
            BackofficeAdminAccountStatus.ACTIVE,
                Set.of(
                    BackofficeAdminAccountStatus.LOCKED,
                    BackofficeAdminAccountStatus.SUSPENDED,
                    BackofficeAdminAccountStatus.DISABLED),
            BackofficeAdminAccountStatus.LOCKED,
                Set.of(
                    BackofficeAdminAccountStatus.ACTIVE,
                    BackofficeAdminAccountStatus.SUSPENDED,
                    BackofficeAdminAccountStatus.DISABLED),
            BackofficeAdminAccountStatus.SUSPENDED,
                Set.of(BackofficeAdminAccountStatus.ACTIVE, BackofficeAdminAccountStatus.DISABLED),
            BackofficeAdminAccountStatus.DISABLED, Set.of(BackofficeAdminAccountStatus.PENDING_ACTIVATION));
    if (!allowed.getOrDefault(current, Set.of()).contains(next))
      throw new CpfValidationException("허용되지 않은 관리자 상태 전이입니다: " + current + " -> " + next);
  }

  private Map<String, Object> requireVersion(Map<String, Object> v, Long version, String name) {
    if (version == null) throw new CpfValidationException(name + " 수정에는 expectedVersion이 필요합니다.");
    return v;
  }

  private String hashPassword(String raw, boolean create) {
    if (raw == null || raw.isBlank()) {
      if (create) throw new CpfValidationException("신규 사용자의 rawPassword는 필수입니다.");
      return null;
    }
    if (raw.length() < 12) throw new CpfValidationException("비밀번호는 12자 이상이어야 합니다.");
    char[] c = raw.toCharArray();
    try {
      return passwordHashingPort.hash(c);
    } finally {
      Arrays.fill(c, '\0');
    }
  }

  private void audit(
      String actor,
      String action,
      String type,
      String id,
      String reason,
      Object before,
      Object after) {
    auditService.record(actor, action, type, id, reason, before, after);
  }

  private Map<String, Object> withoutSecret(Map<String, Object> v) {
    Map<String, Object> r = new LinkedHashMap<>(v);
    r.remove("passwordHash");
    return r;
  }

  private String required(String v, String f) {
    return CpfStrings.requireText(v, f);
  }

  private String code(String v, String f) {
    return required(v, f).toUpperCase(Locale.ROOT);
  }

  private String defaultText(String v, String d) {
    return v == null || v.isBlank() ? d : v.trim();
  }

  private String blank(String v) {
    return v == null || v.isBlank() ? null : v.trim();
  }

  private String yn(String v, String d) {
    String x = defaultText(v, d).toUpperCase(Locale.ROOT);
    if (!Set.of("Y", "N").contains(x)) throw new CpfValidationException("Y/N 값이 올바르지 않습니다.");
    return x;
  }

  /** AdminUserRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
  public record AdminUserRequest(
      String loginId,
      String adminName,
      String roleCode,
      String rawPassword,
      String accountStatus,
      String useYn,
      String lockYn,
      String passwordChangeRequiredYn,
      Long expectedVersion,
      String requestUser,
      String reason) {
    /** AdminUserRequest 작업을 CPF 표준 계약에 따라 수행한다. */
    public AdminUserRequest(
        String loginId,
        String adminName,
        String roleCode,
        String rawPassword,
        String useYn,
        String lockYn,
        String passwordChangeRequiredYn,
        String requestUser,
        String reason) {
      this(
          loginId,
          adminName,
          roleCode,
          rawPassword,
          null,
          useYn,
          lockYn,
          passwordChangeRequiredYn,
          null,
          requestUser,
          reason);
    }
  }

  /** MenuImpact 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
  public record MenuImpact(
      String menuCode,
      int descendantCount,
      long permissionCount,
      String routePath,
      boolean deletable) {}

  public record MenuDeleteRequest(Long expectedVersion, String reason, String operationId) {}

  /** MenuDeleteResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
  public record MenuDeleteResult(
      String menuCode, boolean deleted, long deletedVersion, String operatorId) {}

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
      Long expectedVersion,
      String requestUser,
      String reason) {}

  /** RoleRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
  public record RoleRequest(
      String roleCode,
      String roleName,
      String writeAllowedYn,
      String dataScope,
      String useYn,
      Long expectedVersion,
      String requestUser,
      String reason) {}

  /** PermissionRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
  public record PermissionRequest(
      Long permissionId,
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
      Long expectedVersion,
      String requestUser,
      String reason) {}
}
