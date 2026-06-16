package cpf.adm.opr.controller;

import cpf.pfw.common.logging.DynamicLogLevelRequest;
import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.DynamicTransactionLogLevelService;
import cpf.pfw.common.logging.FpsLogLevel;
import cpf.pfw.common.logging.FpsTransaction;
import cpf.adm.opr.service.AdmDynamicLogLevelRuleStore;
import cpf.adm.opr.service.AdmAuditLogService;
import cpf.cmn.ref.service.CacheRefreshEventPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
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
    private final ObjectProvider<CacheRefreshEventPublisher> cacheRefreshEventPublisherProvider;

    public AdmDynamicLogLevelController(
            DynamicTransactionLogLevelService dynamicLogLevelService,
            AdmDynamicLogLevelRuleStore ruleStore,
            AdmAuditLogService auditLogService,
            ObjectProvider<CacheRefreshEventPublisher> cacheRefreshEventPublisherProvider) {
        this.dynamicLogLevelService = dynamicLogLevelService;
        this.ruleStore = ruleStore;
        this.auditLogService = auditLogService;
        this.cacheRefreshEventPublisherProvider = cacheRefreshEventPublisherProvider;
    }

    @GetMapping("/rules")
    @FpsTransaction(id = "ADM01OPR0020", name = "ADMDynamicLogRuleList")
    @Operation(summary = "List dynamic log rules", description = "Returns active dynamic log-level rules for this WAS.")
    public ResponseEntity<Map<String, Object>> findRules() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runtimeRules", dynamicLogLevelService.findActiveRules());
        response.put("persistedRules", ruleStore.findActiveRules());
        response.put("persistence", ruleStore.persistenceStatus());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/rules")
    @FpsTransaction(id = "ADM05OPR0021", name = "ADMDynamicLogRuleRegister")
    @Operation(summary = "Register dynamic log rule", description = "Registers a temporary log-level rule by transaction id or business transaction id.")
    public ResponseEntity<DynamicLogLevelRule> register(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") FpsLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam(defaultValue = "ADM diagnostics") String reason,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId(businessTransactionId);
        request.setTransactionId(transactionId);
        request.setModuleId("ADM");
        request.setLogLevel(logLevel);
        request.setTtl(Duration.ofSeconds(ttlSeconds));
        request.setReason(reason);
        request.setRequestUser(requestUser);
        DynamicLogLevelRule rule = dynamicLogLevelService.register(request);
        ruleStore.save(rule);
        publishDynamicLogEvent("UPSERT", rule.ruleId(), requestUser);
        auditLogService.record(
                cpf.pfw.common.logging.TransactionContext.getOrCreateTransactionId(),
                requestUser,
                "DYNAMIC_LOG_REGISTER",
                "dynamic_log_level_rule",
                rule.ruleId(),
                reason,
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(rule);
    }

    @DeleteMapping("/rules/{ruleId}")
    @FpsTransaction(id = "ADM04OPR0022", name = "ADMDynamicLogRuleRemove")
    @Operation(summary = "Remove dynamic log rule", description = "Removes a dynamic log-level rule by rule id.")
    public ResponseEntity<Map<String, Object>> remove(
            @PathVariable String ruleId,
            @RequestParam(defaultValue = "ADM") String requestUser,
            HttpServletRequest servletRequest) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("runtimeRemoved", dynamicLogLevelService.remove(ruleId));
        response.put("persistedDisabled", ruleStore.disable(ruleId, requestUser));
        publishDynamicLogEvent("DELETE", ruleId, requestUser);
        auditLogService.record(
                cpf.pfw.common.logging.TransactionContext.getOrCreateTransactionId(),
                requestUser,
                "DYNAMIC_LOG_REMOVE",
                "dynamic_log_level_rule",
                ruleId,
                "Dynamic log-level rule removed",
                servletRequest.getRemoteAddr());
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }

    private void publishDynamicLogEvent(String eventType, String ruleId, String requestUser) {
        CacheRefreshEventPublisher publisher = cacheRefreshEventPublisherProvider.getIfAvailable();
        if (publisher != null) {
            publisher.publishAfterCommit("dynamicLogLevelRule", eventType, ruleId, requestUser);
        }
    }
}
