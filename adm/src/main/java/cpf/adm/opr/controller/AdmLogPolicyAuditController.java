package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmObservabilityService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * PFW 로그 정책 감사 로그 조회 API입니다.
 */
@RestController
@RequestMapping("/adm/api/log-policy-audits")
@Tag(name = "ADM-LogPolicyAudit", description = "PFW 로그 정책 감사 조회 API")
public class AdmLogPolicyAuditController {
    private final AdmObservabilityService observabilityService;

    public AdmLogPolicyAuditController(AdmObservabilityService observabilityService) {
        this.observabilityService = observabilityService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01LGP0018", name = "ADMLogPolicyAuditList")
    @Operation(operationId = "admLogPolicyAuditFindPolicyAudits", summary = "로그 정책 감사 목록 조회", description = "정책 변경, override 등록/중지, cache refresh 이력을 pfw_log_policy_audit 기준으로 조회합니다.")
    public ResponseEntity<Map<String, Object>> findPolicyAudits(
            @RequestParam(required = false) String operatorId,
            @RequestParam(required = false) String actionType,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) Long policyId,
            @RequestParam(required = false) Long overrideId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(observabilityService.findPolicyAudits(
                operatorId, actionType, targetType, targetId, policyId, overrideId, limit));
    }
}
