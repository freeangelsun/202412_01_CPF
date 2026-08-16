package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.*;
import com.cpf.starter.runtime.CpfRuntimeCapabilityInventory;
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
    private final List<String> capabilities;
    private final Map<String,String> publicDiagnostics;
    public CpfRuntimeHealthService(CpfHealthConfig config, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks) {
        this(config, drain, checks, Clock.systemUTC(), List.of(), Map.of());
    }
    public CpfRuntimeHealthService(CpfHealthConfig config, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks,
            CpfRuntimeCapabilityInventory inventory, Map<String,String> runtimeIdentity) {
        this(config, drain, checks, Clock.systemUTC(),
                inventory == null ? List.of() : inventory.capabilityIds(),
                mergeDiagnostics(inventory == null ? Map.of() : inventory.publicDiagnostics(), runtimeIdentity));
    }
    CpfRuntimeHealthService(CpfHealthConfig config, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks, Clock clock) {
        this(config, drain, checks, clock, List.of(), Map.of());
    }
    private CpfRuntimeHealthService(CpfHealthConfig config, CpfDrainControl drain, List<CpfDependencyHealthCheck> checks, Clock clock,
            List<String> capabilities, Map<String,String> publicDiagnostics) {
        this.config = config; this.drain = drain; this.checks = List.copyOf(checks); this.clock = clock;
        this.capabilities = List.copyOf(capabilities == null ? List.of() : capabilities);
        this.publicDiagnostics = Map.copyOf(publicDiagnostics == null ? Map.of() : publicDiagnostics);
        this.startedAt = clock.instant(); this.executor = Executors.newCachedThreadPool();
        this.permits = new Semaphore(config.maxConcurrentChecks());
    }
    private static Map<String,String> mergeDiagnostics(Map<String,String> starter, Map<String,String> identity){
        Map<String,String> merged=new java.util.LinkedHashMap<>(); if(starter!=null)merged.putAll(starter);
        if(identity!=null)identity.forEach((k,v)->{if(k!=null&&v!=null&&!v.isBlank())merged.put(k,v);}); return Map.copyOf(merged);
    }
    @Override public CpfRuntimeHealth snapshot() {
        List<CpfDependencyHealth> dependencies = dependencyHealth();
        CpfHealthStatus readiness = config.maintenance() || drain.state() != CpfDrainState.RUNNING ? CpfHealthStatus.OUT_OF_SERVICE : CpfHealthStatus.UP;
        for (CpfDependencyHealth dependency : dependencies) readiness = CpfHealthStatus.worst(readiness, dependency.status());
        Instant now = clock.instant();
        return new CpfRuntimeHealth(config.systemId(), config.instanceId(), CpfHealthStatus.UP, readiness, CpfHealthStatus.UP,
                drain.state() != CpfDrainState.RUNNING, config.maintenance(), config.version(), config.buildSha(), startedAt, now,
                Duration.between(startedAt, now).toMillis(), List.of(), capabilities, dependencies, runtimeDiagnostics());
    }

    private Map<String,String> runtimeDiagnostics(){
        Map<String,String> out=new java.util.LinkedHashMap<>(publicDiagnostics);
        out.put("state",drain.state().name());
        out.put("capabilityCount",Integer.toString(capabilities.size()));
        return Map.copyOf(out);
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
