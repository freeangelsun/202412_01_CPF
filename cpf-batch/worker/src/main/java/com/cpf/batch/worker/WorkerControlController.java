package com.cpf.batch.worker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Operational admission control for the Spring Batch/Kafka worker listener. */
@RestController
@RequestMapping("/internal/v1/worker")
public final class WorkerControlController {
    private final SpringBatchWorkerRuntimeState runtime;

    public WorkerControlController(SpringBatchWorkerRuntimeState runtime) {
        this.runtime = runtime;
    }

    @PostMapping("/drain")
    ResponseEntity<Void> drain() {
        runtime.drain();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/resume")
    ResponseEntity<Void> resume() {
        runtime.resume();
        return ResponseEntity.accepted().build();
    }
}
