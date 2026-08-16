package com.cpf.platform.operations.health;
import com.cpf.platform.operations.api.health.CpfHealthSnapshotProvider;
import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
/** Actuator read-only Health endpoint입니다. */
@Endpoint(id="cpfRuntimeHealth")
public final class CpfHealthEndpoint {
    private final CpfHealthSnapshotProvider provider;
    public CpfHealthEndpoint(CpfHealthSnapshotProvider provider){this.provider=provider;}
    @ReadOperation public CpfRuntimeHealth health(){return provider.snapshot();}
}
