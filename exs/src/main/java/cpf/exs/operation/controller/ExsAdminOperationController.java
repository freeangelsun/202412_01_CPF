package cpf.exs.operation.controller;

import cpf.exs.operation.service.ExsOperationService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * EXS 대외 연계 운영 조회 API입니다.
 *
 * <p>기관 기준정보, 라우팅, 거래 로그, 전문 로그를 exsDB 기준으로 조회합니다.</p>
 */
@RestController
@RequestMapping("/api/exs")
@Tag(name = "EXS-Admin", description = "대외 연계 기준정보, 라우팅, 거래/전문 로그 운영 API")
public class ExsAdminOperationController {
    private final ExsOperationService operationService;

    public ExsAdminOperationController(ExsOperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/institutions")
    @CpfTransaction(id = "EXS01INS1001", name = "ExsInstitutionList")
    @Operation(summary = "대외기관 조회", description = "exs_institution 기준 대외기관을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findInstitutions() {
        return ResponseEntity.ok(operationService.findInstitutions());
    }

    @GetMapping("/channels")
    @CpfTransaction(id = "EXS01CHN1001", name = "ExsChannelList")
    @Operation(summary = "대외 채널 조회", description = "기관별 채널, 송수신 방향, 사용 여부를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findChannels() {
        return ResponseEntity.ok(operationService.findChannels());
    }

    @GetMapping("/endpoints")
    @CpfTransaction(id = "EXS01END1001", name = "ExsEndpointList")
    @Operation(summary = "대외 endpoint 조회", description = "기관별 endpoint, timeout, 재시도 기준을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findEndpoints() {
        return ResponseEntity.ok(operationService.findEndpoints());
    }

    @GetMapping("/auth-profiles")
    @CpfTransaction(id = "EXS01AUT1001", name = "ExsAuthProfileList")
    @Operation(summary = "대외 인증 프로파일 조회", description = "secret 원문 없이 인증 프로파일 참조 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAuthProfiles() {
        return ResponseEntity.ok(operationService.findAuthProfiles());
    }

    @GetMapping("/tokens")
    @CpfTransaction(id = "EXS01TOK1001", name = "ExsTokenList")
    @Operation(summary = "대외 token 상태 조회", description = "원문 token 없이 hash preview, 마스킹 token, 만료 시각을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTokens() {
        return ResponseEntity.ok(operationService.findTokens());
    }

    @GetMapping("/routes")
    @CpfTransaction(id = "EXS01RTE1001", name = "ExsRouteList")
    @Operation(summary = "대외 라우팅 규칙 조회", description = "기관/채널/endpoint 기준 라우팅 규칙을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoutes() {
        return ResponseEntity.ok(operationService.findRoutes());
    }

    @GetMapping("/transactions")
    @CpfTransaction(id = "EXS01TRN1001", name = "ExsTransactionLogList")
    @Operation(summary = "대외 거래 로그 조회", description = "CPF 거래 ID와 외부 거래 ID를 함께 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTransactions(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(operationService.findTransactions(limit));
    }

    @GetMapping("/messages")
    @CpfTransaction(id = "EXS01MSG1001", name = "ExsMessageLogList")
    @Operation(summary = "대외 전문 로그 조회", description = "마스킹된 전문 요약과 payload 저장 참조를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMessages(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(operationService.findMessages(limit));
    }

    @GetMapping("/control-policies")
    @CpfTransaction(id = "EXS01CTL1001", name = "ExsControlPolicyList")
    @Operation(summary = "대외 통제 정책 조회", description = "기관별 통제 유형, 사용 여부, 통제 사유를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findControlPolicies() {
        return ResponseEntity.ok(operationService.findControlPolicies());
    }

    @GetMapping("/retries")
    @CpfTransaction(id = "EXS01RTY1001", name = "ExsRetryLogList")
    @Operation(summary = "대외 재처리 로그 조회", description = "재처리 요청 상태, 횟수, 다음 수행 예정 시각을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRetries(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(operationService.findRetryRequests(limit));
    }
}
