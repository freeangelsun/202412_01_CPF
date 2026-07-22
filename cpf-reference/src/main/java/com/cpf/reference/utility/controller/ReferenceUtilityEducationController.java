package com.cpf.reference.utility.controller;

import com.cpf.common.utils.DateTimeUtils;
import com.cpf.common.utils.IdUtils;
import com.cpf.common.utils.MaskingUtils;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.workflow.CpfWorkflowContext;
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
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 04. Utility", description = "Common utilities and transaction header samples")
public class ReferenceUtilityEducationController extends com.cpf.reference.common.base.ReferenceBaseController {

    @GetMapping("/utils")
    @CpfOnlineTransaction(id = "OREFAA0018", name = "REFCommonUtilitySample")
    @Operation(operationId = "refUtilityEducationUseCommonUtils", summary = "CMN utility sample", description = "Shows date, id, and masking utility usage.")
    public ResponseEntity<Map<String, Object>> useCommonUtils(@RequestParam(defaultValue = "Sample User") String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("today", DateTimeUtils.today());
        response.put("now", DateTimeUtils.nowDateTimeMillis());
        response.put("uuid32", IdUtils.uuid32());
        response.put("temporaryId", IdUtils.temporaryId("REF"));
        response.put("maskedName", MaskingUtils.maskName(name));
        response.put("maskedSensitive", MaskingUtils.maskSensitive("accountNo=123456789012&password=abc123"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/headers")
    @CpfOnlineTransaction(id = "OREFAA0023", name = "REFCurrentHeaderSample")
    @Operation(operationId = "refUtilityEducationGetCurrentHeaders", summary = "Current header sample", description = "Shows transaction and workflow propagation headers.")
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
