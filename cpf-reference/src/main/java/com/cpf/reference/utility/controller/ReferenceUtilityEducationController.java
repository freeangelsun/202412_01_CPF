package com.cpf.reference.utility.controller;

import com.cpf.core.api.util.CpfTimes;
import com.cpf.core.api.util.CpfIds;
import com.cpf.core.api.security.CpfMasking;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.workflow.CpfWorkflow;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CPF 범용 Utility와 거래 Context 전파 기능을 실제 공개 API로 보여주는 REF 교육 Controller입니다.
 *
 * <p>ID·시간·마스킹·거래 추적 Header 예제를 한 곳에서 제공하여
 * 신규 Domain이 JDK/개별 라이브러리를 임의 조합하지 않고 CPF 표준을 사용할 수 있게 합니다.</p>
 */
@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 04. Utility", description = "Common utilities and transaction header samples")
public class ReferenceUtilityEducationController extends com.cpf.reference.common.base.ReferenceBaseController {

    @GetMapping("/utils")
    @CpfOnlineTransaction(id = "OREFAA0018", name = "REFCommonUtilitySample")
    @Operation(operationId = "refUtilityEducationUseCommonUtils", summary = "CMN utility sample", description = "Shows date, id, and masking utility usage.")
    public ResponseEntity<Map<String, Object>> useCommonUtils(@RequestParam(defaultValue = "Sample User") String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("today", CpfTimes.today());
        response.put("now", CpfTimes.nowDateTimeMillis());
        response.put("uuid32", CpfIds.uuid32());
        response.put("temporaryId", CpfIds.temporaryId("REF"));
        response.put("maskedName", CpfMasking.maskName(name));
        response.put("maskedSensitive", CpfMasking.maskSensitive("accountNo=123456789012&password=abc123"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/headers")
    @CpfOnlineTransaction(id = "OREFAA0023", name = "REFCurrentHeaderSample")
    @Operation(operationId = "refUtilityEducationGetCurrentHeaders", summary = "Current header sample", description = "Shows transaction and workflow propagation headers.")
    public ResponseEntity<Map<String, Object>> getCurrentHeaders() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", CpfTransactionContext.transactionId());
        response.put("traceId", CpfTransactionContext.traceId());
        response.put("spanId", CpfTransactionContext.spanId());
                response.put("propagationHeaders", CpfTransactionContext.propagationHeaders());
        response.put("workflowPropagationHeaders", CpfWorkflow.propagationHeaders());
        return ResponseEntity.ok(response);
    }
}
