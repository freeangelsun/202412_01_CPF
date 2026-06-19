package cpf.exs.sample.controller;

import cpf.exs.sample.service.ExsSampleService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 대외 수신/송신 처리 흐름에서 CPF 거래 ID와 외부 거래 ID를 분리하는 예시 API입니다.
 */
@RestController
@RequestMapping("/api/exs")
@Tag(name = "EXS-Flow", description = "대외 수신/송신 처리 샘플 API")
public class ExsFlowController {

    private final ExsSampleService sampleService;

    public ExsFlowController(ExsSampleService sampleService) {
        this.sampleService = sampleService;
    }

    @PostMapping("/inbound/sample")
    @CpfTransaction(id = "EXS02INB0001", name = "ExsInboundSample")
    @Operation(summary = "대외 수신 샘플", description = "대외기관 요청을 선저장한 뒤 내부 표준 거래로 변환하는 흐름을 보여줍니다.")
    public ResponseEntity<Map<String, Object>> receiveInbound(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(sampleService.receiveInbound(TransactionContext.getOrCreateTransactionId(), payload));
    }

    @PostMapping("/outbound/sample")
    @CpfTransaction(id = "EXS02OUT0001", name = "ExsOutboundSample")
    @Operation(summary = "대외 송신 샘플", description = "CPF 거래 ID를 외부 호출 헤더로 전파하고 외부 거래 ID를 별도로 저장하는 흐름을 보여줍니다.")
    public ResponseEntity<Map<String, Object>> sendOutbound(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(sampleService.sendOutbound(TransactionContext.getOrCreateTransactionId(), payload));
    }
}
