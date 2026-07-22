package com.cpf.reference.external;

import com.cpf.reference.common.base.ReferenceBaseController;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * timeout, 5xx와 정상 응답을 재현하는 로컬 전용 중립 외부 시스템 시뮬레이터입니다.
 *
 * <p>운영 profile에는 등록되지 않으며 지연 시간도 5초로 제한해 EDU 오용을 방지합니다.</p>
 */
@RestController
@Profile({"local", "dev", "test"})
@RequestMapping({"/api/reference/external-simulator", "/reference/edu/external-simulator"})
@Tag(name = "REF 외부연계 시뮬레이터", description = "CPF 외부 호출 복원력 교육을 위한 로컬 전용 fake server")
public class ReferenceNeutralExternalSimulatorController extends ReferenceBaseController {

    private final Map<String, Map<String, Object>> results = new ConcurrentHashMap<>();

    @GetMapping("/response")
    @CpfOnlineTransaction(
            id = "OREFEX0001", name = "REF 외부연계 시뮬레이터", ownerDomain = "REF",
            description = "로컬 교육 환경에서 정상·지연·오류 응답을 재현합니다.",
            visibility = "INTERNAL", gatewayAllowed = false)
    @Operation(operationId = "simulateNeutralExternalResponse", summary = "중립 외부 시스템 응답 시뮬레이션")
    public ResponseEntity<Map<String, Object>> response(
            @RequestParam(defaultValue = "200") int status,
            @RequestParam(defaultValue = "0") long delayMillis,
            @RequestParam(defaultValue = "EDU") String externalKey) {
        if (status < 200 || status > 599) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_STATUS"));
        }
        sleep(Math.max(0, Math.min(delayMillis, 5000)));
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(Map.of(
                "externalKey", externalKey,
                "status", status,
                "processedAt", Instant.now().toString()));
    }

    @PostMapping("/executions")
    @CpfOnlineTransaction(
            id = "OREFEX0002", name = "REF 대외요청실행", ownerDomain = "REF",
            description = "EXS 로컬 런타임 검증용 대외 요청을 멱등 키와 기관 요청 ID로 접수합니다.",
            visibility = "INTERNAL", gatewayAllowed = false)
    @Operation(operationId = "executeNeutralExternalRequest", summary = "중립 외부 시스템 요청 실행")
    public ResponseEntity<Map<String, Object>> execute(
            @RequestHeader("X-External-Request-Id") String externalRequestId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody Map<String, Object> payload) {
        long delayMillis = longValue(payload.get("simulateDelayMillis"), 0L);
        int status = (int) longValue(payload.get("simulateHttpStatus"), 200L);
        sleep(Math.max(0, Math.min(delayMillis, 5000)));
        if (status < 200 || status > 599) {
            return ResponseEntity.badRequest().body(Map.of("code", "INVALID_STATUS"));
        }
        Map<String, Object> result = Map.of(
                "externalRequestId", externalRequestId,
                "idempotencyKey", idempotencyKey,
                "status", status < 400 ? "COMPLETED" : "FAILED",
                "processedAt", Instant.now().toString());
        results.putIfAbsent(externalRequestId, result);
        return ResponseEntity.status(HttpStatus.valueOf(status)).body(results.get(externalRequestId));
    }

    @GetMapping("/results/{externalRequestId}")
    @CpfOnlineTransaction(
            id = "OREFEX0003", name = "REF 대외결과조회", ownerDomain = "REF",
            description = "기관 요청 ID로 앞서 처리한 결과를 재조회합니다.",
            visibility = "INTERNAL", gatewayAllowed = false)
    @Operation(operationId = "findNeutralExternalResult", summary = "중립 외부 시스템 결과 조회")
    public ResponseEntity<Map<String, Object>> result(@PathVariable String externalRequestId) {
        Map<String, Object> result = results.get(externalRequestId);
        return result == null
                ? ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "externalRequestId", externalRequestId,
                        "status", "NOT_FOUND"))
                : ResponseEntity.ok(result);
    }

    private long longValue(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private void sleep(long delayMillis) {
        if (delayMillis == 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("외부 시스템 시뮬레이션 대기가 중단되었습니다.", ex);
        }
    }
}
