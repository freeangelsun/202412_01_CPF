package com.cpf.reference.servicecall.controller;

import com.cpf.core.common.http.CpfWebClient;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.workflow.CpfWorkflow;
import com.cpf.core.common.workflow.CpfWorkflowFailurePolicy;
import com.cpf.core.common.workflow.CpfWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 내부 서비스 호출과 외부 HTTP 호출 표준을 학습하는 EDU API입니다.
 *
 * <p>CPF-ARCH-ALLOW-DIRECT-URL: EDU_ONLY</p>
 * <p>이 URL literal은 운영 호출이 아니라 WebClient timeout 교육 샘플의 기본값입니다.
 * 운영 업무 코드는 CPF Service Call Engine registry 기반 호출을 사용해야 합니다.</p>
 */
@RestController
@RequestMapping({"/api/reference", "/reference/edu"})
@Tag(name = "REF Reference 03. 서비스 호출", description = "내부 서비스 호출과 외부 API 호출 표준 샘플")
public class ReferenceServiceCallEducationController extends com.cpf.reference.common.base.ReferenceBaseController {
    private final CpfWebClient cpfWebClient;
    private final WebClient externalWebClient;

    public ReferenceServiceCallEducationController(
            CpfWebClient cpfWebClient,
            WebClient.Builder webClientBuilder,
            @Value("${cpf.ref.external-base-url:https://postman-echo.com}") String externalBaseUrl) {
        this.cpfWebClient = cpfWebClient;
        this.externalWebClient = webClientBuilder.clone().baseUrl(externalBaseUrl).build();
    }

    @GetMapping("/service-call/mbr-detail")
    @CpfOnlineTransaction(id = "OREFAA0013", name = "REFMbrDetailCall")
    @CpfWorkflow(id = "OREFAA9002", name = "회원 상세 내부 호출")
    @CpfWorkflowStep(name = "MBR 회원 상세 조회", failurePolicy = CpfWorkflowFailurePolicy.VERIFY)
    @Operation(operationId = "refServiceCallEducationCallMbrDetail", summary = "MBR 내부 호출 샘플", description = "CpfWebClient로 내부 서비스에 거래 헤더를 전파하는 흐름을 확인합니다.")
    public ResponseEntity<Map<String, Object>> callMbrDetail(@RequestParam Long memberId) {
        Map<String, Object> mbrResponse = cpfWebClient.get(
                "mbr",
                uriBuilder -> uriBuilder
                        .path("/mbr/detail")
                        .queryParam("memberId", memberId)
                        .build(),
                new ParameterizedTypeReference<>() {
                });

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("guide", "내부 호출은 서비스 ID, 표준 헤더, timeout, 오류 변환 기준을 함께 관리합니다.");
        response.put("mbrResponse", mbrResponse);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/webclient/external-get")
    @CpfOnlineTransaction(id = "OREFAA0014", name = "REFExternalGet")
    @Operation(operationId = "refServiceCallEducationCallExternalWebsite", summary = "외부 API 호출 샘플", description = "WebClient timeout과 외부 응답 처리 방식을 확인합니다.")
    public ResponseEntity<Map<String, Object>> callExternalWebsite() {
        String responseBody = externalWebClient.get()
                .uri("/get?source=cpf-ref")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("body", responseBody);
        response.put("guide", "외부 endpoint와 timeout 정책은 중앙 설정 및 adapter가 관리합니다.");
        return ResponseEntity.ok(response);
    }
}

