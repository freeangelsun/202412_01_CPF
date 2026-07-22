package com.cpf.reference.logging.controller;

import com.cpf.core.common.logging.DynamicLogLevelRequest;
import com.cpf.core.common.logging.DynamicLogLevelRule;
import com.cpf.core.common.logging.DynamicTransactionLogLevelService;
import com.cpf.core.common.logging.CpfLogLevel;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 동적 로그 레벨 운영 방법을 보여주는 EDU API입니다.
 */
@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 10. 동적 로그", description = "거래별 로그 레벨을 운영 중 임시로 높이는 교육 샘플")
public class ReferenceDynamicLogEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final DynamicTransactionLogLevelService dynamicLogLevelService;

    public ReferenceDynamicLogEducationController(DynamicTransactionLogLevelService dynamicLogLevelService) {
        this.dynamicLogLevelService = dynamicLogLevelService;
    }

    @PutMapping("/admin/log-level")
    @CpfOnlineTransaction(id = "OREFAA0019", name = "REFDynamicLogLevelRegister")
    @Operation(operationId = "refDynamicLogEducationRegisterDynamicLogLevel", summary = "동적 로그 레벨 등록", description = "업무 거래 ID 또는 거래 ID 기준으로 임시 로그 레벨 규칙을 등록합니다.")
    public ResponseEntity<DynamicLogLevelRule> registerDynamicLogLevel(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") CpfLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam(defaultValue = "운영 진단을 위한 임시 로그 레벨 변경") String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser) {

        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId(businessTransactionId);
        request.setTransactionId(transactionId);
        request.setModuleId("REF");
        request.setLogLevel(logLevel);
        request.setTtl(Duration.ofSeconds(ttlSeconds));
        request.setReason(reason);
        request.setRequestUser(requestUser);
        return ResponseEntity.ok(dynamicLogLevelService.register(request));
    }

    @GetMapping("/admin/log-level")
    @CpfOnlineTransaction(id = "OREFAA0021", name = "REFDynamicLogLevelList")
    @Operation(operationId = "refDynamicLogEducationFindDynamicLogLevelRules", summary = "동적 로그 레벨 조회", description = "현재 유효한 동적 로그 레벨 규칙을 조회합니다.")
    public ResponseEntity<List<DynamicLogLevelRule>> findDynamicLogLevelRules() {
        return ResponseEntity.ok(dynamicLogLevelService.findActiveRules());
    }

    @DeleteMapping("/admin/log-level")
    @CpfOnlineTransaction(id = "OREFAA0022", name = "REFDynamicLogLevelRemove")
    @Operation(operationId = "refDynamicLogEducationRemoveDynamicLogLevelRule", summary = "동적 로그 레벨 제거", description = "등록된 동적 로그 레벨 규칙을 ruleId 기준으로 제거합니다.")
    public ResponseEntity<Map<String, Object>> removeDynamicLogLevelRule(@RequestParam String ruleId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("removed", dynamicLogLevelService.remove(ruleId));
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }
}

