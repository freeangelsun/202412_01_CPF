package com.cpf.gateway.runtime;

import com.cpf.gateway.api.*;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Lease/Fencing을 획득한 Gateway 인스턴스만 Protocol Health Probe를 수행합니다. */
@Component
public final class CpfGatewayHealthWorker {
    private final CpfGatewayRegistryPort registry;
    private final CpfGatewayProbeExecutor probes;

    public CpfGatewayHealthWorker(CpfGatewayRegistryPort registry, CpfGatewayProbeExecutor probes) {
        this.registry = registry;
        this.probes = probes;
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.health.worker-millis:10000}")
    /** run 작업을 CPF 표준 계약에 따라 수행한다. */
    public void run() {
        String gatewayInstanceId = CpfInstanceIdentity.current().instanceId();
        for (CpfGatewayRegistryPort.HealthProbeTarget target
                : registry.claimHealthProbes(gatewayInstanceId, 200, 30)) {
            registry.reportHealth(probe(target));
        }
    }

    private CpfGatewayRegistryPort.HealthProbeResult probe(CpfGatewayRegistryPort.HealthProbeTarget target) {
        CpfGatewayProbeExecutor.ProbeResult result = probes.execute(target, "APPLICATION");
        return new CpfGatewayRegistryPort.HealthProbeResult(
                UUID.randomUUID().toString(), target.serverGroupId(), target.instanceId(),
                target.gatewayInstanceId(), target.fencingToken(), result.networkStatus(), result.tcpStatus(),
                result.tlsStatus(), result.applicationStatus(), result.overallStatus(), result.resultCode(),
                result.durationMs(), OffsetDateTime.now(ZoneOffset.UTC), result.certificateNotAfter(),
                result.certificateFingerprintSha256());
    }
}
