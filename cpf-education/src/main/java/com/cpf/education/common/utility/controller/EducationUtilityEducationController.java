package com.cpf.education.common.utility.controller;
import com.cpf.foundation.util.CpfTimes;
import com.cpf.foundation.id.CpfIds;
import com.cpf.security.api.CpfMasking;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.workflow.api.CpfWorkflow;
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
 * CPF 범용 Utility와 거래 Context 전파 기능을 실제 공개 API로 보여주는 EDU 교육 Controller입니다.
 *
 * <p>ID·시간·마스킹·거래 추적 Header 예제를 한 곳에서 제공하여
 * 신규 Domain이 JDK/개별 라이브러리를 임의 조합하지 않고 CPF 표준을 사용할 수 있게 합니다.</p>
 */
@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 04. Utility", description = "Common utilities and transaction header samples")
public class EducationUtilityEducationController extends com.cpf.education.base.EducationBaseController {

    @GetMapping("/utils")
    @CpfOnlineTransaction(id = "OEDUAA0018", name = "EDUCommonUtilitySample", ownerDomain="EDU")
    @Operation(operationId = "refUtilityEducationUseCommonUtils", summary = "CMN utility sample", description = "Shows date, id, and masking utility usage.")
    /** useCommonUtils 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> useCommonUtils(@RequestParam(defaultValue = "Sample User") String name) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("today", CpfTimes.today());
        response.put("now", CpfTimes.nowDateTimeMillis());
        response.put("uuid32", CpfIds.uuid32());
        response.put("temporaryId", CpfIds.temporaryId("EDU"));
        response.put("maskedName", CpfMasking.maskName(name));
        response.put("maskedSensitive", CpfMasking.maskSensitive("accountNo=123456789012&password=abc123"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/headers")
    @CpfOnlineTransaction(id = "OEDUAA0023", name = "EDUCurrentHeaderSample", ownerDomain="EDU")
    @Operation(operationId = "refUtilityEducationGetCurrentHeaders", summary = "Current header sample", description = "Shows transaction and workflow propagation headers.")
    public ResponseEntity<Map<String, Object>> getCurrentHeaders() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("transactionId", CpfContexts.transactionId());
        response.put("traceId", CpfContexts.traceId());
        response.put("spanId", CpfContexts.spanId());
                response.put("propagationHeaders", CpfContexts.propagationHeaders());
        response.put("workflowPropagationHeaders", CpfWorkflow.propagationHeaders());
        return ResponseEntity.ok(response);
    }
}
