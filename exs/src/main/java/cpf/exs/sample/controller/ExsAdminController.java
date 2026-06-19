package cpf.exs.sample.controller;

import cpf.exs.operation.service.ExsOperationService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * EXS 교육/호환용 조회 API입니다.
 *
 * <p>과거 샘플 경로를 유지하되 하드코딩 응답을 만들지 않고 운영 서비스에 위임합니다.</p>
 */
@RestController
@RequestMapping("/api/exs/sample")
@Tag(name = "EXS-Sample", description = "EXS 교육/호환 API")
public class ExsAdminController {

    private final ExsOperationService operationService;

    public ExsAdminController(ExsOperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/institutions")
    @CpfTransaction(id = "EXS01INS0001", name = "ExsSampleInstitutionList")
    @Operation(summary = "대외기관 조회 예시", description = "운영 저장소를 사용해 대외기관 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findInstitutions() {
        return ResponseEntity.ok(operationService.findInstitutions());
    }

    @GetMapping("/channels")
    @CpfTransaction(id = "EXS01CHN0001", name = "ExsSampleChannelList")
    @Operation(summary = "대외 채널 조회 예시", description = "운영 저장소를 사용해 대외 채널 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findChannels() {
        return ResponseEntity.ok(operationService.findChannels());
    }

    @GetMapping("/endpoints")
    @CpfTransaction(id = "EXS01END0001", name = "ExsSampleEndpointList")
    @Operation(summary = "대외 endpoint 조회 예시", description = "운영 저장소를 사용해 endpoint 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findEndpoints() {
        return ResponseEntity.ok(operationService.findEndpoints());
    }

    @GetMapping("/auth-profiles")
    @CpfTransaction(id = "EXS01AUT0001", name = "ExsSampleAuthProfileList")
    @Operation(summary = "대외 인증 프로파일 조회 예시", description = "운영 저장소를 사용해 인증 프로파일을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAuthProfiles() {
        return ResponseEntity.ok(operationService.findAuthProfiles());
    }

    @GetMapping("/tokens")
    @CpfTransaction(id = "EXS01TOK0001", name = "ExsSampleTokenList")
    @Operation(summary = "대외 token 상태 조회 예시", description = "운영 저장소를 사용해 token 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTokens() {
        return ResponseEntity.ok(operationService.findTokens());
    }

    @GetMapping("/routes")
    @CpfTransaction(id = "EXS01RTE0001", name = "ExsSampleRouteList")
    @Operation(summary = "대외 라우팅 규칙 조회 예시", description = "운영 저장소를 사용해 라우팅 규칙을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoutes() {
        return ResponseEntity.ok(operationService.findRoutes());
    }

    @GetMapping("/transactions")
    @CpfTransaction(id = "EXS01TRN0001", name = "ExsSampleTransactionLogList")
    @Operation(summary = "대외 거래 로그 조회 예시", description = "운영 저장소를 사용해 대외 거래 로그를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTransactions() {
        return ResponseEntity.ok(operationService.findTransactions(100));
    }

    @GetMapping("/messages")
    @CpfTransaction(id = "EXS01MSG0001", name = "ExsSampleMessageLogList")
    @Operation(summary = "대외 전문 로그 조회 예시", description = "운영 저장소를 사용해 대외 전문 로그를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMessages() {
        return ResponseEntity.ok(operationService.findMessages(100));
    }

    @GetMapping("/control-policies")
    @CpfTransaction(id = "EXS01CTL0001", name = "ExsSampleControlPolicyList")
    @Operation(summary = "대외 통제 정책 조회 예시", description = "운영 저장소를 사용해 통제 정책을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findControlPolicies() {
        return ResponseEntity.ok(operationService.findControlPolicies());
    }

    @GetMapping("/retries")
    @CpfTransaction(id = "EXS01RTY0001", name = "ExsSampleRetryLogList")
    @Operation(summary = "대외 재처리 로그 조회 예시", description = "운영 저장소를 사용해 재처리 요청 이력을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRetries() {
        return ResponseEntity.ok(operationService.findRetryRequests(100));
    }
}
