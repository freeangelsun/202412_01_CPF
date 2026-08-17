package com.cpf.admin.opr.controller;

import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRequest;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import com.cpf.platform.operations.observability.api.logging.CpfDynamicLogLevelOperations;
import com.cpf.platform.operations.observability.api.logging.CpfLogLevel;
import com.cpf.admin.opr.service.AdmDynamicLogLevelRuleStore;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmDynamicLogLevelBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Instance별 동적 로그 레벨 변경을 CAS·사유·감사와 함께 제어합니다. */
@RestController
@RequestMapping("/adm/api/log-level")
@Tag(name = "ADM-OPR Dynamic Log", description = "Temporary transaction log-level control APIs")
public class AdmDynamicLogLevelController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfDynamicLogLevelOperations dynamicLogLevelService;
    private final AdmDynamicLogLevelRuleStore ruleStore;
    private final AdmAuditLogService auditLogService;
    private final AdmDynamicLogLevelBroadcastService broadcastService;

    public AdmDynamicLogLevelController(
            CpfDynamicLogLevelOperations dynamicLogLevelService,
            AdmDynamicLogLevelRuleStore ruleStore,
            AdmAuditLogService auditLogService,
            AdmDynamicLogLevelBroadcastService broadcastService) {
        this.dynamicLogLevelService = dynamicLogLevelService;
        this.ruleStore = ruleStore;
        this.auditLogService = auditLogService;
        this.broadcastService = broadcastService;
    }

    @GetMapping("/rules")    @Operation(operationId = "admDynamicLogLevelFindRules", summary = "List dynamic log rules", description = "Returns active dynamic log-level rules for this WAS.")
    public ResponseEntity<Map<String, Object>> findRules() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runtimeRules", dynamicLogLevelService.findActiveRules());
        response.put("persistedRules", ruleStore.findActiveRules());
        response.put("persistence", ruleStore.persistenceStatus());
        return ResponseEntity.ok(response);
    }

    @Hidden
    @PutMapping("/rules")    @Operation(operationId = "admDynamicLogLevelRegister", summary = "Register dynamic log rule", description = "Approval Engine Owner Command only.")
    public ResponseEntity<DynamicLogLevelRule> register(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") CpfLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        requireOperator(servletRequest);
        throw approvalRequired();
    }

    @Hidden
    @DeleteMapping("/rules/{ruleId}")    @Operation(operationId = "admDynamicLogLevelRemove", summary = "Remove dynamic log rule", description = "Approval Engine Owner Command only.")
    public ResponseEntity<Map<String, Object>> remove(
            @PathVariable String ruleId,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        requireOperator(servletRequest);
        throw approvalRequired();
    }

    private ResponseStatusException approvalRequired() {
        return new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                "동적 로그 레벨 등록/제거는 Approval Engine의 DYNAMIC_LOG_* Owner Command로 실행해야 합니다.");
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        return requireOperator(request);
    }
}
