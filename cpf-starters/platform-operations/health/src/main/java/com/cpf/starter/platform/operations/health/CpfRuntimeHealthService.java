package com.cpf.starter.platform.operations.health;

import com.cpf.core.api.health.CpfDependencyHealth;
import com.cpf.core.api.health.CpfDrainControl;
import com.cpf.core.api.health.CpfDrainState;
import com.cpf.core.api.health.CpfHealthSnapshotProvider;
import com.cpf.core.api.health.CpfHealthStatus;
import com.cpf.core.api.health.CpfRuntimeHealth;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public final class CpfRuntimeHealthService implements CpfHealthSnapshotProvider, AutoCloseable {
    private record Cache(Instant at, List<CpfDependencyHealth> values) {}

    private final CpfHealthProperties properties;
    private final CpfDrainControl drain;
    private final List<CpfDependencyHealthCheck> checks;
    private final Clock clock;
    private final Instant startedAt;
    private final ExecutorService executor;
    private final Semaphore permits;
    private final AtomicReference<Cache> cache = new AtomicReference<>();

    public CpfRuntimeHealthService(CpfHealthProperties properties, CpfDrainControl drain,
                                   List<CpfDependencyHealthCheck> checks) {
        this(properties, drain, checks, Clock.systemUTC());
    }

    CpfRuntimeHealthService(CpfHealthProperties properties, CpfDrainControl drain,
                            List<CpfDependencyHealthCheck> checks, Clock clock) {
        this.properties = properties;
        this.drain = drain;
        this.checks = List.copyOf(checks);
        this.clock = clock;
        this.startedAt = clock.instant();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.permits = new Semaphore(properties.getMaxConcurrentChecks());
    }

    @Override
    public CpfRuntimeHealth snapshot() {
        var dependencies = dependencyHealth();
        CpfHealthStatus readiness = properties.isMaintenance() || drain.state() != CpfDrainState.RUNNING
                ? CpfHealthStatus.OUT_OF_SERVICE : CpfHealthStatus.UP;
        for (var dependency : dependencies) readiness = CpfHealthStatus.worst(readiness, dependency.status());
        Instant now = clock.instant();
        return new CpfRuntimeHealth(properties.getSystemId(), properties.getInstanceId(), CpfHealthStatus.UP,
                readiness, CpfHealthStatus.UP, drain.state() != CpfDrainState.RUNNING, properties.isMaintenance(),
                properties.getVersion(), properties.getBuildSha(), startedAt, Duration.between(startedAt, now).toMillis(),
                List.of(), List.of(), dependencies, Map.of("state", drain.state().name()));
    }

    private List<CpfDependencyHealth> dependencyHealth() {
        Instant now = clock.instant();
        var current = cache.get();
        if (current != null && Duration.between(current.at(), now).compareTo(properties.getCacheTtl()) < 0) return current.values();
        var output = new ArrayList<CpfDependencyHealth>();
        for (var check : checks) output.add(runSafe(check));
        var immutable = List.copyOf(output);
        cache.set(new Cache(now, immutable));
        return immutable;
    }

    private CpfDependencyHealth runSafe(CpfDependencyHealthCheck check) {
        if (!permits.tryAcquire()) return unknown(check.name(), "capacity");
        long started = System.nanoTime();
        try {
            Future<CpfDependencyHealth> future = executor.submit(check::check);
            try {
                CpfDependencyHealth result = future.get(properties.getDependencyTimeout().toMillis(), TimeUnit.MILLISECONDS);
                return result == null ? unknown(check.name(), "null-result") : result;
            } catch (TimeoutException timeout) {
                future.cancel(true);
                return unknown(check.name(), "timeout");
            } catch (Exception failure) {
                return unknown(check.name(), "failure");
            }
        } finally {
            permits.release();
        }
    }

    private CpfDependencyHealth unknown(String name, String reason) {
        return new CpfDependencyHealth(name, "masked", CpfHealthStatus.UNKNOWN, reason, clock.instant(), 0, Map.of());
    }

    @Override public void close() { executor.shutdownNow(); }
}
