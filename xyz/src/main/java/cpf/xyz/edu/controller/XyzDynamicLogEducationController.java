package cpf.xyz.edu.controller;

import cpf.pfw.common.logging.DynamicLogLevelRequest;
import cpf.pfw.common.logging.DynamicLogLevelRule;
import cpf.pfw.common.logging.DynamicTransactionLogLevelService;
import cpf.pfw.common.logging.CpfLogLevel;
import cpf.pfw.common.logging.CpfTransaction;
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
 * CPF 기능 설명입니다.
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
public class XyzDynamicLogEducationController {
    private final DynamicTransactionLogLevelService dynamicLogLevelService;

    public XyzDynamicLogEducationController(DynamicTransactionLogLevelService dynamicLogLevelService) {
        this.dynamicLogLevelService = dynamicLogLevelService;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @PutMapping("/admin/log-level")
    @CpfTransaction(id = "XYZ09EDU0005", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<DynamicLogLevelRule> registerDynamicLogLevel(
            @RequestParam(required = false) String businessTransactionId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "DEBUG") CpfLogLevel logLevel,
            @RequestParam(defaultValue = "600") long ttlSeconds,
            @RequestParam(defaultValue = "CPF 처리 기준입니다.") String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser) {

        DynamicLogLevelRequest request = new DynamicLogLevelRequest();
        request.setBusinessTransactionId(businessTransactionId);
        request.setTransactionId(transactionId);
        request.setModuleId("XYZ");
        request.setLogLevel(logLevel);
        request.setTtl(Duration.ofSeconds(ttlSeconds));
        request.setReason(reason);
        request.setRequestUser(requestUser);
        return ResponseEntity.ok(dynamicLogLevelService.register(request));
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    @GetMapping("/admin/log-level")
    @CpfTransaction(id = "XYZ09EDU0006", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<List<DynamicLogLevelRule>> findDynamicLogLevelRules() {
        return ResponseEntity.ok(dynamicLogLevelService.findActiveRules());
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @DeleteMapping("/admin/log-level")
    @CpfTransaction(id = "XYZ09EDU0007", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
    public ResponseEntity<Map<String, Object>> removeDynamicLogLevelRule(@RequestParam String ruleId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("removed", dynamicLogLevelService.remove(ruleId));
        response.put("ruleId", ruleId);
        return ResponseEntity.ok(response);
    }
}

