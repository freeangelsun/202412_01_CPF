package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/audit-logs")
@Tag(name = "ADM-OPR Audit Logs", description = "Operator audit log query APIs")
public class AdmAuditLogController {
    private final AdmAuditLogService auditLogService;

    public AdmAuditLogController(AdmAuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01OPR0050", name = "ADMAuditLogList")
    @Operation(operationId = "admAuditLogFindAuditLogs", summary = "List audit logs", description = "Returns recent ADM operator audit logs.")
    public ResponseEntity<Map<String, Object>> findAuditLogs(
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(defaultValue = "100") int limit) {

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("items", auditLogService.findAuditLogs(operatorId, actionType, targetType, targetId, limit));
        return ResponseEntity.ok(response);
    }
}
