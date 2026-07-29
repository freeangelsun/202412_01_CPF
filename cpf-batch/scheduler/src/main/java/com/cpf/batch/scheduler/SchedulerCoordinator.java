package com.cpf.batch.scheduler;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.RuntimeStateProvider;
import com.cpf.batch.scheduler.internal.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
@Component
public class SchedulerCoordinator implements RuntimeStateProvider {
    static final String LEASE_KEY = "BAT_SCHEDULER";
    private final JdbcSchedulerLeaderRepository repository;
    private final String instanceId;
    private final Duration duration;
    private final AtomicReference<JdbcSchedulerLeaderRepository.Lease> lease = new AtomicReference<>();
    private final AtomicReference<String> lastElectionError = new AtomicReference<>();
    private final AtomicBoolean electionAttempted = new AtomicBoolean();

    public SchedulerCoordinator(
            JdbcSchedulerLeaderRepository repository,
            @Value("${cpf.batch.runtime.instance-id:${CPF_INSTANCE_ID:scheduler-local-01}}") String instanceId,
            @Value("${cpf.batch.scheduler.lease-seconds:15}") long leaseSeconds) {
        this.repository = repository;
        this.instanceId = instanceId;
        this.duration = Duration.ofSeconds(Math.max(5, leaseSeconds));
    }

    @Scheduled(fixedDelayString = "${cpf.batch.scheduler.election-ms:3000}")
    public void elect() {
        electionAttempted.set(true);
        try {
            var current = lease.get();
            if (current != null && repository.heartbeat(LEASE_KEY, current, duration)) {
                lastElectionError.set(null);
                return;
            }
            lease.set(repository.acquire(LEASE_KEY, instanceId, duration).orElse(null));
            lastElectionError.set(null);
        } catch (RuntimeException failure) {
            lease.set(null);
            lastElectionError.set(failure.getClass().getSimpleName());
            throw failure;
        }
    }

    public JdbcSchedulerLeaderRepository.Lease assertLeader(long fencingToken) {
        var current = lease.get();
        if (current == null || current.fencingToken() != fencingToken
                || !repository.isCurrent(LEASE_KEY, current)) {
            lease.compareAndSet(current, null);
            throw new IllegalStateException("Stale scheduler leader fenced");
        }
        return current;
    }

    public long fencingToken() {
        return lease.get() == null ? 0 : lease.get().fencingToken();
    }

    @Override
    public ActualState actualState() {
        if (lastElectionError.get() != null) {
            return ActualState.DEGRADED;
        }
        return lease.get() == null ? ActualState.STARTING : ActualState.READY;
    }

    @Override
    public boolean ready() {
        return lastElectionError.get() == null && lease.get() != null;
    }

    @Override
    public int availableCapacity() {
        return ready() ? 1 : 0;
    }

    @Override
    public Map<String, String> dependencyHealth() {
        return Map.of(
                "schedulerLeaseStore",
                !electionAttempted.get()
                        ? "UNKNOWN"
                        : (lastElectionError.get() == null ? "UP" : "DOWN"));
    }

    @Override
    public String lastErrorCode() {
        return lastElectionError.get() == null ? null : "BAT_SCHEDULER_ELECTION_FAILED";
    }
}
