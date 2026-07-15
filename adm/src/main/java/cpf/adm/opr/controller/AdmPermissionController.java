package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmApiPermission;
import cpf.adm.opr.dto.AdmApiPermissionRoleUpdateRequest;
import cpf.adm.opr.dto.AdmApiPermissionSaveRequest;
import cpf.adm.opr.dto.AdmButton;
import cpf.adm.opr.dto.AdmButtonPermissionUpdateRequest;
import cpf.adm.opr.dto.AdmButtonSaveRequest;
import cpf.adm.opr.dto.AdmMenuManagement;
import cpf.adm.opr.dto.AdmMenuPermissionUpdateRequest;
import cpf.adm.opr.dto.AdmMenuSaveRequest;
import cpf.adm.opr.dto.AdmRoleManagement;
import cpf.adm.opr.dto.AdmRoleSaveRequest;
import cpf.adm.opr.dto.AdmStatusUpdateRequest;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmPermissionService;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/permissions")
@Tag(name = "ADM-OPR Permissions", description = "ADM 메뉴/버튼 권한 관리 API")
public class AdmPermissionController {
    private final AdmPermissionService permissionService;
    private final AdmAuditLogService auditLogService;

    public AdmPermissionController(AdmPermissionService permissionService, AdmAuditLogService auditLogService) {
        this.permissionService = permissionService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/roles")
    @CpfOnlineTransaction(id = "OADM-PER-01-0014", name = "ADMRoleManagementList")
    @Operation(operationId = "admPermissionFindRoles", summary = "역할 관리 목록 조회", description = "ADM 역할의 유형, 사용 여부, 등록/수정 시각을 조회합니다.")
    public ResponseEntity<List<AdmRoleManagement>> findRoles() {
        return ResponseEntity.ok(permissionService.findRoles());
    }

    @GetMapping("/roles/{roleId}")
    @CpfOnlineTransaction(id = "OADM-PER-01-0015", name = "ADMRoleManagementDetail")
    @Operation(operationId = "admPermissionFindRole", summary = "역할 상세 조회", description = "ADM 역할 상세 정보를 조회합니다.")
    public ResponseEntity<AdmRoleManagement> findRole(@PathVariable String roleId) {
        return ResponseEntity.ok(permissionService.findRole(roleId));
    }

    @PostMapping("/roles")
    @CpfOnlineTransaction(id = "OADM-PER-02-0016", name = "ADMRoleCreate")
    @Operation(operationId = "admPermissionCreateRole", summary = "역할 등록", description = "ADM 역할을 등록하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmRoleManagement> createRole(
            @RequestBody AdmRoleSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmRoleManagement role = permissionService.createRole(request);
        recordChange(servletRequest, request.requestUser(), "ROLE_CREATE", "adm_role",
                role.roleId(), reason, null, role, "역할 등록");
        return ResponseEntity.ok(role);
    }

    @PutMapping("/roles/{roleId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0017", name = "ADMRoleUpdate")
    @Operation(operationId = "admPermissionUpdateRole", summary = "역할 수정", description = "ADM 역할을 수정하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmRoleManagement> updateRole(
            @PathVariable String roleId,
            @RequestBody AdmRoleSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmRoleManagement before = permissionService.findRole(roleId);
        AdmRoleManagement after = permissionService.updateRole(roleId, request);
        recordChange(servletRequest, request.requestUser(), "ROLE_UPDATE", "adm_role",
                roleId, reason, before, after, "역할 수정");
        return ResponseEntity.ok(after);
    }

    @PutMapping("/roles/{roleId}/status")
    @CpfOnlineTransaction(id = "OADM-PER-03-0018", name = "ADMRoleStatusUpdate")
    @Operation(operationId = "admPermissionUpdateRoleStatus", summary = "역할 사용 여부 변경", description = "ADM 역할 사용/중지 상태를 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmRoleManagement> updateRoleStatus(
            @PathVariable String roleId,
            @RequestBody AdmStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmRoleManagement before = permissionService.findRole(roleId);
        AdmRoleManagement after = permissionService.updateRoleStatus(roleId, request);
        recordChange(servletRequest, request.requestUser(), "ROLE_STATUS_UPDATE", "adm_role",
                roleId, reason, before, after, "역할 사용 여부 변경");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/menus")
    @CpfOnlineTransaction(id = "OADM-PER-01-0019", name = "ADMMenuManagementList")
    @Operation(operationId = "admPermissionFindManagedMenus", summary = "메뉴 관리 목록 조회", description = "ADM 메뉴 계층과 사용 여부를 조회합니다.")
    public ResponseEntity<List<AdmMenuManagement>> findManagedMenus() {
        return ResponseEntity.ok(permissionService.findManagedMenus());
    }

    @GetMapping("/menus/{menuId}")
    @CpfOnlineTransaction(id = "OADM-PER-01-0020", name = "ADMMenuManagementDetail")
    @Operation(operationId = "admPermissionFindManagedMenu", summary = "메뉴 상세 조회", description = "ADM 메뉴 상세 정보를 조회합니다.")
    public ResponseEntity<AdmMenuManagement> findManagedMenu(@PathVariable String menuId) {
        return ResponseEntity.ok(permissionService.findManagedMenu(menuId));
    }

    @PostMapping("/menus")
    @CpfOnlineTransaction(id = "OADM-PER-02-0021", name = "ADMMenuCreate")
    @Operation(operationId = "admPermissionCreateMenu", summary = "메뉴 등록", description = "ADM 메뉴를 등록하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmMenuManagement> createMenu(
            @RequestBody AdmMenuSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmMenuManagement menu = permissionService.createMenu(request);
        recordChange(servletRequest, request.requestUser(), "MENU_CREATE", "adm_menu",
                menu.menuId(), reason, null, menu, "메뉴 등록");
        return ResponseEntity.ok(menu);
    }

    @PutMapping("/menus/{menuId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0022", name = "ADMMenuUpdate")
    @Operation(operationId = "admPermissionUpdateMenu", summary = "메뉴 수정", description = "ADM 메뉴를 수정하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmMenuManagement> updateMenu(
            @PathVariable String menuId,
            @RequestBody AdmMenuSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmMenuManagement before = permissionService.findManagedMenu(menuId);
        AdmMenuManagement after = permissionService.updateMenu(menuId, request);
        recordChange(servletRequest, request.requestUser(), "MENU_UPDATE", "adm_menu",
                menuId, reason, before, after, "메뉴 수정");
        return ResponseEntity.ok(after);
    }

    @PutMapping("/menus/{menuId}/status")
    @CpfOnlineTransaction(id = "OADM-PER-03-0023", name = "ADMMenuStatusUpdate")
    @Operation(operationId = "admPermissionUpdateMenuStatus", summary = "메뉴 사용 여부 변경", description = "ADM 메뉴 사용/중지 상태를 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmMenuManagement> updateMenuStatus(
            @PathVariable String menuId,
            @RequestBody AdmStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmMenuManagement before = permissionService.findManagedMenu(menuId);
        AdmMenuManagement after = permissionService.updateMenuStatus(menuId, request);
        recordChange(servletRequest, request.requestUser(), "MENU_STATUS_UPDATE", "adm_menu",
                menuId, reason, before, after, "메뉴 사용 여부 변경");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/buttons")
    @CpfOnlineTransaction(id = "OADM-PER-01-0024", name = "ADMButtonManagementList")
    @Operation(operationId = "admPermissionFindButtons", summary = "버튼 관리 목록 조회", description = "ADM 메뉴별 버튼/행위와 연결 API 패턴을 조회합니다.")
    public ResponseEntity<List<AdmButton>> findButtons(@RequestParam(required = false) String menuId) {
        return ResponseEntity.ok(permissionService.findButtons(menuId));
    }

    @GetMapping("/buttons/{buttonId}")
    @CpfOnlineTransaction(id = "OADM-PER-01-0025", name = "ADMButtonManagementDetail")
    @Operation(operationId = "admPermissionFindButton", summary = "버튼 상세 조회", description = "ADM 버튼/행위 상세 정보를 조회합니다.")
    public ResponseEntity<AdmButton> findButton(@PathVariable String buttonId) {
        return ResponseEntity.ok(permissionService.findButton(buttonId));
    }

    @PostMapping("/buttons")
    @CpfOnlineTransaction(id = "OADM-PER-02-0026", name = "ADMButtonCreate")
    @Operation(operationId = "admPermissionCreateButton", summary = "버튼 등록", description = "ADM 버튼/행위를 등록하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmButton> createButton(
            @RequestBody AdmButtonSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmButton button = permissionService.createButton(request);
        recordChange(servletRequest, request.requestUser(), "BUTTON_CREATE", "adm_button",
                button.buttonId(), reason, null, button, "버튼 등록");
        return ResponseEntity.ok(button);
    }

    @PutMapping("/buttons/{buttonId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0027", name = "ADMButtonUpdate")
    @Operation(operationId = "admPermissionUpdateButton", summary = "버튼 수정", description = "ADM 버튼/행위를 수정하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmButton> updateButton(
            @PathVariable String buttonId,
            @RequestBody AdmButtonSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmButton before = permissionService.findButton(buttonId);
        AdmButton after = permissionService.updateButton(buttonId, request);
        recordChange(servletRequest, request.requestUser(), "BUTTON_UPDATE", "adm_button",
                buttonId, reason, before, after, "버튼 수정");
        return ResponseEntity.ok(after);
    }

    @PutMapping("/buttons/{buttonId}/status")
    @CpfOnlineTransaction(id = "OADM-PER-03-0028", name = "ADMButtonStatusUpdate")
    @Operation(operationId = "admPermissionUpdateButtonStatus", summary = "버튼 사용 여부 변경", description = "ADM 버튼/행위 사용/중지 상태를 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmButton> updateButtonStatus(
            @PathVariable String buttonId,
            @RequestBody AdmStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmButton before = permissionService.findButton(buttonId);
        AdmButton after = permissionService.updateButtonStatus(buttonId, request);
        recordChange(servletRequest, request.requestUser(), "BUTTON_STATUS_UPDATE", "adm_button",
                buttonId, reason, before, after, "버튼 사용 여부 변경");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/api-permissions")
    @CpfOnlineTransaction(id = "OADM-PER-01-0029", name = "ADMApiPermissionList")
    @Operation(operationId = "admPermissionFindApiPermissions", summary = "API 권한 목록 조회", description = "ADM API 권한과 실제 API 경로 패턴을 조회합니다.")
    public ResponseEntity<List<AdmApiPermission>> findApiPermissions() {
        return ResponseEntity.ok(permissionService.findApiPermissions());
    }

    @GetMapping("/api-permissions/{apiPermissionId}")
    @CpfOnlineTransaction(id = "OADM-PER-01-0030", name = "ADMApiPermissionDetail")
    @Operation(operationId = "admPermissionFindApiPermission", summary = "API 권한 상세 조회", description = "ADM API 권한 상세 정보를 조회합니다.")
    public ResponseEntity<AdmApiPermission> findApiPermission(@PathVariable String apiPermissionId) {
        return ResponseEntity.ok(permissionService.findApiPermission(apiPermissionId));
    }

    @PostMapping("/api-permissions")
    @CpfOnlineTransaction(id = "OADM-PER-02-0031", name = "ADMApiPermissionCreate")
    @Operation(operationId = "admPermissionCreateApiPermission", summary = "API 권한 등록", description = "ADM API 권한을 등록하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmApiPermission> createApiPermission(
            @RequestBody AdmApiPermissionSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmApiPermission permission = permissionService.createApiPermission(request);
        recordChange(servletRequest, request.requestUser(), "API_PERMISSION_CREATE", "adm_api_permission",
                permission.apiPermissionId(), reason, null, permission, "API 권한 등록");
        return ResponseEntity.ok(permission);
    }

    @PutMapping("/api-permissions/{apiPermissionId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0032", name = "ADMApiPermissionUpdate")
    @Operation(operationId = "admPermissionUpdateApiPermission", summary = "API 권한 수정", description = "ADM API 권한을 수정하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmApiPermission> updateApiPermission(
            @PathVariable String apiPermissionId,
            @RequestBody AdmApiPermissionSaveRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmApiPermission before = permissionService.findApiPermission(apiPermissionId);
        AdmApiPermission after = permissionService.updateApiPermission(apiPermissionId, request);
        recordChange(servletRequest, request.requestUser(), "API_PERMISSION_UPDATE", "adm_api_permission",
                apiPermissionId, reason, before, after, "API 권한 수정");
        return ResponseEntity.ok(after);
    }

    @PutMapping("/api-permissions/{apiPermissionId}/status")
    @CpfOnlineTransaction(id = "OADM-PER-03-0033", name = "ADMApiPermissionStatusUpdate")
    @Operation(operationId = "admPermissionUpdateApiPermissionStatus", summary = "API 권한 사용 여부 변경", description = "ADM API 권한 사용/중지 상태를 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<AdmApiPermission> updateApiPermissionStatus(
            @PathVariable String apiPermissionId,
            @RequestBody AdmStatusUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        AdmApiPermission before = permissionService.findApiPermission(apiPermissionId);
        AdmApiPermission after = permissionService.updateApiPermissionStatus(apiPermissionId, request);
        recordChange(servletRequest, request.requestUser(), "API_PERMISSION_STATUS_UPDATE", "adm_api_permission",
                apiPermissionId, reason, before, after, "API 권한 사용 여부 변경");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/api-matrix")
    @CpfOnlineTransaction(id = "OADM-PER-01-0034", name = "ADMApiPermissionMatrix")
    @Operation(operationId = "admPermissionFindApiPermissionMatrix", summary = "API 권한 매트릭스 조회", description = "역할별 ADM API 권한 허용 여부를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findApiPermissionMatrix() {
        return ResponseEntity.ok(permissionService.findApiPermissionMatrix());
    }

    @PutMapping("/roles/{roleId}/api-permissions/{apiPermissionId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0035", name = "ADMRoleApiPermissionUpdate")
    @Operation(operationId = "admPermissionUpdateRoleApiPermission", summary = "역할별 API 권한 변경", description = "역할별 API 권한 허용 여부를 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> updateRoleApiPermission(
            @PathVariable String roleId,
            @PathVariable String apiPermissionId,
            @RequestBody AdmApiPermissionRoleUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> before = permissionService.findRoleApiPermission(roleId, apiPermissionId);
        Map<String, Object> after = permissionService.updateRoleApiPermission(
                roleId, apiPermissionId, request.allowYn(), requestUser(servletRequest, request.requestUser()));
        recordChange(servletRequest, request.requestUser(), "ROLE_API_PERMISSION_UPDATE", "adm_role_api_permission",
                roleId + ":" + apiPermissionId, reason, before, after, "역할별 API 권한 변경");
        return ResponseEntity.ok(after);
    }

    @GetMapping("/menu-matrix")
    @CpfOnlineTransaction(id = "OADM-PER-01-0010", name = "ADMMenuPermissionMatrix")
    @Operation(operationId = "admPermissionFindMenuMatrix", summary = "메뉴 권한 매트릭스 조회", description = "역할별 ADM 메뉴 조회/쓰기/삭제 권한을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMenuMatrix() {
        return ResponseEntity.ok(permissionService.findMenuPermissions());
    }

    @GetMapping("/button-matrix")
    @CpfOnlineTransaction(id = "OADM-PER-01-0011", name = "ADMButtonPermissionMatrix")
    @Operation(operationId = "admPermissionFindButtonMatrix", summary = "버튼 권한 매트릭스 조회", description = "역할별 ADM 버튼/행위 권한을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findButtonMatrix() {
        return ResponseEntity.ok(permissionService.findButtonPermissions());
    }

    @PutMapping("/roles/{roleId}/menus/{menuId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0012", name = "ADMMenuPermissionUpdate")
    @Operation(operationId = "admPermissionUpdateMenuPermission", summary = "메뉴 권한 변경", description = "역할별 메뉴 권한을 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> updateMenuPermission(
            @PathVariable String roleId,
            @PathVariable String menuId,
            @RequestBody AdmMenuPermissionUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> before = permissionService.findMenuPermission(roleId, menuId);
        Map<String, Object> after = permissionService.updateMenuPermission(
                roleId, menuId, request.readYn(), request.writeYn(), request.deleteYn(), requestUser(servletRequest, request.requestUser()));
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.requestUser()),
                "MENU_PERMISSION_UPDATE",
                "adm_role_menu",
                roleId + ":" + menuId,
                reason,
                String.valueOf(before),
                String.valueOf(after),
                "메뉴 권한 변경",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(after);
    }

    @PutMapping("/roles/{roleId}/buttons/{buttonId}")
    @CpfOnlineTransaction(id = "OADM-PER-03-0013", name = "ADMButtonPermissionUpdate")
    @Operation(operationId = "admPermissionUpdateButtonPermission", summary = "버튼 권한 변경", description = "역할별 버튼/행위 권한을 변경하고 감사 로그를 남깁니다.")
    public ResponseEntity<Map<String, Object>> updateButtonPermission(
            @PathVariable String roleId,
            @PathVariable String buttonId,
            @RequestBody AdmButtonPermissionUpdateRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.reason());
        Map<String, Object> before = permissionService.findButtonPermission(roleId, buttonId);
        Map<String, Object> after = permissionService.updateButtonPermission(
                roleId, buttonId, request.allowYn(), requestUser(servletRequest, request.requestUser()));
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.requestUser()),
                "BUTTON_PERMISSION_UPDATE",
                "adm_role_button",
                roleId + ":" + buttonId,
                reason,
                String.valueOf(before),
                String.valueOf(after),
                "버튼 권한 변경",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(after);
    }

    private void recordChange(
            HttpServletRequest request,
            String fallbackUser,
            String actionType,
            String targetType,
            String targetId,
            String reason,
            Object before,
            Object after,
            String diff) {
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(request, fallbackUser),
                actionType,
                targetType,
                targetId,
                reason,
                before == null ? null : String.valueOf(before),
                after == null ? null : String.valueOf(after),
                diff,
                request.getRemoteAddr());
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
