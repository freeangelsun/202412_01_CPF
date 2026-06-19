package cpf.exs.sample.controller;

import cpf.exs.sample.service.ExsSampleService;
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
 * 대외 연계 기준정보와 운영 로그를 조회하는 관리자 API 골격입니다.
 */
@RestController
@RequestMapping("/api/exs")
@Tag(name = "EXS-Admin", description = "대외 연계 관리자 샘플 API")
public class ExsAdminController {

    private final ExsSampleService sampleService;

    public ExsAdminController(ExsSampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/institutions")
    @CpfTransaction(id = "EXS01INS0001", name = "ExsInstitutionList")
    @Operation(summary = "대외기관 조회", description = "기관 코드, 사용 여부, 통제 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findInstitutions() {
        return ResponseEntity.ok(sampleService.findInstitutions());
    }

    @GetMapping("/channels")
    @CpfTransaction(id = "EXS01CHN0001", name = "ExsChannelList")
    @Operation(summary = "대외 채널 조회", description = "기관별 채널, 송수신 방향, enabled 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findChannels() {
        return ResponseEntity.ok(sampleService.findChannels());
    }

    @GetMapping("/endpoints")
    @CpfTransaction(id = "EXS01END0001", name = "ExsEndpointList")
    @Operation(summary = "대외 endpoint 조회", description = "기관별 endpoint, timeout, 재시도 기준을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findEndpoints() {
        return ResponseEntity.ok(sampleService.findEndpoints());
    }

    @GetMapping("/auth-profiles")
    @CpfTransaction(id = "EXS01AUT0001", name = "ExsAuthProfileList")
    @Operation(summary = "대외 인증 프로파일 조회", description = "OAuth/JWT/mock 인증 프로파일 골격을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAuthProfiles() {
        return ResponseEntity.ok(sampleService.findAuthProfiles());
    }

    @GetMapping("/tokens")
    @CpfTransaction(id = "EXS01TOK0001", name = "ExsTokenList")
    @Operation(summary = "대외 토큰 상태 조회", description = "토큰 저장소의 만료/갱신 상태 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTokens() {
        return ResponseEntity.ok(sampleService.findTokens());
    }

    @GetMapping("/routes")
    @CpfTransaction(id = "EXS01RTE0001", name = "ExsRouteList")
    @Operation(summary = "대외 라우팅 규칙 조회", description = "기관/채널/업무 기준 라우팅 규칙을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoutes() {
        return ResponseEntity.ok(sampleService.findRoutes());
    }

    @GetMapping("/transactions")
    @CpfTransaction(id = "EXS01TRN0001", name = "ExsTransactionLogList")
    @Operation(summary = "대외 거래 로그 조회", description = "CPF 거래 ID와 외부 거래 ID를 분리한 거래 로그 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTransactions() {
        return ResponseEntity.ok(sampleService.findTransactions());
    }

    @GetMapping("/messages")
    @CpfTransaction(id = "EXS01MSG0001", name = "ExsMessageLogList")
    @Operation(summary = "대외 송수신 로그 조회", description = "요약/마스킹 중심의 전문 송수신 로그 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMessages() {
        return ResponseEntity.ok(sampleService.findMessages());
    }

    @GetMapping("/control-policies")
    @CpfTransaction(id = "EXS01CTL0001", name = "ExsControlPolicyList")
    @Operation(summary = "대외 통제 정책 조회", description = "기관별 enabled/disabled, 차단 사유, 통제 범위를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findControlPolicies() {
        return ResponseEntity.ok(sampleService.findControlPolicies());
    }

    @GetMapping("/retries")
    @CpfTransaction(id = "EXS01RTY0001", name = "ExsRetryLogList")
    @Operation(summary = "대외 재처리 로그 조회", description = "재처리 가능 상태와 실패 사유 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRetries() {
        return ResponseEntity.ok(sampleService.findRetries());
    }
}
