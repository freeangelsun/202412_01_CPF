package cpf.xyz.edu.controller;

import cpf.pfw.common.execution.CpfOnlineTransaction;
import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 표준 거래 헤더의 수신, 컨텍스트 적재, 하위 호출 전파를 한 번에 검증하는 EDU API입니다.
 */
@RestController
@RequestMapping("/xyz/edu/headers")
@Tag(name = "XYZ-EDU 14. Standard Header", description = "CPF 표준 거래 헤더 런타임 검증 예제")
public class XyzStandardHeaderEducationController {

    private final WebClient.Builder webClientBuilder;

    public XyzStandardHeaderEducationController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    /**
     * 현재 거래 컨텍스트의 전파 허용 헤더만 하위 시스템으로 보내고 수신 결과를 반환합니다.
     */
    @GetMapping("/propagation")
    @CpfOnlineTransaction(id = "OXYZ-EDU-15-0001", name = "XYZ표준헤더전파검증")
    @Operation(
            operationId = "xyzStandardHeaderEducationVerifyPropagation",
            summary = "표준 거래 헤더 전파 검증",
            description = "수신된 표준 헤더와 허용된 확장 헤더를 하위 호출에 전파하고 거래 컨텍스트와 응답을 함께 반환합니다.")
    public ResponseEntity<Map<String, Object>> verifyPropagation(
            @RequestParam String menuId,
            @RequestParam String execUser,
            @RequestParam String mockUrl) {

        Map<String, String> outboundHeaders = CpfHeaderPropagator.outboundHeaders();
        Map<String, Object> downstreamResponse = webClientBuilder.build()
                .get()
                .uri(mockUrl)
                .headers(headers -> outboundHeaders.forEach(headers::add))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("menuId", menuId);
        response.put("execUser", execUser);
        response.put("transactionId", TransactionContext.currentTransactionId());
        response.put("traceId", TransactionContext.currentTraceId());
        response.put("outboundHeaders", outboundHeaders);
        response.put("downstream", downstreamResponse);
        return ResponseEntity.ok(response);
    }
}
