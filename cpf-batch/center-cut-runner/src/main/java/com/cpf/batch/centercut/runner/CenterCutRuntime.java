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
        repository.recoverExpiredToUnknown();
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.poll-ms:500}")
    public void poll() {
        if (draining.get() || !runtimeEnabled() || current.get() != null) {
            return;
        }
        repository.claim(runnerId, pool, leaseDuration).ifPresent(claim -> {
            if (!runtimeEnabled() || !current.compareAndSet(null, claim)) {
                return;
            }
            executor.submit(() -> {
                try {
                    dispatcher.execute(claim);
                } finally {
                    current.compareAndSet(claim, null);
                }
            });
        });
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.heartbeat-ms:5000}")
    public void renew() {
        JdbcCenterCutClaimRepository.Claim claim = current.get();
        if (claim != null) {
            repository.renew(claim, leaseDuration);
        }
    }

    public void drain() {
        draining.set(true);
    }

    public void resume() {
        draining.set(false);
    }

    @Override
    public ActualState actualState() {
        return draining.get() || !runtimeEnabled()
                ? ActualState.DRAINING
                : (current.get() == null ? ActualState.READY : ActualState.BUSY);
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
    public long fencingToken() {
        return current.get() == null ? 0 : current.get().fencingToken();
    }

    @Override
    public void close() {
        draining.set(true);
        executor.shutdown();
    }
}
