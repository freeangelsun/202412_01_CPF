package cpf.exs.sample.controller;

import cpf.exs.operation.service.ExsOperationService;
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
 * 대외 수신/송신 처리 흐름에서 CPF 거래 ID와 외부 거래 ID를 분리하는 교육/호환 API입니다.
 *
 * <p>요청 결과는 운영 저장소에 거래 로그와 전문 로그로 남깁니다.</p>
 */
@RestController
@RequestMapping("/api/exs/sample")
@Tag(name = "EXS-Sample-Flow", description = "대외 수신/송신 처리 교육/호환 API")
public class ExsFlowController {

    private final ExsOperationService operationService;

    public ExsFlowController(ExsOperationService operationService) {
        this.operationService = operationService;
    }

    @PostMapping("/inbound")
    @CpfTransaction(id = "EXS02INB0001", name = "ExsInboundSample")
    @Operation(summary = "대외 수신 예시", description = "외부기관 요청을 사전 저장한 뒤 CPF 표준 거래로 변환하는 흐름을 보여줍니다.")
    public ResponseEntity<Map<String, Object>> receiveInbound(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(operationService.receiveInbound(TransactionContext.getOrCreateTransactionId(), payload));
    }

    @PostMapping("/outbound")
    @CpfTransaction(id = "EXS02OUT0001", name = "ExsOutboundSample")
    @Operation(summary = "대외 송신 예시", description = "CPF 거래 ID를 외부 호출 헤더로 전파하고 외부 거래 ID를 별도로 저장하는 흐름을 보여줍니다.")
    public ResponseEntity<Map<String, Object>> sendOutbound(@RequestBody(required = false) Map<String, Object> payload) {
        return ResponseEntity.ok(operationService.sendOutbound(TransactionContext.getOrCreateTransactionId(), payload));
    }
}
