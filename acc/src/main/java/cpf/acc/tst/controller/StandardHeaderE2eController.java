package cpf.acc.tst.controller;

import cpf.pfw.common.header.CpfHeaderPropagator;
import cpf.pfw.common.logging.CpfTransaction;
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

@RestController
@RequestMapping("/acc/tran")
@Tag(name = "ACC-TST Standard Header E2E", description = "CPF 표준 헤더 runtime E2E smoke API")
public class StandardHeaderE2eController {

    private final WebClient.Builder webClientBuilder;

    public StandardHeaderE2eController(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @GetMapping("/standard-header-e2e")
    @CpfTransaction(id = "ACC09TST0007", name = "ACCStandardHeaderE2e")
    @Operation(
            summary = "표준 헤더 runtime E2E smoke",
            description = "현재 거래 컨텍스트의 표준/확장 헤더를 mock downstream으로 전파하고 로그 저장 여부를 검증할 수 있게 합니다.")
    public ResponseEntity<Map<String, Object>> verifyStandardHeaderE2e(
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
