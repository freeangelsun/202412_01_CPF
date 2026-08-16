package com.cpf.testkit;

import com.cpf.platform.operations.api.health.CpfDependencyHealth;
import com.cpf.platform.operations.api.health.CpfHealthStatus;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/** Deterministic dependency-health fixture for tests that need a mutable probe outcome. */
public final class CpfHealthProbeStub implements Supplier<CpfDependencyHealth> {
    private final String name;
    private final AtomicReference<CpfHealthStatus> status = new AtomicReference<>(CpfHealthStatus.UP);
    public CpfHealthProbeStub(String name) { this.name = name; }
    public CpfHealthProbeStub status(CpfHealthStatus next) { status.set(next); return this; }
    @Override public CpfDependencyHealth get() {
        return probe();
    }

    public CpfDependencyHealth probe() {
        return new CpfDependencyHealth(name, "test", status.get(), "testkit", Instant.EPOCH, 0, Map.of());
    }
}
