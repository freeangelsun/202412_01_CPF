package com.cpf.batch.worker;

import com.cpf.batch.common.base.BatBaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 현재 BAT worker 프로세스의 식별 정보와 처리 용량을 조회합니다. */
@RestController
@RequestMapping("/bat/api/worker")
@Tag(name = "BAT Worker", description = "독립 BAT worker 프로세스 상태 API")
public class BatWorkerController extends BatBaseController {
    private final BatWorkerAgent workerAgent;

    public BatWorkerController(BatWorkerAgent workerAgent) {
        this.workerAgent = workerAgent;
    }

    @GetMapping
    @Operation(summary = "현재 worker 상태 조회", description = "workerId, instanceId, 버전, capability, 처리 용량과 제어 상태를 조회합니다.")
    public ResponseEntity<BatWorkerAgent.WorkerSnapshot> status() {
        return ok(workerAgent.snapshot());
    }
}
