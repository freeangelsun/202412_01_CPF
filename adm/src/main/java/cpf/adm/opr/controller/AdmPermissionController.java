package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmButtonPermissionUpdateRequest;
import cpf.adm.opr.dto.AdmMenuPermissionUpdateRequest;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmPermissionService;
import cpf.pfw.common.logging.FpsTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @GetMapping("/menu-matrix")
    @FpsTransaction(id = "ADM01PER0010", name = "ADMMenuPermissionMatrix")
    @Operation(summary = "메뉴 권한 매트릭스 조회", description = "역할별 ADM 메뉴 조회/쓰기/삭제 권한을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMenuMatrix() {
        return ResponseEntity.ok(permissionService.findMenuPermissions());
    }

    @GetMapping("/button-matrix")
    @FpsTransaction(id = "ADM01PER0011", name = "ADMButtonPermissionMatrix")
    @Operation(summary = "버튼 권한 매트릭스 조회", description = "역할별 ADM 버튼/행위 권한을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findButtonMatrix() {
        return ResponseEntity.ok(permissionService.findButtonPermissions());
    }

    @PutMapping("/roles/{roleId}/menus/{menuId}")
    @FpsTransaction(id = "ADM03PER0012", name = "ADMMenuPermissionUpdate")
    @Operation(summary = "메뉴 권한 변경", description = "역할별 메뉴 권한을 변경하고 감사 로그를 남깁니다.")
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
    @FpsTransaction(id = "ADM03PER0013", name = "ADMButtonPermissionUpdate")
    @Operation(summary = "버튼 권한 변경", description = "역할별 버튼/행위 권한을 변경하고 감사 로그를 남깁니다.")
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

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
