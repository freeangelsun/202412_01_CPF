package cpf.xyz.edu.controller;

import cpf.pfw.common.http.CpfWebClient;
import cpf.pfw.common.logging.CpfTransaction;
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
 * CPF 기능 설명입니다.
 */
@RestController
@RequestMapping("/xyz/edu")
@Tag(name = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
public class XyzServiceCallEducationController {
    private final CpfWebClient cpfWebClient;
    private final WebClient externalWebClient;

    public XyzServiceCallEducationController(
            CpfWebClient cpfWebClient,
            WebClient.Builder webClientBuilder) {
        this.cpfWebClient = cpfWebClient;
        this.externalWebClient = webClientBuilder.build();
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/service-call/mbr-detail")
    @CpfTransaction(id = "XYZ08EDU0001", name = "CPF 처리 기준입니다.")
    @CpfWorkflow(id = "XYZ08EDU9001", name = "CPF 처리 기준입니다.")
    @CpfWorkflowStep(name = "CPF 처리 기준입니다.", failurePolicy = CpfWorkflowFailurePolicy.VERIFY)
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
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
        response.put("guide", "CPF 처리 기준입니다.");
        response.put("mbrResponse", mbrResponse);
        return ResponseEntity.ok(response);
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    @GetMapping("/webclient/external-get")
    @CpfTransaction(id = "XYZ08EDU0010", name = "CPF 처리 기준입니다.")
    @Operation(summary = "CPF 처리 기준입니다.", description = "CPF 처리 기준입니다.")
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
        response.put("guide", "CPF 처리 기준입니다.");
        return ResponseEntity.ok(response);
    }
}

