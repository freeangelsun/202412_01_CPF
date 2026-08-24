package com.cpf.batch.worker.centercut;

import com.cpf.batch.centercut.runtime.CenterCutWorkProcessor;
import com.cpf.batch.worker.SpringBatchWorkerRuntimeState;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** DB 기반 Center-Cut Dispatch Adapter: 한 poll에 제한된 Item만 처리해 다중 Worker가 공정하게 Claim합니다. */
@Component
public final class CenterCutDbWorker {
    private final CenterCutWorkProcessor processor;
    private final SpringBatchWorkerRuntimeState runtime;
    private final String runnerId;
    private final String pool;
    private final Duration lease;
    private final Duration heartbeat;
    private final int maxItemsPerPoll;

    public CenterCutDbWorker(
            CenterCutWorkProcessor processor,
            SpringBatchWorkerRuntimeState runtime,
            @Value("${cpf.batch.worker.worker-id:${CPF_BAT_WORKER_ID:}}") String runnerId,
            @Value("${cpf.batch.worker.center-cut.pool:${CPF_BAT_WORKER_CENTER_CUT_POOL:center-cut}}") String pool,
            @Value("${cpf.batch.worker.center-cut.lease-seconds:${CPF_BAT_WORKER_CENTER_CUT_LEASE_SECONDS:30}}") long leaseSeconds,
            @Value("${cpf.batch.worker.center-cut.heartbeat-ms:${CPF_BAT_WORKER_CENTER_CUT_HEARTBEAT_MS:5000}}") long heartbeatMillis,
            @Value("${cpf.batch.worker.center-cut.max-items-per-poll:${CPF_BAT_WORKER_CENTER_CUT_MAX_ITEMS_PER_POLL:1}}") int maxItemsPerPoll) {
        this.processor = processor;
        this.runtime = runtime;
        this.runnerId = runnerId == null || runnerId.isBlank()
                ? CpfInstanceIdentity.instanceId() : runnerId.trim();
        this.pool = required(pool, "pool");
        if (leaseSeconds < 10) throw new IllegalArgumentException("Center-Cut lease must be at least 10 seconds");
        if (heartbeatMillis < 100 || heartbeatMillis > leaseSeconds * 500L) {
            throw new IllegalArgumentException("Center-Cut heartbeat must be between 100ms and half the lease");
        }
        if (maxItemsPerPoll < 1 || maxItemsPerPoll > 100) {
            throw new IllegalArgumentException("Center-Cut max-items-per-poll must be between 1 and 100");
        }
        this.lease = Duration.ofSeconds(leaseSeconds);
        this.heartbeat = Duration.ofMillis(heartbeatMillis);
        this.maxItemsPerPoll = maxItemsPerPoll;
    }

    @Scheduled(fixedDelayString = "${cpf.batch.worker.center-cut.poll-ms:${CPF_BAT_WORKER_CENTER_CUT_POLL_MS:100}}")
    public void poll() {
        if (!runtime.acceptingCenterCut()) return;
        for (int index=0; index<maxItemsPerPoll && runtime.acceptingCenterCut(); index++) {
            if (processor.processNext(runnerId, pool, lease, heartbeat).isEmpty()) return;
        }
    }

    private static String required(String value,String name) {
        if(value==null||value.isBlank()) throw new IllegalArgumentException("Center-Cut "+name+" is required");
        return value.trim();
    }
}
