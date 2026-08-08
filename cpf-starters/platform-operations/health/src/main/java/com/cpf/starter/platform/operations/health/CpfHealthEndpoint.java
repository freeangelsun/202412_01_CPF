package com.cpf.starter.platform.operations.health;

import com.cpf.core.api.health.CpfDrainControl;
import com.cpf.core.api.health.CpfDrainState;
import com.cpf.core.api.health.CpfHealthSnapshotProvider;
import com.cpf.core.api.health.CpfRuntimeHealth;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

@Endpoint(id="cpfHealth")
public final class CpfHealthEndpoint {
    private final CpfHealthSnapshotProvider provider;
    private final CpfDrainControl drain;
    private final List<CpfDrainAuditSink> auditSinks;
    public CpfHealthEndpoint(CpfHealthSnapshotProvider provider, CpfDrainControl drain, List<CpfDrainAuditSink> auditSinks) {
        this.provider=provider; this.drain=drain; this.auditSinks=List.copyOf(auditSinks);
    }
    @ReadOperation public CpfRuntimeHealth health(){return provider.snapshot();}
    @WriteOperation public Map<String,Object> control(String action, String reason, Long timeoutMillis) {
        String normalized = action == null ? "" : action.trim().toUpperCase();
        if (reason == null || reason.trim().length() < 5) throw new IllegalArgumentException("reason must contain at least 5 characters");
        CpfDrainState result;
        if ("DRAIN".equals(normalized)) {
            long timeout = timeoutMillis == null ? 30_000L : timeoutMillis;
            if (timeout < 0 || timeout > 600_000L) throw new IllegalArgumentException("timeoutMillis must be between 0 and 600000");
            result=drain.beginDrain(Duration.ofMillis(timeout));
        } else if ("RESUME".equals(normalized)) {
            drain.resume(); result=drain.state();
        } else {
            throw new IllegalArgumentException("action must be DRAIN or RESUME");
        }
        Instant now=Instant.now();
        for (CpfDrainAuditSink sink:auditSinks) sink.record(normalized,reason,result,drain.inFlight(),now);
        return Map.of("action",normalized,"state",result,"inFlight",drain.inFlight(),"occurredAt",now.toString());
    }
}
