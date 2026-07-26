package com.cpf.batch.scheduler; import com.cpf.batch.runtime.RuntimeStateProvider; import com.cpf.batch.scheduler.internal.*; import org.springframework.beans.factory.annotation.Value; import org.springframework.scheduling.annotation.Scheduled; import org.springframework.stereotype.Component; import java.time.Duration; import java.util.concurrent.atomic.AtomicReference;
@Component
public class SchedulerCoordinator implements RuntimeStateProvider {
    static final String LEASE_KEY = "BAT_SCHEDULER";
    private final JdbcSchedulerLeaderRepository repository;
    private final String instanceId;
    private final Duration duration;
    private final AtomicReference<JdbcSchedulerLeaderRepository.Lease> lease = new AtomicReference<>();

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
        var current = lease.get();
        if (current != null && repository.heartbeat(LEASE_KEY, current, duration)) {
            return;
        }
        lease.set(repository.acquire(LEASE_KEY, instanceId, duration).orElse(null));
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
}
