package com.cpf.gateway.runtime;

import com.cpf.gateway.api.CpfGatewayRegistryPort;
import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** 비동기 Gateway Connection Test Operation을 단계별 결과와 함께 실행합니다. */
@Component
public final class CpfGatewayConnectionTestWorker {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayConnectionTestWorker.class);
    private final CpfGatewayRegistryPort registry;
    private final CpfGatewayProbeExecutor probes;

    public CpfGatewayConnectionTestWorker(
            CpfGatewayRegistryPort registry,
            CpfGatewayProbeExecutor probes) {
        this.registry = registry;
        this.probes = probes;
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.connection-test.worker-millis:2000}")
    public void run() {
        String gatewayInstanceId = CpfInstanceIdentity.current().instanceId();
        for (CpfGatewayRegistryPort.ConnectionTestOperation operation
                : registry.claimConnectionTests(gatewayInstanceId, 20)) {
            execute(operation, gatewayInstanceId);
        }
    }

    private void execute(CpfGatewayRegistryPort.ConnectionTestOperation operation, String gatewayInstanceId) {
        try {
            CpfGatewayRegistryPort.ConnectionTestOperation current = registry.findConnectionTestOperation(operation.operationId());
            if (completeCancellationOrExpiry(current)) return;
            String bindingId = current.bindingId();
            CpfGatewayRegistryPort.GatewayBinding binding = registry.findBindings(null, null, null, 10_000).stream()
                    .filter(item -> item.bindingId().equals(bindingId))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Binding not found"));
            List<CpfGatewayRegistryPort.GroupMember> members = registry.findMembers(binding.serverGroupId()).stream()
                    .filter(CpfGatewayRegistryPort.GroupMember::enabled).toList();
            if (members.isEmpty()) throw new IllegalStateException("Enabled Gateway member is empty");
            int success = 0;
            for (CpfGatewayRegistryPort.GroupMember member : members) {
                current = registry.findConnectionTestOperation(current.operationId());
                if (completeCancellationOrExpiry(current)) return;
                Probe probe = probe(binding, member, current.testType());
                if (probe.success()) success++;
                registry.recordConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCommand(
                        UUID.randomUUID().toString(), binding.bindingId(), gatewayInstanceId, member.instanceId(),
                        current.testType(), probe.success() ? "SUCCESS" : "FAILED", probe.failureStage(),
                        probe.durationMs(), UUID.randomUUID().toString(), current.operationId(),
                        OffsetDateTime.now(ZoneOffset.UTC), current.requestedBy()));
            }
            current = registry.findConnectionTestOperation(current.operationId());
            if (completeCancellationOrExpiry(current)) return;
            String state = success == members.size() ? "SUCCESS" : success == 0 ? "FAILED" : "PARTIAL";
            registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                    current.operationId(), state, success + "/" + members.size() + " members succeeded",
                    current.version()));
        } catch (RuntimeException ex) {
            try {
                CpfGatewayRegistryPort.ConnectionTestOperation current =
                        registry.findConnectionTestOperation(operation.operationId());
                if (completeCancellationOrExpiry(current)) return;
                if ("RUNNING".equals(current.status())) {
                    registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                            current.operationId(), "FAILED", sanitize(ex.getMessage()), current.version()));
                }
            } catch (RuntimeException completionFailure) {
                log.error("Gateway Connection Test 실패 상태 기록에 실패했습니다. operationId={}",
                        operation.operationId(), completionFailure);
            }
        }
    }

    private boolean completeCancellationOrExpiry(CpfGatewayRegistryPort.ConnectionTestOperation current) {
        if (!"RUNNING".equals(current.status())) return true;
        if (current.cancelRequested()) {
            registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                    current.operationId(), "CANCELLED", "Cancellation requested", current.version()));
            return true;
        }
        if (current.expiresAt() != null && !current.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                    current.operationId(), "STALE", "Connection Test operation expired", current.version()));
            return true;
        }
        return false;
    }

    private Probe probe(
            CpfGatewayRegistryPort.GatewayBinding binding,
            CpfGatewayRegistryPort.GroupMember member,
            String testType) {
        CpfGatewayRegistryPort.HealthProbeTarget target = registry.claimHealthProbe(
                binding.serverGroupId(), member.instanceId(),
                CpfInstanceIdentity.current().instanceId(), 30);
        if (target == null) return new Probe(false, "LEASE", 0L);
        CpfGatewayProbeExecutor.ProbeResult result = probes.execute(target, binding, testType);
        if (affectsRoutingHealth(testType)) {
            registry.reportHealth(new CpfGatewayRegistryPort.HealthProbeResult(
                    UUID.randomUUID().toString(), target.serverGroupId(), target.instanceId(), target.gatewayInstanceId(),
                    target.fencingToken(), result.networkStatus(), result.tcpStatus(), result.tlsStatus(),
                    result.applicationStatus(), result.overallStatus(), result.resultCode(), result.durationMs(),
                    OffsetDateTime.now(ZoneOffset.UTC), result.certificateNotAfter(),
                    result.certificateFingerprintSha256()));
        }
        return new Probe(result.success(), result.failureStage(), result.durationMs());
    }


    static boolean affectsRoutingHealth(String testType) {
        String normalized = testType == null ? "" : testType.trim().toUpperCase(java.util.Locale.ROOT);
        return "APPLICATION".equals(normalized) || "GATEWAY_E2E".equals(normalized);
    }

    private static String sanitize(String value) {
        String text=Objects.toString(value,"Connection test failed").replaceAll("(?i)(password|token|secret)=[^,\\s]+","$1=***");
        return text.length()>1_000?text.substring(0,1_000):text;
    }
    private record Probe(boolean success,String failureStage,long durationMs) {}
}
