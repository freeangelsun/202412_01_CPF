package cpf.xyz.edu.controller;

import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.MaskingUtils;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.workflow.CpfWorkflowContext;
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
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 04. Utility", description = "Common utilities and transaction header samples")
public class XyzUtilityEducationController {

    @GetMapping("/utils")
    @CpfTransaction(id = "XYZ09EDU0004", name = "XYZCommonUtilitySample")
    @Operation(operationId = "xyzUtilityEducationUseCommonUtils", summary = "CMN utility sample", description = "Shows date, id, and masking utility usage.")
    public ResponseEntity<Map<String, Object>> useCommonUtils(@RequestParam(defaultValue = "Sample User") String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("today", DateTimeUtils.today());
        response.put("now", DateTimeUtils.nowDateTimeMillis());
        response.put("uuid32", IdUtils.uuid32());
        response.put("temporaryId", IdUtils.temporaryId("XYZ"));
        response.put("maskedName", MaskingUtils.maskName(name));
        response.put("maskedSensitive", MaskingUtils.maskSensitive("accountNo=123456789012&password=abc123"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/headers")
    @CpfTransaction(id = "XYZ09EDU0008", name = "XYZCurrentHeaderSample")
    @Operation(operationId = "xyzUtilityEducationGetCurrentHeaders", summary = "Current header sample", description = "Shows transaction and workflow propagation headers.")
    public ResponseEntity<Map<String, Object>> getCurrentHeaders() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", TransactionContext.getOrCreateTransactionId());
        response.put("traceId", TransactionContext.getOrCreateTraceId());
        response.put("spanId", TransactionContext.getOrCreateSpanId());
        response.put("transactionHeader", TransactionContext.currentHeader());
        response.put("propagationHeaders", TransactionContext.propagationHeaders());
        response.put("workflowPropagationHeaders", CpfWorkflowContext.propagationHeaders());
        return ResponseEntity.ok(response);
    }
}
