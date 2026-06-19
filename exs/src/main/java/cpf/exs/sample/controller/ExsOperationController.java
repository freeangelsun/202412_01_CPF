package cpf.exs.sample.controller;

import cpf.exs.sample.service.ExsOperationService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * EXS 대외 연계 운영 API입니다.
 */
@RestController
@RequestMapping("/api/exs/operations")
@Tag(name = "EXS-Operations", description = "대외 token, 통제 정책, 재처리 운영 API")
public class ExsOperationController {
    private final ExsOperationService operationService;

    public ExsOperationController(ExsOperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/tokens")
    @CpfTransaction(id = "EXS01OPT0001", name = "ExsOperationTokenList")
    @Operation(summary = "대외 token 상태 조회", description = "원문 token 없이 hash preview, 마스킹 token, 만료 시각을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTokens() {
        return ResponseEntity.ok(operationService.findTokens());
    }

    @PostMapping("/tokens/refresh")
    @CpfTransaction(id = "EXS02OPT0002", name = "ExsOperationTokenRefresh")
    @Operation(summary = "대외 token 갱신", description = "외부기관 인증 프로파일 기준으로 token을 갱신하고 원문 대신 hash와 마스킹 값을 남깁니다.")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody ExsOperationService.TokenRefreshRequest request) {
        return ResponseEntity.ok(operationService.refreshToken(request));
    }

    @GetMapping("/token-events")
    @CpfTransaction(id = "EXS01OPT0003", name = "ExsOperationTokenEventList")
    @Operation(summary = "대외 token 이벤트 조회", description = "token 갱신/폐기 같은 운영 이벤트를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findTokenEvents(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(operationService.findTokenEvents(limit));
    }

    @PostMapping("/retries")
    @CpfTransaction(id = "EXS02OPT0004", name = "ExsOperationRetryRequest")
    @Operation(summary = "대외 재처리 요청", description = "transactionGlobalId 기준 재처리 요청을 등록하고 운영 사유를 남깁니다.")
    public ResponseEntity<Map<String, Object>> requestRetry(@RequestBody ExsOperationService.RetryRequest request) {
        return ResponseEntity.ok(operationService.requestRetry(request));
    }

    @GetMapping("/retries")
    @CpfTransaction(id = "EXS01OPT0005", name = "ExsOperationRetryList")
    @Operation(summary = "대외 재처리 요청 조회", description = "대외 재처리 요청 상태와 사유를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRetryRequests(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(operationService.findRetryRequests(limit));
    }

    @PostMapping("/control-policies")
    @CpfTransaction(id = "EXS02OPT0006", name = "ExsOperationControlPolicySave")
    @Operation(summary = "대외 통제 정책 저장", description = "외부기관/통제유형별 허용 여부와 운영 사유를 저장합니다.")
    public ResponseEntity<Map<String, Object>> saveControlPolicy(@RequestBody ExsOperationService.ControlPolicyRequest request) {
        return ResponseEntity.ok(operationService.saveControlPolicy(request));
    }

    @GetMapping("/control-policies")
    @CpfTransaction(id = "EXS01OPT0007", name = "ExsOperationControlPolicyList")
    @Operation(summary = "대외 통제 정책 조회", description = "외부기관/통제유형별 허용 여부를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findControlPolicies() {
        return ResponseEntity.ok(operationService.findControlPolicies());
    }
}
