package com.cpf.batch.runtime.centercut;

import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** BAT Center-Cut 등록/실행/중단 운영 API. */
@RestController
@RequestMapping("/bat/api/center-cut")
@Tag(name="BAT-CenterCut", description="Center-Cut Runner Registry, 실행, 중단, 최근 결과")
public class BatCenterCutOperationController {
    private final BatCenterCutRegistry registry;
    private final BatCenterCutRunner runner;

    public BatCenterCutOperationController(BatCenterCutRegistry registry, BatCenterCutRunner runner) {
        this.registry = registry;
        this.runner = runner;
    }

    @GetMapping("/definitions")
    @CpfOnlineTransaction(id="OBATCC0101", name="BatCenterCutDefinitions")
    @Operation(operationId="batCenterCutDefinitions", summary="Center-Cut 정의 목록")
    public ResponseEntity<List<Map<String,Object>>> definitions() { return ResponseEntity.ok(registry.describe()); }

    @PostMapping("/{jobId}/run")
    @CpfOnlineTransaction(id="OBATCC0102", name="BatCenterCutRun")
    @Operation(operationId="batCenterCutRun", summary="Center-Cut 실행")
    public ResponseEntity<BatCenterCutRunResult> run(
            @PathVariable String jobId,
            @RequestParam(required=false) Integer limit,
            @RequestParam(required=false) Double ratePerSecond) {
        return ResponseEntity.ok(runner.run(jobId, limit, ratePerSecond));
    }

    @PostMapping("/{jobId}/stop")
    @CpfOnlineTransaction(id="OBATCC0103", name="BatCenterCutStop")
    @Operation(operationId="batCenterCutStop", summary="실행 중 Center-Cut Stop 요청")
    public ResponseEntity<Map<String,Object>> stop(@PathVariable String jobId) {
        boolean accepted = runner.requestStop(jobId);
        return ResponseEntity.ok(Map.of("jobId", jobId, "accepted", accepted));
    }

    @GetMapping("/{jobId}/last-run")
    @CpfOnlineTransaction(id="OBATCC0104", name="BatCenterCutLastRun")
    @Operation(operationId="batCenterCutLastRun", summary="최근 Center-Cut 실행 결과")
    public ResponseEntity<?> lastRun(@PathVariable String jobId) {
        BatCenterCutRunResult result = runner.lastRun(jobId);
        return result == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(result);
    }
}
