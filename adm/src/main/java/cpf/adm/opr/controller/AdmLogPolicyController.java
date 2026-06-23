package cpf.adm.opr.controller;

import cpf.adm.opr.dto.AdmLogPolicyOverrideRequest;
import cpf.adm.opr.dto.AdmLogPolicyRequest;
import cpf.adm.opr.service.AdmLogPolicyService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/adm/api/log-policies")
@Tag(name = "ADM-OPR Log Policy", description = "PFW 로그 정책과 임시 override 관리 API")
public class AdmLogPolicyController {
    private final AdmLogPolicyService logPolicyService;

    public AdmLogPolicyController(AdmLogPolicyService logPolicyService) {
        this.logPolicyService = logPolicyService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01LGP0010", name = "ADMLogPolicyList")
    @Operation(summary = "로그 정책 목록 조회", description = "대상 유형, 대상 ID, 활성 여부 기준으로 PFW 로그 정책을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findPolicies(
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) String activeYn,
            @RequestParam(defaultValue = "200") int limit) {
        return ResponseEntity.ok(logPolicyService.findPolicies(targetType, targetId, activeYn, limit));
    }

    @GetMapping("/{policyId}")
    @CpfTransaction(id = "ADM01LGP0011", name = "ADMLogPolicyDetail")
    @Operation(summary = "로그 정책 상세 조회", description = "로그 정책과 해당 정책의 override 이력을 조회합니다.")
    public ResponseEntity<Map<String, Object>> findPolicy(@PathVariable long policyId) {
        return ResponseEntity.ok(logPolicyService.findPolicy(policyId));
    }

    @PostMapping
    @CpfTransaction(id = "ADM03LGP0012", name = "ADMLogPolicyCreate")
    @Operation(summary = "로그 정책 등록", description = "기본 로그 정책을 등록하거나 같은 policyKey 정책을 갱신합니다.")
    public ResponseEntity<Map<String, Object>> createPolicy(
            @RequestBody AdmLogPolicyRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(logPolicyService.createPolicy(request, requestUser(servletRequest, request.requestUser()), servletRequest.getRemoteAddr()));
    }

    @PutMapping("/{policyId}")
    @CpfTransaction(id = "ADM03LGP0013", name = "ADMLogPolicyUpdate")
    @Operation(summary = "로그 정책 수정", description = "기본 로그 정책을 수정하고 pfw_log_policy_audit에 변경 이력을 남깁니다.")
    public ResponseEntity<Map<String, Object>> updatePolicy(
            @PathVariable long policyId,
            @RequestBody AdmLogPolicyRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(logPolicyService.updatePolicy(policyId, request, requestUser(servletRequest, request.requestUser()), servletRequest.getRemoteAddr()));
    }

    @PostMapping("/overrides")
    @CpfTransaction(id = "ADM03LGP0014", name = "ADMLogPolicyOverrideCreate")
    @Operation(summary = "로그 정책 override 등록", description = "기간과 사유가 있는 임시 로그 정책 override를 등록합니다.")
    public ResponseEntity<Map<String, Object>> createOverride(
            @RequestBody AdmLogPolicyOverrideRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(logPolicyService.createOverride(request, requestUser(servletRequest, request.requestUser()), servletRequest.getRemoteAddr()));
    }

    @PatchMapping("/overrides/{overrideId}/disable")
    @CpfTransaction(id = "ADM04LGP0015", name = "ADMLogPolicyOverrideDisable")
    @Operation(summary = "로그 정책 override 중지", description = "임시 override를 비활성화하고 감사 이력을 남깁니다.")
    public ResponseEntity<Map<String, Object>> disableOverride(
            @PathVariable long overrideId,
            @RequestParam String reason,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(logPolicyService.disableOverride(overrideId, reason, requestUser(servletRequest, "ADM"), servletRequest.getRemoteAddr()));
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
