package cpf.xyz.edu.controller;

import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.workflow.CpfWorkflow;
import cpf.pfw.common.workflow.CpfWorkflowFailurePolicy;
import cpf.pfw.common.workflow.CpfWorkflowStep;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 내부 서비스 호출과 외부 HTTP 호출 표준을 학습하는 EDU API입니다.
 *
 * <p>CPF-ARCH-ALLOW-DIRECT-URL: EDU_ONLY</p>
 * <p>이 URL literal은 운영 호출이 아니라 WebClient timeout 교육 샘플의 기본값입니다.
 * 운영 업무 코드는 PFW Service Call Engine registry 기반 호출을 사용해야 합니다.</p>
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "XYZ-EDU 03. 서비스 호출", description = "내부 서비스 호출과 외부 API 호출 표준 샘플")
public class XyzServiceCallEducationController {
    private final CpfWebClient cpfWebClient;
    private final WebClient externalWebClient;

    public XyzServiceCallEducationController(
            CpfWebClient cpfWebClient,
            WebClient.Builder webClientBuilder) {
        this.cpfWebClient = cpfWebClient;
        this.externalWebClient = webClientBuilder.build();
    }

    @GetMapping("/service-call/mbr-detail")
    @CpfOnlineTransaction(id = "OXYZ-EDU-08-0001", name = "XYZMbrDetailCall")
    @CpfWorkflow(id = "OXYZ-EDU-08-9001", name = "회원 상세 내부 호출")
    @CpfWorkflowStep(name = "MBR 회원 상세 조회", failurePolicy = CpfWorkflowFailurePolicy.VERIFY)
    @Operation(operationId = "xyzServiceCallEducationCallMbrDetail", summary = "MBR 내부 호출 샘플", description = "CpfWebClient로 내부 서비스에 거래 헤더를 전파하는 흐름을 확인합니다.")
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
    @CpfOnlineTransaction(id = "OXYZ-EDU-08-0010", name = "XYZExternalGet")
    @Operation(operationId = "xyzServiceCallEducationCallExternalWebsite", summary = "외부 API 호출 샘플", description = "WebClient timeout과 외부 응답 처리 방식을 확인합니다.")
    public ResponseEntity<Map<String, Object>> callExternalWebsite(
            @RequestParam(defaultValue = "https://postman-echo.com/get?source=cpf-xyz") String url) {
        String responseBody = externalWebClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block(Duration.ofSeconds(5));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("url", url);
        response.put("body", responseBody);
        response.put("guide", "운영 코드는 retry, circuit breaker, correlation id 전파, 민감정보 마스킹을 함께 적용해야 합니다.");
        return ResponseEntity.ok(response);
    }
}

