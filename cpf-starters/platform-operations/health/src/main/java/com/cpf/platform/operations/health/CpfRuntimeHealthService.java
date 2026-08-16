package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

/** Dependency timeout/concurrency/cache/UNKNOWN fail-safe를 포함하는 Runtime Health 서비스입니다. */
public final class CpfRuntimeHealthService implements CpfHealthSnapshotProvider, AutoCloseable {
    private record Cache(Instant at, List<CpfDependencyHealth> values) {}
    private final CpfHealthConfig config;
    private final CpfDrainControl drain;
    private final List<CpfDependencyHealthCheck> checks;
    private final Clock clock;
    private final Instant startedAt;
    private final ExecutorService executor;
    private final Semaphore permits;
    private final AtomicReference<Cache> cache = new AtomicReference<>();
    public CpfRuntimeHealthService(CpfHealthConfig config, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks) {
        this(config, drain, checks, Clock.systemUTC());
    }
    CpfRuntimeHealthService(CpfHealthConfig config, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks, Clock clock) {
        this.config = config; this.drain = drain; this.checks = List.copyOf(checks); this.clock = clock;
        this.startedAt = clock.instant(); this.executor = Executors.newCachedThreadPool();
        this.permits = new Semaphore(config.maxConcurrentChecks());
    }
    @Override public CpfRuntimeHealth snapshot() {
        List<CpfDependencyHealth> dependencies = dependencyHealth();
        CpfHealthStatus readiness = config.maintenance() || drain.state() != CpfDrainState.RUNNING ? CpfHealthStatus.OUT_OF_SERVICE : CpfHealthStatus.UP;
        for (CpfDependencyHealth dependency : dependencies) readiness = CpfHealthStatus.worst(readiness, dependency.status());
        Instant now = clock.instant();
        return new CpfRuntimeHealth(config.systemId(), config.instanceId(), CpfHealthStatus.UP, readiness, CpfHealthStatus.UP,
                drain.state() != CpfDrainState.RUNNING, config.maintenance(), config.version(), config.buildSha(), startedAt, now,
                Duration.between(startedAt, now).toMillis(), List.of(), List.of(), dependencies, Map.of("state", drain.state().name()));
    }
    private List<CpfDependencyHealth> dependencyHealth() {
        Instant now = clock.instant(); Cache current = cache.get();
        if (current != null && Duration.between(current.at(), now).compareTo(config.cacheTtl()) < 0) return current.values();
        var output = new ArrayList<CpfDependencyHealth>(); for (CpfDependencyHealthCheck check : checks) output.add(runSafe(check));
        List<CpfDependencyHealth> immutable = List.copyOf(output); cache.set(new Cache(now, immutable)); return immutable;
    }
    private CpfDependencyHealth runSafe(CpfDependencyHealthCheck check) {
        if (!permits.tryAcquire()) return unknown(check.name(), "capacity", 0);
        long started = System.nanoTime();
        try {
            Future<CpfDependencyHealth> future = executor.submit(check::check);
            try {
                CpfDependencyHealth result = future.get(config.dependencyTimeout().toMillis(), TimeUnit.MILLISECONDS);
                long elapsed = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
                return result == null ? unknown(check.name(), "null-result", elapsed) : sanitize(result, elapsed);
            } catch (TimeoutException timeout) {
                future.cancel(true); return unknown(check.name(), "timeout", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
            } catch (Exception failure) {
                return unknown(check.name(), "failure", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started));
            }
        } finally { permits.release(); }
    }
    private CpfDependencyHealth sanitize(CpfDependencyHealth value, long elapsed) {
        return new CpfDependencyHealth(value.name(), "masked", value.status(), value.reasonCode(), clock.instant(), elapsed, value.details());
    }
    private CpfDependencyHealth unknown(String name, String reason, long elapsed) {
        return new CpfDependencyHealth(name, "masked", CpfHealthStatus.UNKNOWN, reason, clock.instant(), elapsed, Map.of());
    }
    @Override public void close() { executor.shutdownNow(); }
}
