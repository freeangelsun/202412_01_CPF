package cpf.bat.worker;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 독립 BAT 프로세스의 등록, heartbeat, claim, lease 갱신, drain과 실행 완료를 조정합니다.
 */
@Component
public class BatWorkerAgent {
    private static final Logger log = LoggerFactory.getLogger(BatWorkerAgent.class);

    private final BatWorkerProperties properties;
    private final BatWorkerIdentity identity;
    private final BatWorkerLeaseStore leaseStore;
    private final BatWorkerJobDispatcher dispatcher;
    private final ThreadPoolExecutor executor;
    private final Map<Long, BatWorkerLease> activeLeases = new ConcurrentHashMap<>();
    private volatile String controlStatus = "STOPPED";
    private volatile boolean started;
    private volatile int recoveredLeaseCount;

    public BatWorkerAgent(
            BatWorkerProperties properties,
            BatWorkerLeaseStore leaseStore,
            BatWorkerJobDispatcher dispatcher) {
        this.properties = properties;
        this.identity = BatWorkerIdentity.resolve(properties);
        this.leaseStore = leaseStore;
        this.dispatcher = dispatcher;
        AtomicInteger threadSequence = new AtomicInteger();
        this.executor = new ThreadPoolExecutor(
                properties.maxConcurrency(),
                properties.maxConcurrency(),
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(Math.max(1, properties.queueCapacity())),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("bat-worker-" + identity.workerId() + "-" + threadSequence.incrementAndGet());
                    thread.setDaemon(false);
                    return thread;
                },
                new ThreadPoolExecutor.AbortPolicy());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!properties.enabled()) {
            return;
        }
        leaseStore.register(identity, properties);
        if (properties.recoverExpiredLease()) {
            recoveredLeaseCount = leaseStore.recoverExpiredLeases(identity.workerId());
        }
        controlStatus = leaseStore.heartbeat(identity, "IDLE", null);
        started = true;
        log.info("BAT worker 등록 완료 workerId={}, instanceId={}, version={}, recovered={}",
                identity.workerId(), identity.instanceId(), identity.version(), recoveredLeaseCount);
    }

    @Scheduled(fixedDelayString = "${cpf.bat.worker.poll-interval-ms:1000}")
    public void poll() {
        if (!properties.enabled() || !started || !"RUNNING".equals(controlStatus)) {
            return;
        }
        if (properties.recoverExpiredLease()) {
            recoveredLeaseCount += leaseStore.recoverExpiredLeases(identity.workerId());
        }
        while (activeLeases.size() < properties.maxConcurrency()) {
            Optional<BatWorkerLease> claimed = leaseStore.claim(identity, properties);
            if (claimed.isEmpty()) {
                return;
            }
            BatWorkerLease lease = claimed.get();
            activeLeases.put(lease.executionId(), lease);
            executor.execute(() -> execute(lease));
        }
    }

    @Scheduled(fixedDelayString = "${cpf.bat.worker.heartbeat-interval-ms:2000}")
    public void heartbeat() {
        if (!properties.enabled() || !started) {
            return;
        }
        BatWorkerLease current = activeLeases.values().stream().findFirst().orElse(null);
        String workerStatus = current == null
                ? ("DRAINING".equals(controlStatus) ? "DRAINING" : "IDLE")
                : "RUNNING";
        controlStatus = leaseStore.heartbeat(identity, workerStatus, current);
        for (BatWorkerLease lease : activeLeases.values()) {
            if (!leaseStore.renew(lease, properties.leaseSeconds())) {
                log.error("BAT worker lease 갱신 실패 executionId={}, workerId={}",
                        lease.executionId(), identity.workerId());
            }
        }
        if ("STOPPED".equals(controlStatus)) {
            started = false;
            executor.shutdownNow();
        }
    }

    private void execute(BatWorkerLease lease) {
        if (!leaseStore.markRunning(lease)) {
            activeLeases.remove(lease.executionId());
            return;
        }
        try {
            BatWorkerJobDispatcher.DispatchResult result = dispatcher.dispatch(lease);
            leaseStore.complete(
                    lease,
                    result.status(),
                    result.springBatchExecutionId(),
                    result.failureMessage());
        } catch (RuntimeException ex) {
            leaseStore.complete(lease, "FAILED", null, ex.getClass().getSimpleName() + ": " + ex.getMessage());
            throw ex;
        } finally {
            activeLeases.remove(lease.executionId());
        }
    }

    public WorkerSnapshot snapshot() {
        return new WorkerSnapshot(
                properties.enabled(),
                identity.workerId(),
                identity.instanceId(),
                identity.hostName(),
                identity.processId(),
                identity.version(),
                controlStatus,
                activeLeases.size(),
                properties.maxConcurrency(),
                properties.queueCapacity(),
                properties.capabilities(),
                recoveredLeaseCount,
                Instant.now());
    }

    @PreDestroy
    public void stop() {
        if (!properties.enabled()) {
            return;
        }
        started = false;
        controlStatus = "DRAINING";
        executor.shutdown();
        try {
            if (!executor.awaitTermination(Math.max(1, properties.leaseSeconds()), TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        } finally {
            leaseStore.markStopped(identity, activeLeases.isEmpty() ? "STOPPED" : "FORCED_STOP");
            controlStatus = "STOPPED";
        }
    }

    public record WorkerSnapshot(
            boolean enabled,
            String workerId,
            String instanceId,
            String hostName,
            String processId,
            String version,
            String controlStatus,
            int activeExecutionCount,
            int maxConcurrency,
            int queueCapacity,
            java.util.Set<String> capabilities,
            int recoveredLeaseCount,
            Instant checkedAt) {
    }
}
