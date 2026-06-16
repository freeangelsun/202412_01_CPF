package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmMenu;
import cpf.adm.opr.dto.AdmOperator;
import cpf.adm.opr.dto.AdmOperatorCreateRequest;
import cpf.adm.opr.dto.AdmPasswordChangeRequest;
import cpf.adm.opr.dto.AdmRole;
import cpf.adm.opr.service.AdmOperatorService;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.FpsTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/operators")
@Tag(name = "ADM-OPR Operators", description = "Operator, role, and menu management APIs")
public class AdmOperatorController {
    private final AdmOperatorService operatorService;
    private final AdmAuditLogService auditLogService;

    public AdmOperatorController(AdmOperatorService operatorService, AdmAuditLogService auditLogService) {
        this.operatorService = operatorService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @FpsTransaction(id = "ADM01OPR0030", name = "ADMOperatorList")
    @Operation(summary = "List operators", description = "Returns ADM operators and account status.")
    public ResponseEntity<List<AdmOperator>> findOperators() {
        return ResponseEntity.ok(operatorService.findOperators());
    }

    @PostMapping
    @FpsTransaction(id = "ADM02OPR0031", name = "ADMOperatorCreate")
    @Operation(summary = "Create operator", description = "Creates an ADM operator after password policy validation.")
    public ResponseEntity<AdmOperator> createOperator(@RequestBody AdmOperatorCreateRequest request, HttpServletRequest servletRequest) {
        AdmOperator operator = operatorService.createOperator(request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                request.requestUser(),
                "OPERATOR_CREATE",
                "operator_user",
                operator.operatorId(),
                "Operator created",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @PostMapping("/{operatorId}/password")
    @FpsTransaction(id = "ADM03OPR0032", name = "ADMOperatorPasswordChange")
    @Operation(summary = "Change operator password", description = "Changes an operator password after policy validation.")
    public ResponseEntity<AdmOperator> changePassword(
            @PathVariable String operatorId,
            @RequestBody AdmPasswordChangeRequest request,
            HttpServletRequest servletRequest) {
        AdmOperator operator = operatorService.changePassword(operatorId, request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                request.requestUser(),
                "OPERATOR_PASSWORD_CHANGE",
                "operator_user",
                operatorId,
                "Operator password changed",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(operator);
    }

    @GetMapping("/password-policy/validate")
    @FpsTransaction(id = "ADM06OPR0033", name = "ADMPasswordPolicyValidate")
    @Operation(summary = "Validate password policy", description = "Checks whether a password satisfies the ADM policy.")
    public ResponseEntity<Map<String, Object>> validatePassword(@RequestParam String operatorId, @RequestParam String password) {
        return ResponseEntity.ok(operatorService.validatePassword(operatorId, password));
    }

    @GetMapping("/roles")
    @FpsTransaction(id = "ADM01OPR0034", name = "ADMRoleList")
    @Operation(summary = "List roles", description = "Returns ADM roles.")
    public ResponseEntity<List<AdmRole>> findRoles() {
        return ResponseEntity.ok(operatorService.findRoles());
    }

    @GetMapping("/menus")
    @FpsTransaction(id = "ADM01OPR0035", name = "ADMMenuList")
    @Operation(summary = "List menus", description = "Returns ADM menus.")
    public ResponseEntity<List<AdmMenu>> findMenus() {
        return ResponseEntity.ok(operatorService.findMenus());
    }
}
