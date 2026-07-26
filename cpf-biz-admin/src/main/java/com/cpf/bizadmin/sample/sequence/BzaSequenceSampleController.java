package com.cpf.bizadmin.sample.sequence;

import com.cpf.bizadmin.common.base.BzaBaseController;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 명시적으로 활성화한 환경에서만 노출되는 BZA 채번 Customization Sample API입니다. */
@RestController
@RequestMapping("/api/bza/sample/sequence")
@ConditionalOnProperty(prefix="cpf.bza.sample.sequence",name="enabled",havingValue="true")
@Tag(name="BZA-Sequence-Sample",description="선택형 업무 채번 Customization Sample")
public class BzaSequenceSampleController extends BzaBaseController {
    private final BzaSequenceSampleService service;

    public BzaSequenceSampleController(BzaSequenceSampleService service) {
        this.service = service;
    }

    @GetMapping("/rules")
    @CpfOnlineTransaction(id = "OBZASQ0001", name = "BzaSequenceSampleRules")
    @Operation(operationId = "bzaSequenceSampleRules", summary = "채번 Sample 규칙 조회")
    public ResponseEntity<?> rules() {
        return ResponseEntity.ok(service.rules());
    }

    @PostMapping("/rules")
    @CpfOnlineTransaction(id = "OBZASQ0002", name = "BzaSequenceSampleRuleSave")
    @Operation(operationId = "bzaSequenceSampleSaveRule", summary = "채번 Sample 규칙 저장")
    public ResponseEntity<?> save(
            @RequestBody BzaSequenceSampleService.RuleRequest request,
            @RequestAttribute("bza.operatorId") String user) {
        return ResponseEntity.ok(service.save(request, user));
    }

    @PostMapping("/rules/{code}/issue")
    @CpfOnlineTransaction(id = "OBZASQ0003", name = "BzaSequenceSampleIssue")
    @Operation(operationId = "bzaSequenceSampleIssue", summary = "채번 Sample 번호 발급")
    public ResponseEntity<?> issue(
            @PathVariable String code,
            @RequestBody Map<String, Object> body,
            @RequestAttribute("bza.operatorId") String user) {
        return ResponseEntity.ok(service.issue(
                code, user, String.valueOf(body.getOrDefault("reason", ""))));
    }

    @GetMapping("/history")
    @CpfOnlineTransaction(id = "OBZASQ0004", name = "BzaSequenceSampleHistory")
    @Operation(operationId = "bzaSequenceSampleHistory", summary = "채번 Sample 발급 이력 조회")
    public ResponseEntity<?> history(
            @RequestParam(required = false) String ruleCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(service.history(ruleCode, limit));
    }
}
