package com.cpf.batch.centercut.runner;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.runtime.RuntimeStateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/** Center-Cut item claim/execute Runtime이며 공통 Runtime Control 정책을 실제 poll gate에서 소비합니다. */
@Component
public class CenterCutRuntime implements RuntimeStateProvider, AutoCloseable {
    private final JdbcCenterCutClaimRepository repository;
    private final CenterCutDispatcher dispatcher;
    private final String runnerId;
    private final String pool;
    private final Duration leaseDuration;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicBoolean draining = new AtomicBoolean();
    private final AtomicReference<JdbcCenterCutClaimRepository.Claim> current = new AtomicReference<>();
    private final AtomicReference<Long> lostLeaseItemId = new AtomicReference<>();
    private final AtomicReference<String> repositoryError = new AtomicReference<>();
    private volatile BatchRuntimePolicy runtimePolicy = new BatchRuntimePolicy();

    public CenterCutRuntime(
            JdbcCenterCutClaimRepository repository,
            CenterCutDispatcher dispatcher,
            @Value("${cpf.center-cut.runner-id:${CPF_INSTANCE_ID:center-cut-local-01}}") String runnerId,
            @Value("${cpf.center-cut.pool:center-cut}") String pool,
            @Value("${cpf.center-cut.lease-seconds:30}") long leaseSeconds) {
        this.repository = repository;
        this.dispatcher = dispatcher;
        this.runnerId = runnerId;
        this.pool = pool;
        this.leaseDuration = Duration.ofSeconds(Math.max(10, leaseSeconds));
    }

    @Autowired
    public void setRuntimePolicy(BatchRuntimePolicy runtimePolicy) {
        this.runtimePolicy = Objects.requireNonNull(runtimePolicy, "runtimePolicy");
    }

    boolean runtimeEnabled() {
        return runtimePolicy.current().centerCutEnabled();
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.recovery-ms:5000}")
    public void recover() {
        try {
            repository.recoverExpiredToUnknown();
            repositoryError.set(null);
        } catch (RuntimeException failure) {
            repositoryError.set(failure.getClass().getSimpleName());
            throw failure;
        }
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.poll-ms:500}")
    public void poll() {
        if (draining.get() || !runtimeEnabled() || current.get() != null
                || lostLeaseItemId.get() != null || repositoryError.get() != null) {
            return;
        }
        java.util.Optional<JdbcCenterCutClaimRepository.Claim> claimed;
        try {
            claimed = repository.claim(runnerId, pool, leaseDuration);
            repositoryError.set(null);
        } catch (RuntimeException failure) {
            repositoryError.set(failure.getClass().getSimpleName());
            throw failure;
        }
        claimed.ifPresent(claim -> {
            if (!runtimeEnabled() || !current.compareAndSet(null, claim)) {
                return;
            }
            executor.submit(() -> {
                try {
                    dispatcher.execute(claim);
                } finally {
                    current.compareAndSet(claim, null);
                    clearLostLease(claim.itemId());
                }
            });
        });
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.heartbeat-ms:5000}")
    public void renew() {
        JdbcCenterCutClaimRepository.Claim claim = current.get();
        if (claim != null) {
            try {
                boolean renewed = repository.renew(claim, leaseDuration);
                repositoryError.set(null);
                if (!renewed) {
                    lostLeaseItemId.set(claim.itemId());
                    if (current.get() != claim) {
                        clearLostLease(claim.itemId());
                    }
                }
            } catch (RuntimeException failure) {
                repositoryError.set(failure.getClass().getSimpleName());
                throw failure;
            }
        }
    }

    public void drain() {
        draining.set(true);
    }

    public void resume() {
        draining.set(false);
    }

    private void clearLostLease(long itemId) {
        lostLeaseItemId.updateAndGet(currentItemId ->
                currentItemId != null && currentItemId == itemId
                        ? null
                        : currentItemId);
    }

    @Override
    public ActualState actualState() {
        return repositoryError.get() != null || lostLeaseItemId.get() != null
                ? ActualState.DEGRADED
                : draining.get() || !runtimeEnabled()
                ? ActualState.DRAINING
                : (current.get() == null ? ActualState.READY : ActualState.BUSY);
    }

    @Override
    public boolean ready() {
        return repositoryError.get() == null
                && lostLeaseItemId.get() == null
                && !draining.get()
                && runtimeEnabled();
    }

    @Override
    public int availableCapacity() {
        return ready() && current.get() == null ? 1 : 0;
    }

    @Override
    public List<String> currentExecutions() {
        return current.get() == null ? List.of() : List.of(Long.toString(current.get().itemId()));
    }

    @Override
    public List<String> activeLeases() {
        return current.get() == null ? List.of() : List.of(current.get().claimToken());
    }

    @Override
    public boolean draining() {
        return draining.get() || !runtimeEnabled();
    }

    @Override
    public Map<String, String> dependencyHealth() {
        String error = repositoryError.get();
        return Map.of(
                "centerCutRuntime",
                error != null
                        ? "DOWN"
                        : (lostLeaseItemId.get() != null
                                ? "LEASE_LOST"
                                : (ready() ? "UP" : "NOT_READY")));
    }

    @Override
    public String lastErrorCode() {
        String error = repositoryError.get();
        return error != null
                ? "BAT_CENTER_CUT_REPOSITORY_"
                        + error.toUpperCase(java.util.Locale.ROOT)
                : (lostLeaseItemId.get() != null
                        ? "BAT_CENTER_CUT_LEASE_LOST"
                        : (ready() ? null : "BAT_CENTER_CUT_NOT_READY"));
    }

    @Override
    public long fencingToken() {
        return current.get() == null ? 0 : current.get().fencingToken();
    }

    @Override
    public void close() {
        draining.set(true);
        executor.shutdown();
    }
}
