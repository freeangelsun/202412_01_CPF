package com.cpf.education.integration.servicecall.controller;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.foundation.workflow.CpfWorkflow;
import com.cpf.foundation.workflow.CpfWorkflowFailurePolicy;
import com.cpf.foundation.workflow.CpfWorkflowStep;
import com.cpf.education.integration.external.EducationExternalIntegrationEducationSample;
import com.cpf.education.integration.servicecall.EducationServiceCallEngineEducationSample;
import com.cpf.education.integration.servicecall.EducationServiceEchoResponse;
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
 * typed service client와 외부 HTTP 호출 표준을 학습하는 EDU API입니다.
 *
 * <p>두 예제 모두 CPF registry를 통해 EDU 자체 중립 시뮬레이터를 호출하므로
 * 특정 Generated Domain이나 인터넷 endpoint의 존재를 전제하지 않습니다.</p>
 */
@RestController
@RequestMapping({"/api/education", "/education/edu"})
@Tag(name = "EDU Education 03. 서비스 호출", description = "내부 서비스 호출과 외부 API 호출 표준 샘플")
public class EducationServiceCallEducationController extends com.cpf.education.base.EducationBaseController {
    private final EducationServiceCallEngineEducationSample serviceCallEducation;
    private final EducationExternalIntegrationEducationSample externalIntegrationEducation;

    /** EducationServiceCallEducationController 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationServiceCallEducationController(
            EducationServiceCallEngineEducationSample serviceCallEducation,
            EducationExternalIntegrationEducationSample externalIntegrationEducation) {
        this.serviceCallEducation = serviceCallEducation;
        this.externalIntegrationEducation = externalIntegrationEducation;
    }

    @GetMapping("/service-call/self-echo")
    @CpfOnlineTransaction(id = "OEDUAA0013", name = "EDUSelfEchoCall", ownerDomain="EDU")
    @CpfWorkflow(id = "OEDUAA9002", name = "EDU 중립 내부 호출")
    @CpfWorkflowStep(name = "EDU 중립 응답 조회", failurePolicy = CpfWorkflowFailurePolicy.VERIFY)
    @Operation(
            operationId = "refServiceCallEducationCallSelfEcho",
            summary = "EDU 중립 typed client 호출 샘플",
            description = "CPF typed service client로 EDU 자체 시뮬레이터를 호출해 거래 헤더 전파를 확인합니다.")
    /** callSelfEcho 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> callSelfEcho(
            @RequestParam(defaultValue = "EDU-EDU-REQUEST") String requestKey) {
        EducationServiceEchoResponse echoResponse = serviceCallEducation.callEcho(requestKey);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("guide", "내부 호출은 서비스 ID, 표준 헤더, timeout, 오류 변환 기준을 함께 관리합니다.");
        response.put("echoResponse", echoResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/service-call/external-simulator")
    @CpfOnlineTransaction(id = "OEDUAA0014", name = "EDUExternalGet", ownerDomain="EDU")
    @Operation(
            operationId = "refServiceCallEducationCallExternalSimulator",
            summary = "EDU 중립 외부 호출 샘플",
            description = "CPF registry 기반 WebClient timeout과 외부 응답 처리 방식을 EDU 자체 시뮬레이터로 확인합니다.")
    /** callExternalSimulator 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String, Object>> callExternalSimulator(
            @RequestParam(defaultValue = "EDU-EDU-EXTERNAL") String externalKey,
            @RequestParam(defaultValue = "200") int status,
            @RequestParam(defaultValue = "0") long delayMillis) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("body", externalIntegrationEducation.call(externalKey, status, delayMillis));
        response.put("guide", "외부 endpoint와 timeout 정책은 중앙 설정 및 adapter가 관리합니다.");
        return ResponseEntity.ok(response);
    }
}
