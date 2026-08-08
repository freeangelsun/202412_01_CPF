package com.cpf.testkit;

import com.cpf.core.api.health.CpfDependencyHealth;
import com.cpf.core.api.health.CpfHealthProbe;
import com.cpf.core.api.health.CpfHealthStatus;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public final class CpfHealthProbeStub implements CpfHealthProbe {
    private final String name;
    private final AtomicReference<CpfHealthStatus> status = new AtomicReference<>(CpfHealthStatus.UP);
    public CpfHealthProbeStub(String name) { this.name = name; }
    public CpfHealthProbeStub status(CpfHealthStatus next) { status.set(next); return this; }
    @Override public CpfDependencyHealth probe() {
        return new CpfDependencyHealth(name, "test", status.get(), "testkit", Instant.EPOCH, 0, Map.of());
    }
}
