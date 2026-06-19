package cpf.exs.flow.controller;

import cpf.exs.flow.service.ExsFlowService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.logging.TransactionIdGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * EXS 대외 수신/송신 표준 API입니다.
 *
 * <p>샘플 API와 달리 이 API는 <code>X-Transaction-Id</code>를 필수로 검증합니다. 누락 또는 형식 오류는
 * 표준 오류로 반환하고, 정상 요청은 exsDB 거래/전문 로그에 선저장합니다.</p>
 */
@RestController
@RequestMapping("/api/exs")
@Tag(name = "EXS-Flow", description = "대외 수신/송신 처리 표준 API")
public class ExsFlowController {
    private final ExsFlowService flowService;

    public ExsFlowController(ExsFlowService flowService) {
        this.flowService = flowService;
    }

    @PostMapping("/inbound")
    @CpfTransaction(id = "EXS02INB1001", name = "ExsInbound")
    @Operation(summary = "대외 수신 선저장", description = "X-Transaction-Id를 검증하고 대외 수신 거래/전문 로그를 DB에 선저장합니다.")
    public ResponseEntity<Map<String, Object>> receiveInbound(
            @RequestHeader(TransactionContext.HEADER_TRANSACTION_ID) String transactionGlobalId,
            @RequestBody(required = false) ExsFlowService.ExchangeRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(flowService.receiveInbound(validateTransactionId(transactionGlobalId), request, servletRequest.getRequestURI()));
    }

    @PostMapping("/outbound")
    @CpfTransaction(id = "EXS02OUT1001", name = "ExsOutbound")
    @Operation(summary = "대외 송신 선저장", description = "X-Transaction-Id를 검증하고 대외 송신 거래/전문 로그를 DB에 선저장합니다.")
    public ResponseEntity<Map<String, Object>> sendOutbound(
            @RequestHeader(TransactionContext.HEADER_TRANSACTION_ID) String transactionGlobalId,
            @RequestBody(required = false) ExsFlowService.ExchangeRequest request,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(flowService.sendOutbound(validateTransactionId(transactionGlobalId), request, servletRequest.getRequestURI()));
    }

    private String validateTransactionId(String transactionGlobalId) {
        if (!TransactionIdGenerator.isValid(transactionGlobalId, 7)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "X-Transaction-Id 형식이 올바르지 않습니다.");
        }
        return transactionGlobalId;
    }
}
