package cpf.bat.edu.ondemand;

import cpf.pfw.common.base.BaseController;
import cpf.pfw.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

/** 온디맨드 배치 접수·상태·step·중지 EDU API입니다. */
@RestController
@RequestMapping("/bat/api/v1/edu/on-demand")
@Tag(name = "BAT EDU 온디맨드", description = "표준 배치 ID를 202로 접수하고 실제 Spring Batch 실행을 추적합니다.")
public class BatOnDemandController extends BaseController {
    private final BatOnDemandService service;

    public BatOnDemandController(BatOnDemandService service) {
        this.service = service;
    }

    @PostMapping
    @CpfOnlineTransaction(
            id = "OBATOD0001", name = "BAT 온디맨드 접수", ownerDomain = "BAT",
            requiredPermission = "BAT_ON_DEMAND_EXECUTE", auditReasonRequired = true)
    @Operation(operationId = "submitBatOnDemand", summary = "온디맨드 배치 실행 접수")
    public ResponseEntity<BatOnDemandStatus> submit(@Valid @RequestBody BatOnDemandRequest request) {
        BatOnDemandStatus status = service.submit(request);
        URI location = URI.create("/bat/api/v1/edu/on-demand/" + status.executionRequestId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).location(location).body(status);
    }

    @GetMapping("/{executionRequestId}")
    @CpfOnlineTransaction(id = "OBATOD0002", name = "BAT 온디맨드 상태 조회", ownerDomain = "BAT",
            requiredPermission = "BAT_ON_DEMAND_READ")
    @Operation(operationId = "getBatOnDemandStatus", summary = "온디맨드 배치 상태 조회")
    public ResponseEntity<BatOnDemandStatus> status(@PathVariable String executionRequestId) {
        return ResponseEntity.ok(service.status(executionRequestId));
    }

    @GetMapping("/{executionRequestId}/steps")
    @CpfOnlineTransaction(id = "OBATOD0003", name = "BAT 온디맨드 step 조회", ownerDomain = "BAT",
            requiredPermission = "BAT_ON_DEMAND_READ")
    @Operation(operationId = "getBatOnDemandSteps", summary = "온디맨드 배치 step 조회")
    public ResponseEntity<List<Map<String, Object>>> steps(@PathVariable String executionRequestId) {
        return ResponseEntity.ok(service.steps(executionRequestId));
    }

    @PostMapping("/{executionRequestId}/stop")
    @CpfOnlineTransaction(
            id = "OBATOD0004", name = "BAT 온디맨드 중지", ownerDomain = "BAT",
            requiredPermission = "BAT_ON_DEMAND_STOP", auditReasonRequired = true)
    @Operation(operationId = "stopBatOnDemand", summary = "온디맨드 배치 중지")
    public ResponseEntity<BatOnDemandStatus> stop(
            @PathVariable String executionRequestId,
            @RequestHeader("X-Operator-Id") String requestUser,
            @RequestHeader("X-Audit-Reason") String reason) {
        return ResponseEntity.ok(service.stop(executionRequestId, requestUser, reason));
    }

    @PostMapping("/{executionRequestId}/restart")
    @CpfOnlineTransaction(
            id = "OBATOD0005", name = "BAT 온디맨드 재시작", ownerDomain = "BAT",
            requiredPermission = "BAT_ON_DEMAND_RESTART", auditReasonRequired = true)
    @Operation(operationId = "restartBatOnDemand", summary = "실패한 Spring Batch 실행 재시작")
    public ResponseEntity<BatOnDemandStatus> restart(
            @PathVariable String executionRequestId,
            @RequestHeader("X-Operator-Id") String requestUser,
            @RequestHeader("X-Audit-Reason") String reason) {
        return ResponseEntity.accepted().body(service.restart(executionRequestId, requestUser, reason));
    }

    @PostMapping("/{executionRequestId}/rerun")
    @CpfOnlineTransaction(
            id = "OBATOD0006", name = "BAT 온디맨드 재수행", ownerDomain = "BAT",
            requiredPermission = "BAT_ON_DEMAND_RERUN", auditReasonRequired = true)
    @Operation(operationId = "rerunBatOnDemand", summary = "기존 파라미터 기반 신규 배치 재수행")
    public ResponseEntity<BatOnDemandStatus> rerun(
            @PathVariable String executionRequestId,
            @RequestHeader("X-Operator-Id") String requestUser,
            @RequestHeader("X-Audit-Reason") String reason) {
        return ResponseEntity.accepted().body(service.rerun(executionRequestId, requestUser, reason));
    }
}
