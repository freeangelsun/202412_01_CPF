package cpf.adm.opr.controller;

import cpf.pfw.common.logging.DynamicLogLevelRequest;
import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.DynamicTransactionLogLevelService;
import cpf.pfw.common.logging.CpfLogLevel;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.adm.opr.service.AdmDynamicLogLevelRuleStore;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.adm.opr.service.AdmDynamicLogLevelBroadcastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/adm/api/log-level")
@Tag(name = "ADM-OPR Dynamic Log", description = "Temporary transaction log-level control APIs")
public class AdmDynamicLogLevelController {
    private final DynamicTransactionLogLevelService dynamicLogLevelService;
    private final AdmDynamicLogLevelRuleStore ruleStore;
    private final AdmAuditLogService auditLogService;
    private final AdmDynamicLogLevelBroadcastService broadcastService;

    public AdmDynamicLogLevelController(
            DynamicTransactionLogLevelService dynamicLogLevelService,
            AdmDynamicLogLevelRuleStore ruleStore,
            AdmAuditLogService auditLogService,
            AdmDynamicLogLevelBroadcastService broadcastService) {
        this.dynamicLogLevelService = dynamicLogLevelService;
        this.ruleStore = ruleStore;
        this.auditLogService = auditLogService;
        this.broadcastService = broadcastService;
    }

    @GetMapping("/rules")
    @CpfTransaction(id = "ADM01OPR0020", name = "ADMDynamicLogRuleList")
    @Operation(operationId = "admDynamicLogLevelFindRules", summary = "List dynamic log rules", description = "Returns active dynamic log-level rules for this WAS.")
    public ResponseEntity<Map<String, Object>> findRules() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runtimeRules", dynamicLogLevelService.findActiveRules());
        response.put("persistedRules", ruleStore.findActiveRules());
        response.put("persistence", ruleStore.persistenceStatus());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rules")
    @CpfTransaction(id = "ADM05OPR0021", name = "ADMDynamicLogRuleRegister")
    @Operation(operationId = "admDynamicLogLevelRegister", summary = "Register dynamic log rule", description = "Registers a temporary log-level rule by transaction id or business transaction id.")
    public ResponseEntity<DynamicLogLevelRule> register(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") CpfLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId(businessTransactionId);
        request.setTransactionId(transactionId);
        request.setModuleId("ADM");
        request.setLogLevel(logLevel);
        request.setTtl(Duration.ofSeconds(ttlSeconds));
        request.setReason(auditReason);
        request.setRequestUser(requestUser);
        DynamicLogLevelRule rule = dynamicLogLevelService.register(request);
        ruleStore.save(rule);
        String operatorId = requestUser(servletRequest, requestUser);
        broadcastService.publishUpsert(rule, operatorId);
        auditLogService.record(
                cpf.pfw.common.logging.TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "DYNAMIC_LOG_REGISTER",
                "adm_dynamic_log_level_rule",
                rule.ruleId(),
                auditReason,
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/rules/{ruleId}")
    @CpfTransaction(id = "ADM04OPR0022", name = "ADMDynamicLogRuleRemove")
    @Operation(operationId = "admDynamicLogLevelRemove", summary = "Remove dynamic log rule", description = "Removes a dynamic log-level rule by rule id.")
    public ResponseEntity<Map<String, Object>> remove(
            @PathVariable String ruleId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        String auditReason = auditLogService.requireReason(reason);
        String operatorId = requestUser(servletRequest, requestUser);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runtimeRemoved", dynamicLogLevelService.remove(ruleId));
        response.put("persistedDisabled", ruleStore.disable(ruleId, operatorId));
        broadcastService.publishDelete(ruleId, operatorId);
        auditLogService.record(
                cpf.pfw.common.logging.TransactionContext.getOrCreateTransactionId(),
                operatorId,
                "DYNAMIC_LOG_REMOVE",
                "adm_dynamic_log_level_rule",
                ruleId,
                auditReason,
                servletRequest.getRemoteAddr());
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
