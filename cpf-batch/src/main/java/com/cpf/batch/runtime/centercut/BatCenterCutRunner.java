package com.cpf.batch.runtime.centercut;

import com.cpf.core.spi.centercut.CenterCutHandler;
import com.cpf.core.api.centercut.CpfCenterCutResult;
import com.cpf.core.api.centercut.CpfCenterCutStatus;
import com.cpf.core.api.centercut.CpfCenterCutSummary;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.LockSupport;

/**
 * BAT가 소유하는 독립 Center-Cut Runner.
 *
 * <p>jobId별 중복 실행을 차단하고 처리 한도, Rate, Stop 요청을 일관되게 집행합니다.
 * 실제 대상 Claim/Fencing/결과 저장은 Owner Adapter인 {@code CenterCutTargetProvider}가 담당합니다.</p>
 */
@Service
public class BatCenterCutRunner {
    private final BatCenterCutRegistry registry;
    private final BatCenterCutService service;
    private final Map<String, ReentrantLock> jobLocks = new ConcurrentHashMap<>();
    private final Map<String, AtomicBoolean> stopRequests = new ConcurrentHashMap<>();
    private final Map<String, BatCenterCutRunResult> lastRuns = new ConcurrentHashMap<>();

    public BatCenterCutRunner(BatCenterCutRegistry registry, BatCenterCutService service) {
        this.registry = registry;
        this.service = service;
    }

    public BatCenterCutRunResult run(String jobId, Integer requestedLimit, Double requestedRate) {
        BatCenterCutDefinition definition = registry.find(jobId)
                .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 Center-Cut jobId입니다: " + jobId));
        ReentrantLock lock = jobLocks.computeIfAbsent(jobId, ignored -> new ReentrantLock());
        if (!lock.tryLock()) {
            throw new IllegalStateException("동일 Center-Cut Job이 이미 실행 중입니다: " + jobId);
        }
        Instant started = Instant.now();
        String runId = UUID.randomUUID().toString();
        AtomicBoolean stop = stopRequests.computeIfAbsent(jobId, ignored -> new AtomicBoolean(false));
        stop.set(false);
        try {
            int limit = requestedLimit == null ? definition.defaultLimit() : Math.max(1, requestedLimit);
            if (limit > definition.maxLimit()) {
                throw new IllegalArgumentException("요청 limit이 maxLimit을 초과했습니다. maxLimit=" + definition.maxLimit());
            }
            double rate = requestedRate == null ? definition.ratePerSecond() : Math.max(0.0d, requestedRate);
            CenterCutHandler guardedHandler = target -> {
                if (stop.get()) {
                    return new CpfCenterCutResult(
                            target.targetId(), CpfCenterCutStatus.STOP_REQUESTED,
                            "운영자 Stop 요청으로 신규 item 처리를 중단합니다.", null,
                            target.transactionSegmentId());
                }
                CpfCenterCutResult result = definition.handler().handle(target);
                if (rate > 0.0d) {
                    long nanos = Math.max(1L, Math.round(1_000_000_000.0d / rate));
                    LockSupport.parkNanos(nanos);
                }
                return result;
            };
            CpfCenterCutSummary summary = service.execute(jobId, limit, definition.provider(), guardedHandler);
            String status = summary.unknownResultCount() > 0 ? "COMPLETED_WITH_UNKNOWN"
                    : summary.failedCount() > 0 ? "COMPLETED_WITH_FAILURE"
                    : summary.stopRequestedCount() > 0 ? "STOPPED" : "COMPLETED";
            BatCenterCutRunResult result = new BatCenterCutRunResult(
                    jobId, runId, status, started, Instant.now(), summary, null);
            lastRuns.put(jobId, result);
            return result;
        } catch (RuntimeException ex) {
            BatCenterCutRunResult result = new BatCenterCutRunResult(
                    jobId, runId, "FAILED", started, Instant.now(), null, ex.getMessage());
            lastRuns.put(jobId, result);
            throw ex;
        } finally {
            stop.set(false);
            lock.unlock();
        }
    }

    public boolean requestStop(String jobId) {
        if (!jobLocks.containsKey(jobId)) return false;
        stopRequests.computeIfAbsent(jobId, ignored -> new AtomicBoolean()).set(true);
        return true;
    }

    public BatCenterCutRunResult lastRun(String jobId) {
        return lastRuns.get(jobId);
    }
}
